package top.zhost.buletooth.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import top.zhost.buletooth.ClsUtils;
import top.zhost.buletooth.R;
import top.zhost.buletooth.adapter.DeviceListAdapter;
import top.zhost.buletooth.connection.WriteSnConnecion;
import top.zhost.buletooth.connection.WriteTimeConnecion;

public class CommandActivity extends Activity {



    private EditText codeET;

    private BluetoothAdapter bluetoothAdapter;
    private ArrayList<BluetoothDevice> list = new ArrayList();
    private DeviceListAdapter adapter;
    private ListView lv;

    private Context mContext;

    private BluetoothDevice deviceTarget;

    private Handler mWriteMandler = new  Handler(){
        // 通过复写handlerMessage()从而确定更新UI的操作
        @Override
        public void handleMessage(Message msg) {
            String result = msg.obj+"";
            if("snok}".equals(result)){
                Toast. makeText(mContext, "sn写入成功", Toast.LENGTH_SHORT).show();
            }else if("on".equals(result) || "off".equals(result)){
                Toast. makeText(mContext, "时间同步成功"+result, Toast.LENGTH_SHORT).show();
            }else{
                Toast. makeText(mContext, result, Toast.LENGTH_SHORT).show();
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_command);
        mContext = this;

        codeET = findViewById(R.id.codeET);
        lv = findViewById(R.id.listView);
        adapter = new DeviceListAdapter(this,list);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothDevice bluetoothDevice = list.get(position);
                Toast.makeText(mContext, bluetoothDevice.getAddress()+"已选择", Toast.LENGTH_SHORT).show();
                deviceTarget = bluetoothDevice;
                if (bluetoothDevice.getBondState() != BluetoothDevice.BOND_BONDED) {

                    try {

                        ClsUtils.createBond(bluetoothDevice.getClass(), bluetoothDevice);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else{

                }
            }
        });

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // 找到设备的广播
        IntentFilter filter = new IntentFilter();
        filter.setPriority(1000);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        // 注册广播
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!bluetoothAdapter.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                //Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                codeET.setText(result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    public void goScanCodeClick(View view){
        new IntentIntegrator(this)
                .setCaptureActivity(CustomCaptureActivity.class)
                .setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)// 扫码的类型,可选：一维码，二维码，一/二维码
                //.setPrompt("请对准二维码")// 设置提示语
                .setCameraId(0)// 选择摄像头,可使用前置或者后置
                .setBeepEnabled(true)// 是否开启声音,扫完码之后会"哔"的一声
                .initiateScan();// 初始化扫码

    }

    public void searchClick(View view){
        setProgressBarIndeterminateVisibility(true);
        setTitle("正在搜索...");
        if (!bluetoothAdapter.isEnabled()){
            Toast.makeText(this, "failed", Toast.LENGTH_SHORT).show();
            return;
        }

        if (bluetoothAdapter.isDiscovering()){
            bluetoothAdapter.cancelDiscovery();//停止搜索
            return;
        }
        Toast.makeText(mContext, "开始搜索约12秒，再次点击可立即停止", Toast.LENGTH_SHORT).show();
        list.clear();
        bluetoothAdapter.startDiscovery();

    }
    public void writeClick(View view){
        if(deviceTarget == null){
            Toast.makeText(mContext, "请搜索后选择一台设备", Toast.LENGTH_SHORT).show();
            return;
        }
        String sn = codeET.getText().toString();

        WriteSnConnecion writeSnConnecion = new WriteSnConnecion(deviceTarget,sn,mWriteMandler);
        writeSnConnecion.start();
    }


    public void timeClick(View view){
        if(deviceTarget == null){
            Toast.makeText(mContext, "请搜索后选择一台设备", Toast.LENGTH_SHORT).show();
            return;
        }
        //String time = "2019-03-19 17:16:15";
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = sdf.format(d);
        WriteTimeConnecion timenConnecion = new WriteTimeConnecion(deviceTarget,time,mWriteMandler);
        timenConnecion.start();
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
                //if (device.getBondState() != BluetoothDevice.BOND_BONDED) {

                list.add(device);
                adapter.notifyDataSetChanged();
//                if("zzbule2".equals(device.getName())|| "00000001".equals(device.getName())){
//                    try {
//                        bluetoothAdapter.cancelDiscovery();
//                        //ClsUtils.createBond(device.getClass(), device);
//                    } catch (Exception e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                    }
//                }

                //}
            }else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //Toast.makeText(mContext, "搜索完成！", Toast.LENGTH_SHORT).show();
                setTitle("搜索完成！");
            }
//            else if(action.equals("android.bluetooth.device.action.PAIRING_REQUEST")){
//                BluetoothDevice mBluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                setTitle("PAIRING_REQUEST！");
//                abortBroadcast();
//                try {
//                    boolean ret = ClsUtils.autoBond(mBluetoothDevice.getClass(), mBluetoothDevice, "0000");
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
        }
    };
}
