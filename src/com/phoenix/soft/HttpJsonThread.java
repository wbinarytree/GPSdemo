package com.phoenix.soft;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.os.Handler;
import android.os.Message;
import android.util.Log;


/**
 * http取JSON字符串线程
 * @author Phoenix WANG
 * 
 */
public class HttpJsonThread implements Runnable {
	
	private Handler handler;
	private int id;
	private String url;
	String result;

	
	public HttpJsonThread(Handler handler,int id,String url){
		this.handler = handler;
		this.id = id;
		this.url = url;
	}
	


	@Override
	public void run() {

		// TODO Auto-generated method stub
		Message msg = handler.obtainMessage();
		result = httpGetRequest(url);
		msg.what = id;
		msg.obj = result;
		handler.sendMessage(msg);
		Log.d("HTTP RES", "============= HTTP THREAD SUCCESS =============");
		
	}
	/**
	 * 从HTTP数据
	 * @return
	 */
	
	private String httpGetRequest(String url) {
			
			String mResultData = "";

			
			try {
				HttpClient httpClient = new DefaultHttpClient();
				HttpGet httpGet = new HttpGet(url);
				HttpResponse httpResponse = httpClient.execute(httpGet);
				HttpEntity entity=httpResponse.getEntity();
				if (httpResponse.getStatusLine().getStatusCode() == 200) {
					Log.d("HTTP RSP", "============= Http response OK! =============");
					mResultData = EntityUtils.toString(httpResponse.getEntity());
					if (entity != null) {
						entity.consumeContent();
						//释放资源，这一步非常重要
						}
				}
			} catch (Exception e) {
	
				Log.d("HTTP RSP",
						"============= Http response Failure! =============");
				e.printStackTrace();
			}
			
			
			Log.d("HTTP get VALUE",mResultData);
			return mResultData;
		}

	
}
