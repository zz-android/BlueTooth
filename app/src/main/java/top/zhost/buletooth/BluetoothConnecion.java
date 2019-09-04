package top.zhost.buletooth;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.renderscript.ScriptIntrinsicYuvToRGB;

/**
 * 读写蓝牙设备
 *
 * @author hbbliyong
 *
 */
public class BluetoothConnecion extends Thread {

    private Handler mhandler;
    private  BluetoothSocket mSocket;
    private  InputStream mInStream;
    private  OutputStream mOutStream;
    BluetoothDevice device;
    byte[] buffer;
    private  BluetoothAdapter mAdapter;
    // 用于本应用程序唯一的UUID，
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    public BluetoothConnecion(BluetoothDevice device) {
        this.device = device;

    }

    public void run() {
        BluetoothSocket bluetoothSockettmp = null;
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        // 获得用于指定蓝牙连接的BluetoothSocket
        try {
            bluetoothSockettmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            //bluetoothSockettmp = device.createRfcommSocketToServiceRecord(uuid);
            //bluetoothSockettmp =(BluetoothSocket) device.getClass().getMethod("createRfcommSocketToServiceRecord", new Class[] {int.class}).invoke(device,MY_UUID);

        } catch (Exception e) {
            e.printStackTrace();
        }
        mSocket = bluetoothSockettmp;
        // TODO Auto-generated method stub
        // 始终取消发现，因为它会降低连接的速度
        mAdapter.cancelDiscovery();

        // 建立到BluetoothSocket的连接
        try {
            // 这是一个阻塞调用，只在成功连接或者异常时返回
            mSocket.connect();
            //mSocket.getOutputStream().write("zbc".getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            // 设备连接失败，关闭套接字
            try {
                mSocket.close();
            } catch (Exception e2) {
                // TODO: handle exception
                e2.printStackTrace();
            }
        }
        boolean isConnected = mSocket.isConnected();
        System.out.print("isConnected "+isConnected);

        Message msg = Message.obtain(); // 实例化消息对象
        msg.obj = "isConnected "+isConnected; // 消息内容存放
        mhandler.sendMessage(msg);



        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // 获得BluetoothSoket输入输出流
        try {
            tmpIn = mSocket.getInputStream();
            tmpOut = mSocket.getOutputStream();
            buffer = new byte[1024];
        } catch (Exception e) {
            e.printStackTrace();
        }
        mInStream = tmpIn;
        mOutStream = tmpOut;

        // 在新线程中建立套接字连接，避免FC

        Thread connectionThread = new Thread(new Runnable() {
            @Override
            public void run() {
                // 连接时保持监听InputStream
                while (true) {
                    try {
                        // 从套接字流读取数据
                        byte[] bufferRead = new byte[1024];
                        mInStream.read(bufferRead);
                        String readStr = new String(bufferRead);
                        System.out.print(readStr);
                        if(mhandler != null){
                            Message msg = Message.obtain(); // 实例化消息对象
                            msg.obj = readStr; // 消息内容存放
                            mhandler.sendMessage(msg);
                        }
                        // 向UI Activity发送获取的数据
                    } catch (Exception e) {
                        // TODO: handle exception
                        Message msg = Message.obtain(); // 实例化消息对象
                        msg.obj = e.getMessage(); // 消息内容存放
                        mhandler.sendMessage(msg);
                        break;
                    }
                }
            }
        });
        connectionThread.start();

    }

    public void write(byte[] buffer)
    {
        try {
            mOutStream.write(buffer);

//            byte[] bufferRead = new byte[1024];
//            mInStream.read(bufferRead);
//            String readStr = new String(bufferRead);
//            System.out.print(readStr);
//            if(this.mhandler != null){
//                Message msg = Message.obtain(); // 实例化消息对象
//                msg.obj = readStr; // 消息内容存放
//                mhandler.sendMessage(msg);
//            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cancel()
    {
        try {
            mSocket.close();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    public void setMhandler(Handler mhandler) {
        this.mhandler = mhandler;
    }
}