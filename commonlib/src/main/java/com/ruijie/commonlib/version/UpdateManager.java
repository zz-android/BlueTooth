package com.ruijie.commonlib.version;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ruijie.commonlib.R;
import com.ruijie.commonlib.application.RootApplication;
import com.ruijie.commonlib.util.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
//import com.example.teacherhelper.application.RootApplication;

/** 
 *@author coolszy 
 *@date 2012-4-26 
 *@blog http://blog.92coding.com
 *@modify chjie
 *@date 2013-12 
 */  
  
public class UpdateManager  
{
    public static boolean isUpdate = false;
    public static UpdateManager instance = null;
    public static String APK_URL = null;
    public static String XML_URL = null;
    public static String XML_NAME = null;
    public static String APK_NAME = null;

    /* 下载中 */  
    private static final int DOWNLOAD = 1;  
    /* 下载结束 */  
    private static final int DOWNLOAD_FINISH = 2;  
    /* 保存解析的XML信息 */  
    HashMap<String, String> mHashMap;  
    /* 下载保存路径 */  
    private String mSavePath;  
    /* 记录进度条数量 */  
    private int progress;  
    /* 是否取消更新 */  
    private boolean cancelUpdate = false;
    /* 是否等待取消确认 */
    private boolean isWaitForCancel = false;

    private Context mContext;  
    /* 更新进度条 */  
    private ProgressBar mProgress;  
    private Dialog mDownloadDialog;  
    private TextView ProgressText;
    
    private Handler mHandler = new Handler()  
    {  
        public void handleMessage(Message msg)  
        {  
            switch (msg.what)  
            {  
            // 正在下载  
            case DOWNLOAD:  
                // 设置进度条位置  
                mProgress.setProgress(progress);  
                ProgressText.setText(progress+"%");
                break;  
            case DOWNLOAD_FINISH:  
                // 安装文件并退出原来的程序
                if(cancelUpdate){
                    break;
                }
            	isUpdate = true;
                try {
                    installApk();
                    RootApplication.getInstance().exit();
                    //Toast.show(mContext, "安装。",Toast.LENGTH_SHORT);
                    //System.exit(0);
                    //((RootApplication)mContext.getApplicationContext()).exit();
                } catch (Exception e) {
                    Toast.show(mContext, "安装错误。",Toast.LENGTH_SHORT);
                }

                //退出
                break;
            default: 	//异常退出 
            	isUpdate = false;
            	Toast.show(mContext, "不合法的下载网址",Toast.LENGTH_SHORT);
            	onFinishTask();
                break;  
            }  
        };  
    };  
  
    public UpdateManager(Context context)  
    {  
        this.mContext = context;  
    }  
  
    /** 
     * 检测软件更新 
     */  
    public void checkUpdate(File xmlFile)  
    {  
        if (isUpdate(xmlFile))  
        {  
            // 显示提示对话框  
            showNoticeDialog();  
        } else {  
        	isUpdate = true;
        	Toast.show(mContext, mContext.getString(R.string.soft_update_no),Toast.LENGTH_SHORT);
            onFinishTask();
        }  
    }  
  
    /**
     * 检查软件是否有更新版本 
     * @param xmlFile 已下载的version.xml
     * @return
     */
    private boolean isUpdate(File xmlFile)  
    {  
        // 获取当前软件版本  
        int versionCode = getVersionCode(mContext);  
        // 把version.xml放到网络上，然后获取文件信息  
        //InputStream inStream = ParseXmlService.class.getClassLoader().getResourceAsStream("version.xml");  
        // 解析XML文件。 由于XML文件比较小，因此使用DOM方式进行解析  
        ParseXmlService service = new ParseXmlService();  
        try {  
            mHashMap = service.parseXml(xmlFile);  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        if (null != mHashMap) {  
            int serviceCode = Integer.valueOf(mHashMap.get("version"));
            // 版本判断  
            if (serviceCode > versionCode) {  
                return true;  
            }  
        }  
        return false;  
    }  
  
	/** 
	 * 获取软件版本号 
	 *  
	 * @param context 
	 * @return 
	 */  
	private int getVersionCode(Context context)  
	{  
	    int versionCode = 0;  
	    try {  
	        // 获取软件版本号，对应AndroidManifest.xml下android:versionCode  
	        versionCode = context.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionCode;  
	    } catch (NameNotFoundException e)  
	    {  
	        e.printStackTrace();  
	    }  
	    return versionCode;  
	}  
	  
    /** 
     * 显示软件更新对话框 
     */  
    private void showNoticeDialog()  
    {  
        // 构造对话框  
        Builder builder = new Builder(mContext);
        builder.setTitle(R.string.soft_update_title);  
        builder.setMessage(R.string.soft_update_info);  
        // 更新  
        builder.setPositiveButton(R.string.soft_update_updatebtn, new OnClickListener()  
        {  
            @Override  
            public void onClick(DialogInterface dialog, int which)  
            {  
                dialog.dismiss();  
                // 显示下载对话框  
                showDownloadDialog();  
            }  
        });  
        // 稍后更新  
        builder.setNegativeButton(R.string.soft_update_later, new OnClickListener()  
        {  
            @Override  
            public void onClick(DialogInterface dialog, int which)  
            {  
            	isUpdate = false;
                dialog.dismiss();  
                onFinishTask();
            }  
        });  
        Dialog noticeDialog = builder.create(); 
        noticeDialog.setCancelable(false);
        noticeDialog.show();  
    }  
  
    /** 
     * 显示软件下载对话框 
     */  
    private void showDownloadDialog()  
    {  
        // 构造软件下载对话框  
        Builder builder = new Builder(mContext);
        builder.setTitle(R.string.soft_updating);  
        // 给下载对话框增加进度条  
        final LayoutInflater inflater = LayoutInflater.from(mContext);  
        View v = inflater.inflate(R.layout.initprocess_dialog, null);  
        mProgress = (ProgressBar) v.findViewById(R.id.processbar);  
        ProgressText = (TextView) v.findViewById(R.id.processtext);
        builder.setView(v);  
        // 取消更新  
        builder.setNegativeButton(R.string.soft_update_cancel, new OnClickListener()  
        {  
            @Override  
            public void onClick(final DialogInterface dialog, int which)
            {  
//            	Resources.isUpdate = false;//取消更新
//                dialog.dismiss();
//                cancelUpdate = true;// 设置取消状态
//                onFinishTask();
                cancelUpdate = false;
                isWaitForCancel = true;
                // 构造对话框
                Builder builder = new Builder(mContext);
                builder.setTitle("取消更新");
                builder.setMessage("确认取消吗？");
                builder.setPositiveButton("是", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogCancel, int which) {
                        dialogCancel.dismiss();

                        isUpdate = false;//取消更新
                        dialog.dismiss();
                        cancelUpdate = true;// 设置取消状态
                        onFinishTask();
                        isWaitForCancel = false;
                    }
                });
                builder.setNegativeButton("否", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogCancel, int which) {
                        dialogCancel.dismiss();
                        mDownloadDialog.show();
                        cancelUpdate = false;
                        isWaitForCancel = false;
                    }
                });
                Dialog noticeDialog = builder.create();
                noticeDialog.setCancelable(false);
                noticeDialog.show();
            }  
        });  
        mDownloadDialog = builder.create();
        mDownloadDialog.setCancelable(false);
        mDownloadDialog.show();  
        // 下载文件  
        downloadApk();  
    }  
  
    /** 
     * 下载apk文件 
     */  
    private void downloadApk()  
    {  
        // 启动新线程下载软件  
        new downloadApkThread().start();  
    }  
  
    /** 
     * 下载文件线程 
     *  
     * @author coolszy 
     *@date 2012-4-26 
     *@blog http://blog.92coding.com 
     */  
    private class downloadApkThread extends Thread  
    {  
        @Override  
        public void run()  
        {  
            try  
            {  
                // 判断SD卡是否存在，并且是否具有读写权限  
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))  
                {  
                    // 获得存储卡的路径  
                    String sdpath = Environment.getExternalStorageDirectory() + "/";  
                    mSavePath = sdpath + "download";  
                    //URL url = new URL(Resources.APK_URL); zzzzAPK_URL
                    //URL url = new URL(Resources.getApkUrl().replace(Resources.SERVER_NAME+".apk",mHashMap.get("name")));
                    URL url = new URL(APK_URL);
                    // 创建连接  
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();  
                    conn.setConnectTimeout(6000);
                    conn.connect();  
                    // 获取文件大小  
                    int length = conn.getContentLength();  
                    // 创建输入流  
                    InputStream is = conn.getInputStream();  
  
                    File file = new File(mSavePath);  
                    // 判断文件目录是否存在  
                    if (!file.exists())  
                    {  
                        file.mkdir();  
                    }  
                    File apkFile = new File(mSavePath, mHashMap.get("name"));  
                    FileOutputStream fos = new FileOutputStream(apkFile);  
                    int count = 0;  
                    // 缓存  
                    byte buf[] = new byte[1024];  
                    // 写入到文件中  
                    do  
                    {  
                        int numread = is.read(buf);  
                        count += numread;  
                        // 计算进度条位置  
                        progress = (int) (((float) count / length) * 100);  
                        // 更新进度  
                        mHandler.sendEmptyMessage(DOWNLOAD);  
                        if (numread <= 0) {
                            // 下载完成
                            while (isWaitForCancel){
                                try {
                                    Thread.sleep(500);
                                    //Toast.show(mContext, "等待确认。",Toast.LENGTH_SHORT);
                                } catch (Exception e) {
                                    Toast.show(mContext, "等待确认异常。",Toast.LENGTH_SHORT);
                                }
                            }
                            mHandler.sendEmptyMessage(DOWNLOAD_FINISH);  
                            break;  
                        }  
                        // 写入文件  
                        fos.write(buf, 0, numread);  
                    } while (!cancelUpdate);// 点击取消就停止下载.  
                    fos.close();  
                    is.close(); 
                    mDownloadDialog.dismiss();
                    return;
                }  
                isUpdate = false;
            } catch (MalformedURLException e)  
            {  isUpdate = false;
                e.printStackTrace();  
            } catch (IOException e)  
            {  isUpdate = false;
                e.printStackTrace();  
            } catch (Exception e) {
            	e.printStackTrace();
            	isUpdate = false;
            }
            // 取消下载对话框显示  
            mDownloadDialog.dismiss();  
            mHandler.sendEmptyMessage(0);
        }  
    };  
  
    /** 
     * 安装APK文件 
     */  
    private void installApk()  
    {  
        File apkfile = new File(mSavePath, mHashMap.get("name"));  
        if (!apkfile.exists())  
        {  
            return;  
        }

        // 通过Intent安装APK文件
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri data;
        //判断是否是AndroidN以及更高的版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // 清单文件中配置的authorities
            data = FileProvider.getUriForFile(mContext, "top.zhost.bluetooth.fileprovider", apkfile);
            // 给目标应用一个临时授权
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }else{
            data = Uri.parse("file://" + apkfile.toString());
        }
        intent.setDataAndType(data, "application/vnd.android.package-archive");
        mContext.startActivity(intent);
    }  
    /**
     * 供调用者复写当下载结束后调用
     */
    protected void onFinishTask() {};
}
