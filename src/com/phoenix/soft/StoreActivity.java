package com.phoenix.soft;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


public class StoreActivity extends Activity {
	
	private Button btn_search;
	private TextView tv_result;
	private EditText et_pd_name;
	private EditText et_pd_sn;
	private EditText et_pd_desc;
	private Spinner sp_store;
	private MyApplication myApplication;
	private String url = "http://www.wx45.com/json.php?mod=app&act=querystore&session_id=";
	private ArrayList<CItem> list;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_store);
		Construct();
		btn_search.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(et_pd_desc.getText().toString().equals("")
						&&et_pd_name.getText().toString().equals("")
						&&et_pd_sn.getText().toString().equals("")){
					ToastUtil.showToast(StoreActivity.this, "请至少输入一项", Toast.LENGTH_SHORT);
				}else{
					String temp = url + myApplication.getSessionID()
							    + "&username=" + myApplication.getUsername()
							    + "&store_region=" + ((CItem)sp_store.getSelectedItem()).GetID()
							    + "&goods_name=" + et_pd_name.getText().toString()
							    + "&goods_sn=" + et_pd_sn.getText().toString()
							    + "&goods_desc=" +et_pd_desc.getText().toString();
					if(!myApplication.isNetConnected()){
						ToastUtil.showToast(StoreActivity.this, "网络未连接，请联网重试", Toast.LENGTH_SHORT);
					}else{
						Thread thread = new Thread(new HttpJsonThread(handler, 0xB1, temp));
						thread.start();
					}
				}
			}
		});
		
		list = new ArrayList<CItem>();
		String[] temp = {"全部","绵阳","深圳"};
		for (int i = 0;i<3;i++){
			CItem ct = new CItem(i+"", temp[i]);
			list.add(ct);
		}
		ArrayAdapter<CItem > Adapter = new ArrayAdapter<CItem>(StoreActivity.this,
				android.R.layout.simple_spinner_item, list);
		sp_store.setAdapter(Adapter);
	}
	
	@SuppressLint("HandlerLeak") private Handler handler = new  Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case 0xB1:
				int temp = parserJsonDataResult(msg.obj.toString());
				if(temp == 1){
					tv_result.setText("请求已发送");
					ToastUtil.showToast(StoreActivity.this, "请求已发送", Toast.LENGTH_SHORT);
					finish();
				}else{
					tv_result.setText("请求发送失败，请重试");
				}
				break;

			default:
				break;
			}
		}
    	
    };
    
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
	
	private void Construct(){
		myApplication = (MyApplication) StoreActivity.this.getApplicationContext();
		btn_search = (Button) findViewById(R.id.btn_search);
		tv_result = (TextView) findViewById(R.id.tv_result);
		et_pd_name = (EditText) findViewById(R.id.et_pd_name);
		et_pd_sn = (EditText) findViewById(R.id.et_pd_sn);
		et_pd_desc = (EditText) findViewById(R.id.et_pd_desc);
		sp_store = (Spinner) findViewById(R.id.spinner_region_store);
		
	}

}
