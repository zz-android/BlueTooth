package com.ruijie.commonlib.version;

import android.os.Environment;



import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class HttpDownloader{
	private static HttpDownloader httpDownloader = new HttpDownloader();
	public static HttpDownloader getInstance() {
		return httpDownloader;
	}
	private HttpDownloader() {
		
	}
	private URL url=null;
	 
	 /**
	  * 根据URL下载文本文件
	  */
	public File download(String urlStr){
		File tmp = new File(Environment.getExternalStorageDirectory().toString()+"/"+ UpdateManager.XML_NAME);
		String line = null;
		BufferedReader buffer = null;
		try {
			url = new URL(urlStr);
			HttpURLConnection urlConn = (HttpURLConnection)url.openConnection();
			urlConn.setConnectTimeout(5000);
			buffer = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
			BufferedWriter bos = new BufferedWriter(new FileWriter(tmp));
			while((line = buffer.readLine())!=null)
			{
				bos.write(line);
			}
			bos.flush();
			bos.close();
		} catch(Exception e){
			e.printStackTrace();
			return null;
		}finally{
			try{
				buffer.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return tmp;
	}
	
}
