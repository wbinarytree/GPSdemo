package com.phoenix.soft;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnKeyListener;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

public class RecordActivity extends Activity {

	
	/**
	 * Record Store data to database
	 * View Group
	 * 
	 * 
	 */
	private Spinner spinner_type;
	private Spinner spinner_province;
	private Spinner spinner_city;
	private Spinner spinner_county;
	private Dialog mdialog;
	private Dialog cdialog;//品牌checkbox
	private MyApplication myApplication;
	private SharedPreferences sp;
	private Editor editor;
	private Button btn_brand;
	private Button btn_confirm;
	private ListView list_brand;
	private ArrayList<CItem> value;
	private ArrayAdapter<CItem> adapter_origin;
	
	//EditText for information about store
	private EditText et_wxname;
	private EditText et_address;
	private EditText et_responsible;
	private EditText et_tel;
	private EditText et_proportion;
	private EditText et_rec;
	
	/**
	 * 维修点类型
	 * 维修类型定义：
     *0-综合（默认）
     *1-家电
	 *2-电脑
	 *3-手机
	 *4-汽车
     *9-其它
	 */
	private String[] type_value = {"综合","家电","电脑","手机","汽车","其他"};
	private int[] type_key = {0,1,2,3,4,9};
	/**
	 * HTTP FLAG
	 * 0xA1 省
	 * 0xA2 市
	 * 0xA3 县
	 * 0xA4 录入
	 */
	
	private String url_region = "http://www.wx45.com/json.php?mod=app&act=queryregion&session_id=";
	private String url_record = "http://www.wx45.com/json.php?mod=app&act=recordwx&session_id=";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_record);
		Construct();
		List<CItem > list = new ArrayList<CItem>();

		for(int i = 0;i < 6;i++){
			CItem  ct = new CItem (type_key[i]+"",type_value[i]);
			list.add(ct);
		}
		
		ArrayAdapter<CItem > Adapter = new ArrayAdapter<CItem>(RecordActivity.this,
				android.R.layout.simple_spinner_item, list);
		//类型下拉菜单
		spinner_type.setAdapter(Adapter);
		spinner_type.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				//String str = ((CItem)arg0.getItemAtPosition(arg2)).GetID();
				//ToastUtil.showToast(RecordActivity.this, str, Toast.LENGTH_LONG);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
					
			}
		});
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
						ToastUtil.showToast(RecordActivity.this, "网络未连接，请联网重试", Toast.LENGTH_SHORT);
					}else{
						mdialog.cancel();
						Thread thread = new Thread(new HttpJsonThread(handler, 0xA2, temp));
						thread.start();
					}
				}
				//set default selection
				if(str.equals("请选择")){
					spinner_city.setSelection(0);
					spinner_county.setSelection(0);
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
					Log.d("URL",temp);
					if(!myApplication.isNetConnected()){
						mdialog.cancel();
						ToastUtil.showToast(RecordActivity.this, "网络未连接，请联网重试", Toast.LENGTH_SHORT);
					}else{
						Thread thread = new Thread(new HttpJsonThread(handler, 0xA3, temp));
						thread.start();
					}
				}
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
		});	
		
		brandBuilder();
		
		btn_brand.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				cdialog.show();
			}
		});
		
		btn_confirm.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				SparseBooleanArray tem = list_brand.getCheckedItemPositions();
				String wx_brand = "";
				//对品牌检查 这一步非常重要，极容易出现空指针异常
				if(tem.size() == 0){
					wx_brand = "";
				}else{
					for(int i = 0;i < tem.size();i++){
						if(tem.valueAt(i) == true){
							wx_brand = wx_brand + ((CItem)list_brand.getItemAtPosition(tem.keyAt(i))).GetID() + ",";
						}
					}	
				}
				
				if(wx_brand.length() != 0){
					wx_brand = wx_brand.substring(0,wx_brand.length()-1);
					Log.d("wx_brand", wx_brand);
				}
				else{
					Log.d("wx_brand", wx_brand);
				}
					
				String temp = url_record + myApplication.getSessionID()
						    + "&username=" + myApplication.getUsername()
						    + "&wx_name=" +et_wxname.getText().toString()
						    + "&wx_province=" + ((CItem)spinner_province.getSelectedItem()).GetID()
						    + "&wx_city=" + ((CItem)spinner_city.getSelectedItem()).GetID()
						    + "&wx_county=" + ((CItem)spinner_county.getSelectedItem()).GetID()
						    + "&wx_address=" + et_address.getText().toString()
						    + "&wx_responsible=" + et_responsible.getText().toString()
						    + "&wx_tel=" + et_tel.getText().toString().replace(" ", ",")
						    + "&wx_type=" + ((CItem)spinner_type.getSelectedItem()).GetID()
						    + "&wx_brand=" + wx_brand
						    + "&wx_proportion=" + et_proportion.getText().toString()
						    + "&wx_lon=" + myApplication.getLon()
						    + "&wx_lat=" + myApplication.getLat()
						    + "&ll_type=" + myApplication.getLltype()
						    + "&wx_account=" + et_rec.getText().toString().replace(" ",",");
				
				Log.d("URL",temp);
				if(!myApplication.isNetConnected()){
					mdialog.cancel();
					ToastUtil.showToast(RecordActivity.this, "网络未连接，请联网重试", Toast.LENGTH_SHORT);
				}else{
					Thread thread = new Thread(new HttpJsonThread(handler, 0xA4, temp));
					thread.start();
				}
			}
		});
		//获取省信息
		mdialog.show();
		String temp = url_region + myApplication.getSessionID()
				    + "&username=" + myApplication.getUsername()
				    + "&region_id=1&region_type";
		Log.d("URL",temp);
		if(!myApplication.isNetConnected()){
			ToastUtil.showToast(RecordActivity.this, "网络未连接，请联网重试", Toast.LENGTH_SHORT);
			mdialog.cancel();
		}else{
			Thread thread = new Thread(new HttpJsonThread(handler, 0xA1, temp));
			thread.start();
		}
	
				
	}
	
	private void Construct(){
		myApplication = (MyApplication) RecordActivity.this.getApplicationContext();
		spinner_type = (Spinner) findViewById(R.id.spiner_type);
		spinner_city = (Spinner) findViewById(R.id.spiner_city);
		spinner_county = (Spinner) findViewById(R.id.spiner_county);
		spinner_province = (Spinner) findViewById(R.id.spinner_provice);
		btn_brand = (Button) findViewById(R.id.btn_checkbrand);
		btn_confirm = (Button) findViewById(R.id.btn_confirm);
		sp = RecordActivity.this.getPreferences(MODE_PRIVATE);
		
		et_address = (EditText) findViewById(R.id.et_address);
		et_proportion = (EditText) findViewById(R.id.et_proportion);
		et_responsible = (EditText) findViewById(R.id.et_responsible);
		et_tel = (EditText) findViewById(R.id.et_tel);
		et_wxname = (EditText) findViewById(R.id.et_wxname);
		et_rec  = (EditText) findViewById(R.id.et_rec);
		
		editor = sp.edit();
		list_brand = new ListView(this);
		mdialog = ProgressDialog.show(RecordActivity.this,"", "请稍候...");
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
		mdialog.cancel();
		/**
		 * 默认Spinner Adapter 只有“请选择”
		 */
		ArrayList<CItem> list = new ArrayList<CItem>();
		CItem ct = new CItem("","请选择");
		list.add(ct);
		adapter_origin = new ArrayAdapter<CItem>(RecordActivity.this,
				android.R.layout.simple_spinner_item, list);
		
		spinner_city.setAdapter(adapter_origin);
		spinner_county.setAdapter(adapter_origin);
		spinner_province.setAdapter(adapter_origin);
	}
	
	@SuppressLint("HandlerLeak") private Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case 0xA1:
				parserJsonDataResult(msg.obj.toString(),spinner_province);
				mdialog.cancel();
				break;
			case 0xA2:
				parserJsonDataResult(msg.obj.toString(),spinner_city);
				mdialog.cancel();
				break;
			case 0xA3:
				parserJsonDataResult(msg.obj.toString(),spinner_county);
				mdialog.cancel();
				break;
			case 0xA4:
				int temp = parserJsonDataResult(msg.obj.toString());
				if(temp == 1){
					ToastUtil.showToast(RecordActivity.this, "录入成功", Toast.LENGTH_SHORT);
					myApplication.setIsGPSGeted(false);
					myApplication.setLat(null);
					myApplication.setLon(null);
					RecordActivity.this.onBackPressed();
				}else{
					ToastUtil.showToast(RecordActivity.this, "录入失败，请重试", Toast.LENGTH_SHORT);
				}
				break;
			default:
				break;
			}
		}
		
	};
	
	//parser json data to spinner and fill next level spinner 
	private int parserJsonDataResult(String strContent,Spinner spn) {
		
		JSONObject temp = null;
		
		String result = null;
		
		try {

			temp =  new JSONObject(strContent);
			result = temp.getString("result");
			if(Integer.valueOf(result) == 1){
				JSONArray regions = new JSONArray(temp.getString("regions"));
				Log.d("REGIONS ARRAY", regions.toString());
				editor.putString("regions", regions.toString());
				editor.commit();
				fillSpiner(regions,spn);
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return Integer.valueOf(result);
	}
	private int parserJsonDataResult(String strContent) {
		
		JSONObject temp = null;
		
		String result = null;
		
		try {

			temp =  new JSONObject(strContent);
			result = temp.getString("result");
			
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
		ArrayAdapter<CItem > Adapter = new ArrayAdapter<CItem>(RecordActivity.this,
				android.R.layout.simple_spinner_item, list);
		sp.setAdapter(Adapter);
	}
	
	
	// build brand select dialog (through ListView to show)
	private void brandBuilder(){
		try {
			value = new ArrayList<CItem>();
			JSONArray brand = new JSONArray(myApplication.getBrand());
			for(int i = 0;i < brand.length();i++){
				CItem ct = new CItem(brand.getJSONObject(i).getString("brand_id"), brand.getJSONObject(i).getString("brand_name"));
				value.add(ct);
			}
			ArrayAdapter<CItem > Adapter = new ArrayAdapter<CItem>(RecordActivity.this,
					android.R.layout.simple_list_item_multiple_choice, value);
			list_brand.setAdapter(Adapter);
			list_brand.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
			
			// button is useless but need to show it
			cdialog = new AlertDialog.Builder(this).setTitle("请选择品牌")
				     .setView(list_brand)
				     .setPositiveButton("确定", null)
				     .setNegativeButton("取消", null)
				     .create();
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

}
