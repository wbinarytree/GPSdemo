package com.phoenix.soft;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class MapActivity extends Activity {

	private Button btn_name;
	private Button btn_region;
	private Button btn_show;
	private Spinner spinner_province;
	private Spinner spinner_city;
	private Spinner spinner_county;
	private EditText et_name;
	private Dialog mdialog;
	private MyApplication myApplication;
	
	/**
	 * Thread Flag 
	 * 0xC1 省
	 * 0xC2 市
	 * 0xC3 县
	 * 0xC4 查询
	 */
	private String url_region = "http://www.wx45.com/json.php?mod=app&act=queryregion&session_id=";
	private String url_search_r = "http://www.wx45.com/json.php?mod=app&act=querywxlist&session_id=";
	private String url_search_n = "http://www.wx45.com/json.php?mod=app&act=searchwxshop&session_id=";
	private ArrayAdapter<CItem> adapter_origin;
	private Boolean show = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_map);
		Construct();
		
		spinner_province.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				String str = ((CItem)arg0.getItemAtPosition(arg2)).GetID();
				if(!str.equals("")){
				mdialog.show();
				spinner_county.setAdapter(adapter_origin);
				String temp = url_region + myApplication.getSessionID()
					    + "&username=" + myApplication.getUsername()
					    + "&region_id=" +str + "&region_type";
				Log.d("URL",temp);
				if(!myApplication.isNetConnected()){
					mdialog.cancel();
					ToastUtil.showToast(MapActivity.this, "网络未连接，请联网重试", Toast.LENGTH_SHORT);
					}else{
						Thread thread = new Thread(new HttpJsonThread(handler, 0xC2, temp));
						thread.start();
					}
				}
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
		});	
		spinner_city.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				String str = ((CItem)arg0.getItemAtPosition(arg2)).GetID();
				if(!str.equals("")){
					mdialog.show();
					String temp = url_region + myApplication.getSessionID()
						    + "&username=" + myApplication.getUsername()
						    + "&region_id=" +str + "&region_type";
					Log.d("URL",temp);
					if(!myApplication.isNetConnected()){
						mdialog.cancel();
						ToastUtil.showToast(MapActivity.this, "网络未连接，请联网重试", Toast.LENGTH_SHORT);
					}else{
						Thread thread = new Thread(new HttpJsonThread(handler, 0xC3, temp));
						thread.start();
					}
				}
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
		});	
		mdialog.show();
		String temp = url_region + myApplication.getSessionID()
				    + "&username=" + myApplication.getUsername()
				    + "&region_id=1&region_type";
		Log.d("URL",temp);
		if(!myApplication.isNetConnected()){
			mdialog.cancel();
			ToastUtil.showToast(MapActivity.this, "网络未连接，请联网重试", Toast.LENGTH_SHORT);
		}else{
			Thread thread = new Thread(new HttpJsonThread(handler, 0xC1, temp));
			thread.start();
		}
		
		btn_region.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				String temp = url_search_r + myApplication.getSessionID()
							+ "&username=" + myApplication.getUsername()
							+ "&region_prov=" + ((CItem)spinner_province.getSelectedItem()).GetID()
							+ "&region_city=" + ((CItem)spinner_city.getSelectedItem()).GetID()
							+ "&region_county=" + ((CItem)spinner_county.getSelectedItem()).GetID();
				Log.d("URL",temp);
				if(!myApplication.isNetConnected()){
					mdialog.cancel();
					ToastUtil.showToast(MapActivity.this, "网络未连接，请联网重试", Toast.LENGTH_SHORT);
				}else{
					Thread thread = new Thread(new HttpJsonPost(handler, 0xC4, temp));
					thread.start();
				}
			}
		});
		
		btn_show.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				show = true;
				String temp = url_search_n + myApplication.getSessionID()
						+ "&username=" + myApplication.getUsername()
						+ "&&wx_name=" + et_name.getText().toString();
				Log.d("URL",temp);
				if(!myApplication.isNetConnected()){
					mdialog.cancel();
					ToastUtil.showToast(MapActivity.this, "网络未连接，请联网重试", Toast.LENGTH_SHORT);
				}else{
					Thread thread = new Thread(new HttpJsonThread(handler,0xC4,temp));
					thread.start();
				}
			}
		});
		
		btn_name.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				String temp = url_search_n + myApplication.getSessionID()
							+ "&username=" + myApplication.getUsername()
							+ "&&wx_name=" + et_name.getText().toString();
				Log.d("URL",temp);
				if(!myApplication.isNetConnected()){
					mdialog.cancel();
					ToastUtil.showToast(MapActivity.this, "网络未连接，请联网重试", Toast.LENGTH_SHORT);
				}else{
					Thread thread = new Thread(new HttpJsonThread(handler,0xC4,temp));
					thread.start();
				}
			}
		});
	}
	
	@SuppressLint("HandlerLeak") private Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case 0xC1:
				parserJsonDataResult(msg.obj.toString(),spinner_province);
				mdialog.cancel();
				break;
			case 0xC2:
				parserJsonDataResult(msg.obj.toString(),spinner_city);
				mdialog.cancel();
				break;
			case 0xC3:
				parserJsonDataResult(msg.obj.toString(),spinner_county);
				mdialog.cancel();
				break;
			case 0xC4:
				String temp = parserJsonDataResult(msg.obj.toString());
				if(temp.equals("0")){
					ToastUtil.showToast(MapActivity.this, "未查到维修点", Toast.LENGTH_SHORT);
				}else
				{
					Intent intent = new Intent(MapActivity.this,ProductListActivity.class);
					intent.putExtra("result", msg.obj.toString());
					intent.putExtra("show",show);
					Log.d("RESULT",msg.obj.toString());
					startActivity(intent);
				}
				break;
			default:
				break;
			}
		}
	};
	
	private String parserJsonDataResult(String str){
		JSONObject temp = null;
		String result = null;
		try {

			temp =  new JSONObject(str);
			result = temp.getString("result");
			if(Integer.valueOf(result) == 1){
				String wxshop = temp.getString("wxshop");
				Log.d("REGIONS ARRAY", wxshop);
				return wxshop;
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "0";
		
	}
	private int parserJsonDataResult(String strContent,Spinner spn) {
		
		JSONObject temp = null;
		
		String result = null;
		
		try {

			temp =  new JSONObject(strContent);
			result = temp.getString("result");
			if(Integer.valueOf(result) == 1){
				JSONArray regions = new JSONArray(temp.getString("regions"));
				Log.d("REGIONS ARRAY", regions.toString());
				fillSpiner(regions,spn);
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return Integer.valueOf(result);
	}
	
	private void fillSpiner(JSONArray js,Spinner sp){
		List<CItem > list = new ArrayList<CItem>();
		CItem ctx = new CItem("","请选择");
		list.add(ctx);
		for(int i = 0;i < js.length();i++){
			try {
				CItem  ct = new CItem (js.getJSONObject(i).getString("region_id"),js.getJSONObject(i).getString("region_name"));
				list.add(ct);
			}
			catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		ArrayAdapter<CItem > Adapter = new ArrayAdapter<CItem>(MapActivity.this,
				android.R.layout.simple_spinner_item, list);
		sp.setAdapter(Adapter);
	}
	private void Construct(){
		btn_name = (Button) findViewById(R.id.btn_name);
		btn_region = (Button) findViewById(R.id.button1);
		btn_show = (Button) findViewById(R.id.btn_show);
		spinner_city = (Spinner) findViewById(R.id.spiner_city);
		spinner_county = (Spinner) findViewById(R.id.spiner_county);
		spinner_province = (Spinner) findViewById(R.id.spinner_provice);
		et_name = (EditText) findViewById(R.id.et_pd_name);
		myApplication = (MyApplication) MapActivity.this.getApplicationContext();
		mdialog = ProgressDialog.show(MapActivity.this,"", "请稍候...");
		mdialog.setOnKeyListener(new OnKeyListener() {
			
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				if (keyCode == KeyEvent.KEYCODE_BACK  
                        && event.getRepeatCount() == 0)					
				
				mdialog.cancel();
				return false;
			}
		});
		/**
		 * 默认Spinner Adapter 只有“请选择”
		 */
		ArrayList<CItem> list = new ArrayList<CItem>();
		CItem ct = new CItem("","请选择");
		list.add(ct);
		adapter_origin = new ArrayAdapter<CItem>(MapActivity.this,
				android.R.layout.simple_spinner_item, list);
		
		spinner_city.setAdapter(adapter_origin);
		spinner_county.setAdapter(adapter_origin);
		spinner_province.setAdapter(adapter_origin);
				
	}

}
