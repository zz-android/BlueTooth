package top.zhost.buletooth.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.ruijie.commonlib.util.HttpUtils;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import top.zhost.buletooth.ClsUtils;
import top.zhost.buletooth.R;
import top.zhost.buletooth.adapter.DeviceListAdapter;
import top.zhost.buletooth.config.GlobalConfig;
import top.zhost.buletooth.connection.UpdateConnecion;

public class UpdateBinActivity extends Activity {

    private TextView newVersionTV,versionDownloadTV,updateBatchTV;

    private Button updateBtn,updateBatchBtn;
    private BluetoothAdapter bluetoothAdapter;
    private ArrayList<BluetoothDevice> list = new ArrayList();
    private DeviceListAdapter adapter;
    private ListView lv;

    private Context mContext;
    private BluetoothDevice deviceTarget;
    private byte[] bufferFile = null;

    private Handler mUpdateMandler = new  Handler(){
        // 通过复写handlerMessage()从而确定更新UI的操作
        @Override
        public void handleMessage(Message msg) {

            String result = msg.obj+"";
            if("bok}".equals(result)){
                Toast. makeText(mContext, "设备正在升级", Toast.LENGTH_SHORT).show();
                setTitle("设备正在升级");
            }else if("dok}".equals(result)){
                Toast. makeText(mContext, "设备升级成功", Toast.LENGTH_SHORT).show();
                setTitle("设备升级成功");
            }else{
                Toast. makeText(mContext, result, Toast.LENGTH_SHORT).show();
            }
        }
    };

    private Handler mUpdateBatchMandler = new  Handler(){
        // 通过复写handlerMessage()从而确定更新UI的操作
        @Override
        public void handleMessage(Message msg) {

            String result = msg.obj+"";
            //Toast. makeText(mContext, result, Toast.LENGTH_SHORT).show();
            updateBatchTV.setText(result);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_bin);
        mContext = this;

        newVersionTV = findViewById(R.id.newVersionTV);
        versionDownloadTV = findViewById(R.id.versionDownloadTV);
        updateBatchTV = findViewById(R.id.updateBatchTV);
        updateBtn = findViewById(R.id.updateBtn);
        updateBatchBtn = findViewById(R.id.updateBatchBtn);

        lv = findViewById(R.id.listView);
        adapter = new DeviceListAdapter(this,list);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothDevice bluetoothDevice = list.get(position);
                Toast.makeText(mContext, bluetoothDevice.getAddress(), Toast.LENGTH_SHORT).show();
                deviceTarget = bluetoothDevice;
                if (bluetoothDevice.getBondState() != BluetoothDevice.BOND_BONDED) {

                    //if ("zzbule2".equals(bluetoothDevice.getName()) || "00000001".equals(bluetoothDevice.getName())) {
                    try {

                        ClsUtils.createBond(bluetoothDevice.getClass(), bluetoothDevice);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //}
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
        VersionAsyncTask versionAsyncTask = new VersionAsyncTask();
        versionAsyncTask.execute();

        FileAsyncTask fileAsyncTask = new FileAsyncTask();
        fileAsyncTask.execute();
        if (!bluetoothAdapter.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
        }

    }


    class VersionAsyncTask extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                //String getContent = HttpUtils.sendGet(Resources.VERSION_URL, "");
                String url = GlobalConfig.DOWNLOAD_XML_URL;
                String getContent = HttpUtils.sendGet(url, "");
                Document dom = null;
                dom=DocumentHelper.parseText(getContent);
                Element root=dom.getRootElement();
                String version=root.element("binVersion").getText();
                return version;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        @Override
        protected void onPostExecute(String result) {
            if(result == null){
                newVersionTV.setText("最新版本 -");
            }else{
                newVersionTV.setText("最新版本 "+result);
            }

        }


    }

    class FileAsyncTask extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            updateBatchBtn.setEnabled(false);
            updateBtn.setEnabled(false);
        }

        @Override
        protected String doInBackground(String... params) {
            try {

                String mSavePath = Environment.getExternalStorageDirectory() + "/" + "download";
                File file = new File(mSavePath);
                if (!file.exists()) {
                    file.mkdir();
                }
                File binFile = new File(mSavePath,"rfid.bin");
                FileOutputStream fos = new FileOutputStream(binFile);

                URL url = new URL(GlobalConfig.DOWNLOAD_BIN_URL);
                // 创建连接
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(6000);
                conn.setReadTimeout(6000);
                conn.connect();
                // 获取文件大小
                int length = conn.getContentLength();
                // 创建输入流
                InputStream is = conn.getInputStream();
                // 缓存
                byte buf[] = new byte[512];
                int numread = 0;
                while((numread = is.read(buf))>0){
                    fos.write(buf, 0, numread);

                }
                return "";
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        @Override
        protected void onPostExecute(String result) {
            if(result == null){
                versionDownloadTV.setText("下载失败");
            }else{
                versionDownloadTV.setText("下载成功");
                updateBatchBtn.setEnabled(true);
                updateBtn.setEnabled(true);
            }

        }
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
        dealFile();
        UpdateConnecion updateConnecion = new UpdateConnecion(deviceTarget,bufferFile,mUpdateMandler);
        updateConnecion.start();
    }

    int autoUpdateNum = 0;
    public void writeBatchClick(View view){
        if(list.size() ==0){
            Toast.makeText(mContext, "请先搜索设备", Toast.LENGTH_SHORT).show();
            return;
        }
        dealFile();
        BatchUpdateTask BatchUpdateTask = new BatchUpdateTask();
        BatchUpdateTask.execute();

    }
    class BatchUpdateTask extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(String... params) {
            for(BluetoothDevice device : list){
                if(device.getName()==null || !device.getName().startsWith("JY")){
                    continue;
                }
                //updateBatchTV.setText("正在升级"+device.getName());
                Message msg = Message.obtain(); // 实例化消息对象
                msg.obj = "正在升级"+device.getName(); // 消息内容存放
                mUpdateBatchMandler.sendMessage(msg);

                try{
                    UpdateConnecion updateConnecion = new UpdateConnecion(device,bufferFile,mUpdateMandler);
                    updateConnecion.start();
                    updateConnecion.join();
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
            return "";
        }
        @Override
        protected void onPostExecute(String result) {
            updateBatchTV.setText("批量自动升级完成");

        }
    }
    //class BatchThread extends Thread()


    private void dealFile(){
        if(bufferFile != null){
            return;
        }
        try{
            File file = new File(Environment.getExternalStorageDirectory() + "/download/rfid.bin");
            long fileSize = file.length();
            if (fileSize > Integer.MAX_VALUE) {
                Toast.makeText(mContext, "file too big...", Toast.LENGTH_SHORT).show();
                return;
            }
            FileInputStream fi = new FileInputStream(file);
            bufferFile = new byte[(int) fileSize];
            int offset = 0;
            int numRead = 0;
            while (offset < bufferFile.length
                    && (numRead = fi.read(bufferFile, offset, bufferFile.length - offset)) >= 0) {
                offset += numRead;
            }
            // 确保所有数据均被读取
            if (offset != bufferFile.length) {
                throw new IOException("Could not completely read file " + file.getName());
            }
            fi.close();
        }catch (Exception e){
            e.printStackTrace();
        }
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
                if (device.getBondState() != BluetoothDevice.BOND_BONDED
                        && device.getName()!=null && device.getName().startsWith("JY")) {

                    try {

                        ClsUtils.createBond(device.getClass(), device);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                if(device.getName()!=null){
                    list.add(device);
                    adapter.notifyDataSetChanged();
                }



                //}
                // 搜索完成
            }else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

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
