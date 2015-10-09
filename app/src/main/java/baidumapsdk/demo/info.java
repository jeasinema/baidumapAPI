package baidumapsdk.demo;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class info extends Activity {

    //控件
    private TextView userinfo;
    //private Switch switchBtn;
    private TextView tv8;
    private Button back;
    private TextView cd;
    //private ProgressBar progressBar;
    private ListView listView;
    private List<String> showList = new ArrayList<String>();//‘~’ 写法简直酷炫
    private ArrayAdapter<String> btInfoAdapter;
    private TextView dealinfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_info);
        setupVariables();
        //switchSetting();
        //init();
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(info.this);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("logout",true);
                editor.commit();
                Intent intent = new Intent(info.this,Intelligent_electrombile.class);
                startActivity(intent);
                info.this.finish();
            }
        });
        userinfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(info.this, PoiSearchDemo.class);
                startActivity(intent);

            }
        });
        dealinfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(info.this, PoiSearchDemo.class);
                startActivity(intent);
            }
        });
    }

    private void setupVariables(){
        tv8 =(TextView)findViewById(R.id.textView8);
        //switchBtn=(Switch)findViewById(R.id.switch1);
        back =(Button)findViewById(R.id.back);
        userinfo=(TextView)findViewById(R.id.userinfo);
        cd=(TextView)findViewById(R.id.DealInfo);
        //progressBar = (ProgressBar) findViewById(R.id.progressbar);
        dealinfo = (TextView)findViewById(R.id.DealInfo);
    }

//    private void init(){
//        // ListView相关初始化
//        listView = (ListView) findViewById(R.id.listview);
//        btInfoAdapter = new ArrayAdapter<String>(info.this,
//                android.R.layout.simple_list_item_1, showList);
//        listView.setAdapter(btInfoAdapter);
//    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * 设置switch，开关蓝牙
     */
//    private void switchSetting(){
//        //首先检测蓝牙是否打开，打开后是否连接买易网蓝牙，并设置switchButton的状态
//        btAdapter= BluetoothAdapter.getDefaultAdapter();
//        btSet = btAdapter.getBondedDevices();//是否得以打开蓝牙为前提
//        btDevice = btAdapter.getRemoteDevice(btMac);//此为我的蓝牙mac地址，以后需改为买易网蓝牙地址
////        if(btDevice==null)
////            Toast.makeText(info.this,"create btDevice failed",Toast.LENGTH_LONG).show();
////        else
////            Toast.makeText(info.this,"create btDevice not failed",Toast.LENGTH_LONG).show();
//        switchBtn.setChecked(false);
//        isConnected = btAdapter.isEnabled()&&btSet.contains(btDevice)&&btDevice.getBondState()==BluetoothDevice.BOND_BONDED;
//        //初始化switch显示
//        if(isConnected){
//            //switchBtn.setChecked(true);
//            tv8.setText("Bonded");
//        }else {
//            tv8.setText("Not Bonded");
//        }
//        switchBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if(isChecked) {
//                    //在用户按下SwitchButton后，自动进行蓝牙打开，蓝牙搜索，蓝牙socket连接（使用service）。
//                    //跳出弹框，显示配对的蓝牙，若为买易网蓝牙，直接连接。连接后自动跳转至bluetooth.class控制。
//                    cd.setVisibility(View.VISIBLE);
//                    //Toast.makeText(info.this,"Connecting",Toast.LENGTH_LONG).show();
//                    turnOnBluetooth();
//                    SystemClock.sleep(1000);
//                    scanBluetooth();
//                }else {
//                    //tv8.setText("Not Connected");
//
//                }
//            }
//        });
//    }

//    public void TurnToDealInfo(View view){
//        Intent intent =new Intent(info.this,DealInfo.class);
//        startActivity(intent);
//    }

//    private class TryToConnect extends Thread {
//        public void run() {
////            try {
//                Toast.makeText(info.this,"Device Connecting",Toast.LENGTH_LONG).show();
////                btSocket = btDevice.createRfcommSocketToServiceRecord(MY_UUID);
////                btSocket.connect();
////                outputStream = btSocket.getOutputStream();
////            } catch (IOException e) {
////                // TODO Auto-generated catch block
////                e.printStackTrace();
////            }
//        }
//    }

//    Thread toConnect = new Thread(){
//        public void run(){
//            try{
//                //Toast.makeText(info.this,"Device Connecting",Toast.LENGTH_LONG).show();
//                btSocket = btDevice.createRfcommSocketToServiceRecord(MY_UUID);
//                btSocket.connect();
//                outputStream = btSocket.getOutputStream();
//            }catch(IOException e){
//                e.printStackTrace();
//            }
//        }
//    };

//    private void turnOnBluetooth() {
//        if (!btAdapter.isEnabled()) {
//            btAdapter.enable();
//            Toast.makeText(info.this, "蓝牙开启中...", Toast.LENGTH_SHORT)
//                    .show();
//        } else {
//            Toast.makeText(info.this, "蓝牙已开启", Toast.LENGTH_SHORT)
//                    .show();
//        }
//    }

//    private void scanBluetooth(){
//        if (btAdapter.isEnabled()) {
//            // 扫描周围的蓝牙设备
//            btAdapter.startDiscovery();
//            progressBar.setVisibility(View.VISIBLE);
//            //Toast.makeText(info.this, "开始扫描", Toast.LENGTH_SHORT).show();
//        } else {
//            //Toast.makeText(info.this, "请打开蓝牙", Toast.LENGTH_SHORT).show();
//        }
//    }

//    public class BtFoundReceiver extends BroadcastReceiver {
//        @Override
//        public void onReceive(Context arg0, Intent intent) {
//            // TODO Auto-generated method stub
//            // 获得扫描到的蓝牙设备对象
//            BluetoothDevice device = intent
//                    .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//            //new TryToConnect().start();放在这里也会崩溃
//            // 将扫描到的蓝牙设备对象存放到remoteList中
//            if (!remoteBtList.contains(device)) {
//                remoteBtList.add(device);
//                // 将该蓝牙设备相关信息存放到showList中
//                String btInfo = device.getAddress() + "  " + device.getName();
//                showList.add(btInfo);
//                btInfoAdapter.notifyDataSetChanged();
//            }
//            //每当扫描到新的设备，检查是否为买易网蓝牙，若是，直接连接，若否，继续。
//            if (device.getAddress().equals(btDevice.getAddress())) {
//                //Toast.makeText(info.this, "Device Founded", Toast.LENGTH_SHORT).show();
//                btAdapter.cancelDiscovery();
//                toConnect.start();
//            } //else
//            //Toast.makeText(info.this, "found:" + device.getAddress(), Toast.LENGTH_SHORT).show();
//        }
//    }
//
//
//    private class BtScanFinishedReceiver extends BroadcastReceiver {
//        @Override
//        public void onReceive(Context arg0, Intent intent) {
//            // TODO Auto-generated method stub
//            progressBar.setVisibility(View.INVISIBLE);
//            switchBtn.setChecked(false);
//            if (remoteBtList.size() == 0) {
//                Toast.makeText(info.this, "没有扫描到有蓝牙设备，请重试...",
//                        Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//
//    private class BtConnectedReceiver extends BroadcastReceiver {
//
//        @Override
//        public void onReceive(Context arg0, Intent arg1) {
//            // TODO Auto-generated method stub
//            isConnected = true;
//            //tv8.setText("Connected");
//            switchBtn.setChecked(true);
//            Toast.makeText(info.this,"Bluetooth Already Connected",Toast.LENGTH_SHORT).show();
//            //textView.setText("蓝牙连接至" + connectedDevice + "，请进入控制台");
//            //Toast.makeText(MainActivity.this,"Bond status:"+Integer.toString(btDevice.getBondState()),Toast.LENGTH_LONG).show();
//            //turnToControlActivity();
//            Intent intent =new Intent(info.this,PoiSearchDemo.class);
//            dealinfo.setVisibility(View.VISIBLE);
//            startActivity(intent);
//        }
//
//    }
//
//    private class BtDisconnectReceiver extends BroadcastReceiver {
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            // TODO Auto-generated method stub
//            isConnected = false;
//            //textView.setText("蓝牙断开连接");
//            //tv8.setText("Not Connected");
//            switchBtn.setChecked(false);
//            // 断开后重新连接
//            //new TryToConnect().start();
//            toConnect.start();
//            Toast.makeText(info.this, "蓝牙断开连接，重新连接中...",
//                    Toast.LENGTH_SHORT).show();
//        }
//    }
}
