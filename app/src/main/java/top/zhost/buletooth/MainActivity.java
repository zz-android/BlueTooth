package top.zhost.buletooth;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import top.zhost.buletooth.adapter.DeviceListAdapter;

public class MainActivity extends Activity {

    //private Button On,Off,Visible,list;
    private ArrayList<BluetoothDevice> list = new ArrayList();
    private DeviceListAdapter adapter;
    private BluetoothAdapter bluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices;
    private BluetoothDevice deviceTarget;

    private BluetoothConnecion bluetoothConnecion;
    private ListView lv;

    private Context mContext;

    String searchName = "zzbule2";
    //String searchName = "00000001";

    private Handler mhandler = new  Handler(){
        // 通过复写handlerMessage()从而确定更新UI的操作
        @Override
        public void handleMessage(Message msg) {

            Toast. makeText(mContext, msg.obj+"", Toast.LENGTH_SHORT).show();
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
//        On = findViewById(R.id.button1);
//        Off = findViewById(R.id.button2);
//        Visible = findViewById(R.id.button3);
//        list = findViewById(R.id.button4);

        lv = findViewById(R.id.listView1);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        String address = bluetoothAdapter.getAddress();
        String name = bluetoothAdapter.getName();
        String toastText = name + " :" + address;
        Toast. makeText(this, toastText, Toast.LENGTH_SHORT).show();

        // 找到设备的广播
        IntentFilter filter = new IntentFilter();

        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        // 注册广播
        registerReceiver(receiver, filter);

        //adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, list);
        adapter = new DeviceListAdapter(mContext,list);
        lv.setAdapter(adapter);


        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothDevice bluetoothDevice = list.get(position);
                Toast.makeText(MainActivity.this, bluetoothDevice.getAddress(), Toast.LENGTH_SHORT).show();
                deviceTarget = bluetoothDevice;
            }
        });
    }

    public void on(View view){
        if (!bluetoothAdapter.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
            Toast.makeText(getApplicationContext(),"Turned on",Toast.LENGTH_SHORT).show();
        } else{
            Toast.makeText(getApplicationContext(),"Already on", Toast.LENGTH_SHORT).show();
        }
    }
    public void list(View view){
        pairedDevices = bluetoothAdapter.getBondedDevices();


        for(BluetoothDevice bt : pairedDevices) {
            list.add(bt);

//            if(searchName.equals(bt.getName())){
//                deviceTarget = bt;
//                Toast.makeText(MainActivity.this, searchName, Toast.LENGTH_SHORT).show();
//            }
        }

        adapter.notifyDataSetChanged();


    }

    public void connect(View view){
        if(deviceTarget == null){
            Toast.makeText(MainActivity.this, "deviceTarget == null", Toast.LENGTH_SHORT).show();
            return;
        }
        bluetoothConnecion = new BluetoothConnecion(deviceTarget);
        bluetoothConnecion.setMhandler(mhandler);
        bluetoothConnecion.start();
    }

    public void off(View view){
        bluetoothAdapter.disable();
        Toast.makeText(getApplicationContext(),"Turned off" , Toast.LENGTH_SHORT).show();
    }
    public void visible(View view){
//        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
//        startActivity(discoverableIntent);
        //设置进度条
        setProgressBarIndeterminateVisibility(true);
        setTitle("正在搜索...");
        if (!bluetoothAdapter.isEnabled()){
            Toast.makeText(MainActivity.this, "failed", Toast.LENGTH_SHORT).show();
        }

        if (bluetoothAdapter.isDiscovering()){
            bluetoothAdapter.cancelDiscovery();//停止搜索
        }
        bluetoothAdapter.startDiscovery();


    }

    public void write(View view){



        if(bluetoothConnecion != null){
            String send = "ota begin}";
            byte[] btyes = send.getBytes();
            bluetoothConnecion.write(btyes);
//            bluetoothConnecion.cancel();
//            bluetoothConnecion.write("version}".getBytes());
        }
    }

    public void server(View view){
//        Intent disCoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//        disCoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 150);
//        startActivity(disCoverableIntent);
        Thread listenThread = new Thread(new Runnable() {
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
            String serverName = "BTServer" ;

            BluetoothServerSocket bluetoothServer = null;
            @Override
            public void run() {
                // TODO Auto-generated method stub
                try {
                    bluetoothServer = bluetoothAdapter.listenUsingRfcommWithServiceRecord(serverName, uuid);
                    while (true){
                        BluetoothSocket serverSocket = bluetoothServer.accept();
                        Message msg = Message.obtain(); // 实例化消息对象
                        msg.obj = "socket连接建立"; // 消息内容存放
                        mhandler.sendMessage(msg);
                        myHandleConnectionWiht(serverSocket);
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                    Message msg = Message.obtain(); // 实例化消息对象
                    msg.obj = e.getMessage(); // 消息内容存放
                    mhandler.sendMessage(msg);

                }
            }

            private void myHandleConnectionWiht(BluetoothSocket serverSocket) {
                //Toast.makeText(MainActivity.this, "myHandleConnectionWiht", Toast.LENGTH_SHORT).show();
                try {
                    InputStream tmpIn = serverSocket.getInputStream();
                    byte buffer[] = new byte[1024];
                    while (true) {
                        int len = tmpIn.read(buffer);// 输入流读取数据
                        if (len > 0) {
                            String a = new String(buffer);
                            System.out.print(a);

                            Message msg = Message.obtain(); // 实例化消息对象
                            msg.obj = a; // 消息内容存放
                            mhandler.sendMessage(msg);

                            OutputStream tmpOut = serverSocket.getOutputStream();
                            tmpOut.write("bok}".getBytes());
                            tmpOut.flush();

                        } else {
                            // 抛出读取数据异常(小米手机不会运行到这里；三星5.0系统手机，会在read后返回-1，然后运行到这里)
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Message msg = Message.obtain(); // 实例化消息对象
                    msg.obj = e.getMessage(); // 消息内容存放
                    mhandler.sendMessage(msg);
                    //Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                }

            }

        });
        listenThread.start();
        Toast.makeText(MainActivity.this, "开始监听", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    // 广播接收器
    private final BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // 收到的广播类型
            String action = intent.getAction();
            // 发现设备的广播
            if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                // 从intent中获取设备
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // 判断是否配对过
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    // 添加到列表
                    //tvDevices.append(device.getName() + ":" + device.getAddress() + "\n");
                    list.add(device);
                    adapter.notifyDataSetChanged();
                    //String searchName = "00000001";//"zzbule2"
//                    if(searchName.equals(device.getName())){
//                        deviceTarget = device;
//                        Toast.makeText(MainActivity.this, searchName, Toast.LENGTH_SHORT).show();
//                        bluetoothAdapter.cancelDiscovery();
//                    }
                }
                // 搜索完成
            }else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED
                    .equals(action)) {
                //关闭进度条
                setProgressBarIndeterminateVisibility(true);
                setTitle("搜索完成！");
            }else if(action.equals("android.bluetooth.device.action.PAIRING_REQUEST")){
                setTitle("PAIRING_REQUEST！");
            }
        }
    };
}
