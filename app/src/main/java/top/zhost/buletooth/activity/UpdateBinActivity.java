package top.zhost.buletooth.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.ruijie.commonlib.util.HttpUtils;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import top.zhost.buletooth.R;
import top.zhost.buletooth.config.GlobalConfig;

public class UpdateBinActivity extends Activity {

    private TextView newVersionTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_bin);

        newVersionTV = findViewById(R.id.newVersionTV);
        VersionAsyncTask versionAsyncTask = new VersionAsyncTask();
        versionAsyncTask.execute();

        FileAsyncTask fileAsyncTask = new FileAsyncTask();
        fileAsyncTask.execute();
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
                newVersionTV.setText("最新版本");
            }else{
                newVersionTV.setText("最新版本 "+result);
            }

        }


    }

    class FileAsyncTask extends AsyncTask<String, Integer, String> {
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
                newVersionTV.setText("最新版本");
            }else{
                newVersionTV.setText("最新版本 "+result);
            }

        }
    }
}
