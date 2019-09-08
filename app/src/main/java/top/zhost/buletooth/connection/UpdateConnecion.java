package top.zhost.buletooth.connection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;

import java.io.DataInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 读写蓝牙设备
 *
 * @author hbbliyong
 *
 */
public class UpdateConnecion extends Thread {

    private Handler mhandler;
    byte[] bufferFile;
    private  BluetoothSocket mSocket;
    private  InputStream mInStream;
    private  OutputStream mOutStream;
    BluetoothDevice device;
    byte[] buffer;
    private  BluetoothAdapter mAdapter;
    // 用于本应用程序唯一的UUID，
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public UpdateConnecion(BluetoothDevice device, byte[] bufferFile, Handler mhandler) {
        this.device = device;
        this.bufferFile = bufferFile;
        this.mhandler = mhandler;

    }
    public void run() {

        mAdapter = BluetoothAdapter.getDefaultAdapter();
        // 始终取消发现，因为它会降低连接的速度
        mAdapter.cancelDiscovery();

        BluetoothSocket bluetoothSockettmp = null;
        // 获得用于指定蓝牙连接的BluetoothSocket
        try {

            bluetoothSockettmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            //bluetoothSockettmp = device.createRfcommSocketToServiceRecord(uuid);
            //bluetoothSockettmp =(BluetoothSocket) device.getClass().getMethod("createRfcommSocketToServiceRecord", new Class[] {int.class}).invoke(device,MY_UUID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mSocket = bluetoothSockettmp;



        // 建立到BluetoothSocket的连接
        try {
            // 这是一个阻塞调用，只在成功连接或者异常时返回
            mSocket.connect();
            //mSocket.getOutputStream().write("zbc".getBytes());
            mInStream = mSocket.getInputStream();
            mOutStream = mSocket.getOutputStream();
            buffer = new byte[1024];

            String snCommand = "ota begin}";
            mOutStream.write(snCommand.getBytes());
            String readStrB = readString(mInStream);
            Message msg = Message.obtain(); // 实例化消息对象
            msg.obj = readStrB; // 消息内容存放
            mhandler.sendMessage(msg);
            if(!"bok}".equals(readStrB)){
                return;
            }
            writeFile();


            mOutStream.write("ota done}".getBytes());
//            byte[] resultByte = new byte[64];
//            mInStream.read(resultByte);
//            String resultStr = new String(resultByte);
            String resultStr = readString(mInStream);
            Message msg2 = Message.obtain(); // 实例化消息对象
            msg2.obj = resultStr; // 消息内容存放
            mhandler.sendMessage(msg2);




        } catch (Exception e) {
            e.printStackTrace();
            Message msg = Message.obtain(); // 实例化消息对象
            msg.obj = e.getMessage(); // 消息内容存放
            mhandler.sendMessage(msg);
            try {
                mSocket.close();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }finally {
            try {
                mSocket.close();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }

    }

    private String readString(InputStream bis) throws Exception{

        DataInputStream dis = new DataInputStream(bis);
        byte[] bytes = new byte[1]; // 一次读取一个byte
        String ret = "";
        while (dis.read(bytes) != -1) {
            ret += new String(bytes);
            if (dis.available() == 0) { //一个请求
                return ret;
            }
        }
        return ret;

    }
    private void writeFile() throws Exception{
        //mOutStream.write(bufferFile);
        List<byte[]> subAryList = new ArrayList<byte[]>();
        int block = (bufferFile.length/500)+1;
        for (int i = 0; i < block; i++) {
            if((i+1)==block){
                byte[] sub =java.util.Arrays.copyOfRange(bufferFile,i*500,bufferFile.length);
                subAryList.add(sub);
            }else{
                byte[] sub =java.util.Arrays.copyOfRange(bufferFile,i*500,i*500+500);
                subAryList.add(sub);
            }
        }
        for(byte[] subByte : subAryList){
            mOutStream.write(subByte);
            String readStrB = readString(mInStream);
            if(!"wok}".equals(readStrB)){
                mSocket.close();
            }
        }
    }
}