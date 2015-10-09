package baidumapsdk.demo;

import android.nfc.Tag;
import android.util.Log;

import java.awt.font.TextAttribute;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Arrays;

/**
 * Created by gzy on 2015/8/30.
 */
public class Stm32Crc {
    public static final String TAG = "Stm32Crc";

    public static int CRC;

    public Stm32Crc(){
        CRC = 0xFFFFFFFF;
    }

    public void reset(){
        CRC = 0xFFFFFFFF;
    }

    public int getCRC(){
        return CRC;
    }

    public static int updateByteArray(byte byteArray[], int count) throws Exception {
        if(count%4 != 0){
            throw new Exception("count must be multiple of 4");
        }
        IntBuffer intBuf =
                ByteBuffer.wrap(byteArray, 0, count)
                        .order(ByteOrder.LITTLE_ENDIAN)
                        .asIntBuffer();
        int len = intBuf.remaining();
        int[] array = new int[len];
        intBuf.get(array);
        return updateIntArray(array);
    }

    public static byte[] addCRC(byte[] Pack){
        byte[] crc = getCRC(Pack,88);
        System.arraycopy(crc,0,Pack,88,crc.length);
        return Pack;
    }

    public static byte[] getCRC(byte[] Pack,int count){
        int crc = 0;
        try{
            crc = updateByteArray(Pack,count);
        }catch (Exception e){
            e.printStackTrace();
        }
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(crc).array();
    }

    public static int updateIntArray(int ptr[])
    {
        int crc = 0xFFFFFFFF;
        final int dwPolynomial = 0x04c11db7;
        int i = 0;
        int    xbit;
        int    data;
        int    len = ptr.length;
        while (len-- > 0) {
            xbit = 1 << 31;

            data = ptr[i++];
            for (int bits = 0; bits < 32; bits++) {
                if ((crc & 0x80000000) != 0){
                    crc <<= 1;
                    crc ^= dwPolynomial;
                }
                else
                    crc <<= 1;
                if ((data & xbit) != 0)
                    crc ^= dwPolynomial;

                xbit >>>= 1;
            }
        }
        return crc;
    }

    public static boolean CRCVerify(byte[] buffer){
        byte[] receivedCRC = new byte[4];
        byte[] data = new byte[88];
        System.arraycopy(buffer,88,receivedCRC,0,4);
        System.arraycopy(buffer,0,data,0,88);
        byte[] calculatedCRC = getCRC(data,88);
        Log.v(TAG,"received crc :"+Arrays.toString(receivedCRC));
        Log.v(TAG,"calculated crc :"+Arrays.toString(calculatedCRC));
        if(Arrays.equals(receivedCRC,calculatedCRC)){
            return true;
        }else{
            return false;
        }
    }
}
