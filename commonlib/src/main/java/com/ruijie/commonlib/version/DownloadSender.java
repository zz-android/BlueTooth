package com.ruijie.commonlib.version;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;

import com.ruijie.commonlib.util.Toast;

import java.io.File;

//import com.ruijie.eduregister.util.Toast;

public class DownloadSender extends AsyncTask<String, Integer, File> {
	Context context;
	public DownloadSender(Context context){
		this.context = context;
		
    }
    @Override
    protected File doInBackground(String... params) {
    	File tmp = new File(Environment.getExternalStorageDirectory().toString()+"/"+UpdateManager.XML_NAME);
    	if (tmp.exists())
    		tmp.delete();
       	return HttpDownloader.getInstance().download(params[0]);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

    @Override
    protected void onPostExecute(File result) {
        if(result == null){
            Toast.show(context, "获取更新文件失败，请检查网络或文件权限。",Toast.LENGTH_SHORT);
        }else{
            UpdateManager.instance.checkUpdate(result);
        }

    }

    @Override
    protected void onPreExecute() {
        // 任务启动，可以在这里显示一个对话框，这里简单处理
    	//Toast.show(context, "正在检查更新文件",Toast.LENGTH_SHORT);

    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        // 更新进度
        System.out.println(""+values[0]);
    }

}

