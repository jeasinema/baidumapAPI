package baidumapsdk.demo;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.content.ContextWrapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;


/**
 * Created by gzy on 2015/8/21.
 */
public class Bluetooth_service extends Service{
    private static final String TAG = "BluetoothChatService";

    // Constants that indicate the current connection state
    private int mState;
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device


    public static byte[] deviceID = {(byte)0x11,(byte)0x00,(byte)0x55,(byte)0xff,(byte)0xaa,(byte)0xbb,(byte)0xdd,(byte)0x77,(byte)0x88,(byte)0x99,(byte)0xaa,(byte)0x66,(byte)0x33,(byte)0xdd,(byte)0xee,(byte)0xcc };
    public static byte[] PackTail = {(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
    public static byte[] Ack = {(byte) 0xac, (byte) 0xac, (byte) 0xac};
    public static byte[] askForConnecitionPack = {
            (byte)0xbb,//device type:app
            //device ID
            (byte)0x00,(byte)0x11,(byte)0x22,(byte)0x33,(byte)0x44,(byte)0x55,(byte)0x66,(byte)0x77,(byte)0x88,(byte)0x99,(byte)0xaa,(byte)0xbb,(byte)0xcc,(byte)0xdd ,(byte)0xee,(byte)0xff,
            (byte)0x11,//package type:askForConnection
            (byte)0x00,
            (byte)0x00,
            (byte)0x00,(byte)0x11,(byte)0x22,(byte)0x33,(byte)0x44,(byte)0x55,(byte)0x66,(byte)0x77,(byte)0x88,(byte)0x99,(byte)0xaa,(byte)0xbb,(byte)0xcc,(byte)0xdd ,(byte)0xee,(byte)0xff,
            (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
            (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
            (byte)0x15,(byte)0x08,(byte)0x28,(byte)0x17,//currentTime
            (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
            (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
            (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
            (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
            };
    public static byte[] receiveFailedPack = {(byte) 0xdd, (byte) 0xdd, (byte) 0xdd};
    // 扫描到的蓝牙设备，用于创建Socket
    private List<BluetoothDevice> remoteBtList = new ArrayList<>();

    // 蓝牙管理类
    private BluetoothAdapter btAdapter;
    private BluetoothDevice btDevice;
    private BluetoothSocket btSocket;
    private Set<BluetoothDevice> btSet;

    // 蓝牙连接相关广播
    private BroadcastReceiver btConnectedReceiver;
    private BroadcastReceiver btDisConnectReceiver;
    private boolean isConnected;
    private String connectedDevice;

    // 蓝牙扫描相关广播
    private BroadcastReceiver btFoundReceiver;
    private BroadcastReceiver scanFinishedReceiver;

    // 创建Rfcomm通道的UUDI码
    private static final UUID MY_UUID = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");

    //蓝牙mac地址用以配对
    private String btMac = "20:14:00:00:1F:32";
    private static Handler mHandler;//把handler设置成static不知会不会有问题，毕竟不是在这里定义的

    // 输出流
    //public static OutputStream outputStream;
    public Bluetooth_service(){
        mHandler = null;
        mState = STATE_NONE;
    }

    /**
     * Service被绑定后返回一个IBinder
     * IBinder里的方法可以间接控制Service
     * @param intent
     * @return
     */
    @Override
    public IBinder onBind(Intent intent){
        return new myBinder();
    }

    /**
     * myBinder继承了Binder继承了IBinder
     * 这个类里的方法暴露给PoiSearchDemo.java用来控制蓝牙通信
     *
     */
    public class myBinder extends Binder {
        public void start_Bluetooth(){
            Log.v(TAG, "start Bluetooth_Service");
            start();
        }
        public void set_handler(Handler handler){
            setHandler(handler);
        }
    }

    public void setHandler(Handler handler){
        mHandler = handler;
    }

    public void start(){
        Log.v(TAG, "bluetooth_service start");
        init();
        btAdapter= BluetoothAdapter.getDefaultAdapter();
        btDevice = btAdapter.getRemoteDevice(btMac);//此为我的蓝牙mac地址，以后需改为买易网蓝牙地址
        turnOnBluetooth();
        SystemClock.sleep(1000);
        scanBluetooth();
    }

    private void init(){
        // 注册发现蓝牙以及扫描结束的广播
        btFoundReceiver = new BtFoundReceiver();
        scanFinishedReceiver = new BtScanFinishedReceiver();
        IntentFilter foundFilter = new IntentFilter(
                BluetoothDevice.ACTION_FOUND);
        IntentFilter finishFilter = new IntentFilter(
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(btFoundReceiver, foundFilter);
        registerReceiver(scanFinishedReceiver, finishFilter);

        // 注册蓝牙连接及断开广播
        btConnectedReceiver = new BtConnectedReceiver();
        btDisConnectReceiver = new BtDisconnectReceiver();
        IntentFilter connectedFilter = new IntentFilter(
                BluetoothDevice.ACTION_ACL_CONNECTED);
        IntentFilter disconnectedFilter = new IntentFilter(
                BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(btConnectedReceiver, connectedFilter);
        registerReceiver(btDisConnectReceiver, disconnectedFilter);
    }

    private void turnOnBluetooth() {
        Log.v(TAG, "turn on Blutooth");
        if (!btAdapter.isEnabled()){
            Log.v(TAG,"Bluetooth not on , turing up...");
            btAdapter.enable();
        }
        else
            Log.v(TAG, "bluetooth already turned on");
    }

    private void scanBluetooth(){
        if (btAdapter.isEnabled())
            // 扫描周围的蓝牙设备
            btAdapter.startDiscovery();
    }

    public class MyToConnect extends Thread{
        public void run(){
            try{
                btSocket = btDevice.createRfcommSocketToServiceRecord(MY_UUID);
                btSocket.connect();
                //outputStream = btSocket.getOutputStream();
                Log.v(TAG, "bluetooth connected");
                //outputStream = btSocket.getOutputStream();
            }catch(IOException e){
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    Thread toConnect = new Thread(){
        public void run(){
            try{
                btSocket = btDevice.createRfcommSocketToServiceRecord(MY_UUID);
                Log.v(TAG, "bluetooth connecting");
                btSocket.connect();
                //outputStream = btSocket.getOutputStream();
                Log.v(TAG, "bluetooth connected");
                //outputStream = btSocket.getOutputStream();
            }catch(IOException e){
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    };

    /**
     * 管理连接状态下的蓝牙socket
     */
    public class ConnectedThread extends Thread {

        public final BluetoothSocket mSocket;
        public final InputStream mInputStream;
        public final OutputStream mOutputStream;

        public ConnectedThread(BluetoothSocket socket, String socketType){
            Log.d(TAG, "create ConnectedThread: " + socketType);

            mSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                Log.v(TAG,"get in/out Stream");
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mInputStream = tmpIn;
            mOutputStream = tmpOut;
            //mOutputStream = outputStream;
        }

        /**
         * 解析读入的数据并作出相应操作
         * @param buffer
         * @param bytes
         * @return
         */
        public int parseInput(byte[] buffer,int bytes) {
//            if(bytes!=3){
//                try {
//                    mOutputStream.write(receiveFailedPack);
//                }catch (IOException e){
//                    Log.e(TAG,"Write Package failed");
//                }
//            }
            //mHandler.obtainMessage(Constants.MESSAGE_READ, bytes, -1).sendToTarget();
            if(bytes==5){
                if(buffer[0]==-84&&buffer[1]==-84&&buffer[2]==-84){
                    Log.v(TAG,"parse complete,received pack: ack");
                    return Constants.Ack;
                }else{
                    Log.v(TAG,"parse fail");
                    return 0;
                }
            }
            if (bytes==92){
                switch (buffer[0]){
                    case (byte)0xaa:
                        Log.v(TAG,"start parse pack sent from stm32");
                        if(Stm32Crc.CRCVerify(buffer)){
                            Log.v(TAG,"crc verify complete,start collect data");
                            send(Ack);
                            parsePackFromSTM(buffer);
                        }
                        break;
                    case (byte)0xcc:
                        Log.v(TAG,"cannot parse Package from server");
                        break;
                    default:
                        Log.v(TAG,"cannot parse Package");
                        break;
                }
            }
            return 0;
        }


        /**
         * 解析STM发送过来的包并更新UI
         * @param buffer
         */
        public void parsePackFromSTM(byte[] buffer){
            byte[] currentElectro = new byte[4];
            System.arraycopy(buffer,68,currentElectro,0,4);
            Log.v(TAG,"currentElectro = "+Arrays.toString(currentElectro));
            int x = java.nio.ByteBuffer.wrap(currentElectro).order(ByteOrder.LITTLE_ENDIAN).getInt();
            Log.v(TAG,"currentElectro = "+x);
            mHandler.obtainMessage(Constants.MESSAGE_READ, x, 2)
                    .sendToTarget();
        }

        /**
         * 对要发送的数据进行封装并发送
         * @param data
         */
        public void sendPack(byte[] data){
            byte[] Pack = new byte[92];
            System.arraycopy(data,0,Pack,0,data.length);
            System.arraycopy(deviceID,0,Pack,1,deviceID.length);//设置设备ID
            Log.v(TAG,"Packaging Complete ");
            try {
                try {
                    sleep(1000);
                }catch (InterruptedException e){
                    Log.e(TAG, "wait for message completion failed");
                }
                mOutputStream.write(Stm32Crc.addCRC(Pack));
                Log.v(TAG,"Package sent");
            }catch (IOException e){
             //TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        /**
         * 发包，不对包进行封装直接传输数据
         * @param buffer
         */
        public void send(byte[] buffer){
            try {
                try {
                    sleep(1000);
                }catch (InterruptedException e){
                    Log.e(TAG, "wait for message completion failed");
                }
                mOutputStream.write(buffer);
                Log.v(TAG,"Ack sent");
            }catch (IOException e){
                //TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        /**
         * 发送连接请求包
         */
        public void AskForConnection(){
            sendPack(askForConnecitionPack);
        }

        public void run(){
            Log.i(TAG, "BEGIN mConnectedThread");
            AskForConnection();//请求连接
//            String s = "a";
//            byte[] b;
//            b = s.getBytes();
//            Log.i(TAG, "ab");
//            try{
//                try {
//                    sleep(1000);
//                }catch (InterruptedException e){
//                    Log.e(TAG, "wait for message completion failed");
//                }
//                mOutputStream.write(b);
//                Log.i(TAG, "write success");
//            }catch (IOException e){
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    byte[] buffer = new byte[1];
                    int bytes = 0;
                    byte[] inputByteArray = new byte[100];
                    // Read from the InputStream
                    while(mInputStream.available()!=0) {
                        bytes = mInputStream.read(buffer);
                        if(buffer[0]==(byte)0xac){//如果开头是预定义的，则进行包的填充完整
                            try{
                                System.arraycopy(buffer,0,inputByteArray,bytes-1,buffer.length);
                            }catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                sleep(100);
                            }catch (InterruptedException e){
                                Log.e(TAG,"wait for message completion failed");
                            }
                            for(int i = 0;i<4;i++){
                                bytes += mInputStream.read(buffer);
                                //Log.v(TAG, Arrays.toString(buffer) + bytes);
                                try{
                                    System.arraycopy(buffer,0,inputByteArray,bytes-1,buffer.length);
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        }
                        if(buffer[0]==(byte)0xaa){
                            try{
                                System.arraycopy(buffer,0,inputByteArray,bytes-1,buffer.length);
                            }catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                sleep(100);
                            }catch (InterruptedException e){
                                Log.e(TAG,"wait for message completion failed");
                            }
                            for(int i =0 ;i<91;i++){//补全数据包
                                bytes += mInputStream.read(buffer);
                                //Log.v(TAG, Arrays.toString(buffer) + bytes);
                                try{
                                    System.arraycopy(buffer,0,inputByteArray,bytes-1,buffer.length);
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    if(bytes!=0) {
                        // Send the obtained bytes to the UI Activity
                        Log.v(TAG, Arrays.toString(inputByteArray) +"total:"+bytes+"bytes,start parse");
                        parseInput(inputByteArray,bytes);
                        //mOutputStream.write(inputByteArray);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    //info.connectionLost();
                    // Start the service over to restart listening mode
                    //BluetoothChatService.this.start();
                    mHandler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, -1, -1)
                            .sendToTarget();
                    //此处应该有断开连接的broadcast
//                    Intent it = new Intent(BluetoothDevice.ACTION_ACL_DISCONNECTED);
//                    sendBroadcast(it);
                }
            }
        }
    }

    public class BtFoundReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            // TODO Auto-generated method stub
            // 获得扫描到的蓝牙设备对象
            BluetoothDevice device = intent
                    .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            Log.v(TAG,"found:"+device.getName()+",address:"+device.getAddress());
            //每当扫描到新的设备，检查是否为买易网蓝牙，若是，直接连接，若否，继续。
            if (device.getAddress().equals(btDevice.getAddress())) {
                //Toast.makeText(info.this, "Device Founded", Toast.LENGTH_SHORT).show();
                btAdapter.cancelDiscovery();
                toConnect.start();
            }
        }
    }

    private class BtScanFinishedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            // TODO Auto-generated method stub
            mHandler.obtainMessage(Constants.MESSAGE_SCAN_OVERTIME).sendToTarget();
            Log.v(TAG,"scan bluetooth over time");
        }
    }

    private class BtConnectedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            // TODO Auto-generated method stub
            isConnected = true;
            Thread connectedThread = new ConnectedThread(btSocket , "Insecure");
            connectedThread.start();
        }
    }

    private class BtDisconnectReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            Log.v(TAG,"disconnected");
            isConnected = false;
            new MyToConnect().start();
        }
    }



    /**
     * Return the current connection state.
     */
    public synchronized int getState() {
        return mState;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        unregisterReceiver(btFoundReceiver);
        unregisterReceiver(scanFinishedReceiver);
        unregisterReceiver(btConnectedReceiver);
        unregisterReceiver(btDisConnectReceiver);
        //我也不知道为什么，注释了下面一段代码就能不闪退了，可能是bluetooth_service退出时并不销毁socket，而是保持连接
        //并且下次连接的时候再次使用此socket
//        try {
//            if (btSocket != null)
//                btSocket.close();
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
    }
}




