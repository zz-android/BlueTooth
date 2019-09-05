package top.zhost.buletooth.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.ruijie.commonlib.util.HttpUtils;
import com.ruijie.commonlib.version.DownloadSender;
import com.ruijie.commonlib.version.UpdateManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import top.zhost.buletooth.R;
import top.zhost.buletooth.config.GlobalConfig;

public class UpdateApkActivity extends Activity {
    private TextView nowVersionTV;
    private TextView newVersionTV;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_apk);

        nowVersionTV = findViewById(R.id.nowVersionTV);
        newVersionTV = findViewById(R.id.newVersionTV);

        String nowVersion = "";
        try {
            nowVersion=this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionCode + "";
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        nowVersionTV.setText("当前版本 "+nowVersion);
        newVersionTV.setText("最新版本");
        VersionAsyncTask versionAsyncTask = new VersionAsyncTask();
        versionAsyncTask.execute();


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= 23) {
            int REQUEST_CODE_CONTACT = 101;
            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            //验证是否许可权限
            for (String str : permissions) {
                if (this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    //申请权限
                    this.requestPermissions(permissions, REQUEST_CODE_CONTACT);
                    return;
                }
            }
        }
    }

    public void updateVersionClick(View v) {
        //Resources.updateManager = new UpdateManager(this);
        //new DownloadSender(this).execute(Resources.VERSION_URL);
        UpdateManager.instance = new UpdateManager(this);
        UpdateManager.APK_URL = GlobalConfig.DOWNLOAD_APK_URL;
        UpdateManager.XML_NAME = "rfid.xml";
        new DownloadSender(this).execute(GlobalConfig.DOWNLOAD_XML_URL);
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
                String version=root.element("version").getText();
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



}
