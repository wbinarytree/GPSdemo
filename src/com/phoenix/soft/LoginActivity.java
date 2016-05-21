package com.phoenix.soft;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnKeyListener;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity {
	
	private static final String url = "http://www.wx45.com/json.php?mod=app&act=loginsale&username=";

	private Button btn_login;
	private EditText et_username;
	private EditText et_password;
	private String sessionId;
	private MyApplication myApplication;
	private String username;
	private String password;
	private SharedPreferences sp;
	private Editor editor;
	private Dialog mdialog;
	private CheckBox cb_save;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_login);
		Construct();
		cb_save.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				// TODO Auto-generated method stub
			if(arg1){
				editor.putBoolean("isSave", true);
				editor.putString("username", et_username.getText().toString());
				editor.putString("password", et_password.getText().toString());
				editor.commit();
			}
			else
			{
				editor.putBoolean("isSave", false);
				editor.commit();
			}
			}
		});
		btn_login.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

				
				username = et_username.getText().toString();
				password = et_password.getText().toString();
				String temp = url + username
							+ "&passwd=" + password
							+ "&checkcode=cc";
				Log.d("URL",temp);
				mdialog = ProgressDialog.show(LoginActivity.this,"", "正在登陆");
				mdialog.setOnKeyListener(new OnKeyListener() {
					
					@Override
					public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
						// TODO Auto-generated method stub
						if (keyCode == KeyEvent.KEYCODE_BACK  
		                        && event.getRepeatCount() == 0)
							handler.removeMessages(0xF1);
							
						
						mdialog.cancel();
						return false;
					}
				});
				if(!myApplication.isNetConnected()){
					mdialog.cancel();
					ToastUtil.showToast(LoginActivity.this, "网络未连接，请联网重试", Toast.LENGTH_SHORT);
				}else{
				
				Thread thread = new Thread(new HttpJsonPost(handler, 0xF1, temp));
				thread.start();
				}
				
			}
		});
		
		et_username.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub
				cb_save.setChecked(false);
				et_password.setText("");
			}
			
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
	}
	
	private void Construct(){
		btn_login = (Button) findViewById(R.id.btn_login_main);
		et_username = (EditText) findViewById(R.id.et_login);
		et_password = (EditText) findViewById(R.id.et_password);
		myApplication = (MyApplication) LoginActivity.this.getApplicationContext();
		sp = this.getSharedPreferences("userinform", MODE_PRIVATE);
		editor = sp.edit();
		cb_save = (CheckBox) findViewById(R.id.cb_save);
		
	}

	@SuppressLint("HandlerLeak") private Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case 0xF1:
				int temp = parserJsonDataResult(msg.obj.toString());
				switch (temp) {
				case 0:
					ToastUtil.showToast(LoginActivity.this, "登陆失败", Toast.LENGTH_SHORT);
					et_username.setText("");
					et_password.setText("");
					Log.d("HTTP","HTTP RESULT 0");
					mdialog.cancel();
					break;
				case 1:
					ToastUtil.showToast(LoginActivity.this, "登陆成功", Toast.LENGTH_SHORT);
					myApplication.setUsername(username);
					mdialog.cancel();
					Intent intent = new Intent(LoginActivity.this,MainActivity.class);
					startActivity(intent);
					Log.d("HTTP","HTTP RESULT 1");
					
					break;

				case 2:
					ToastUtil.showToast(LoginActivity.this, "密码错误", Toast.LENGTH_SHORT);
					et_password.setText("");
					Log.d("HTTP","HTTP RESULT 2");
					break;
				case 3:
					ToastUtil.showToast(LoginActivity.this, "用户不存在", Toast.LENGTH_SHORT);
					et_username.setText("");
					et_password.setText("");
					Log.d("HTTP","HTTP RESULT 3");
				
				default:
					break;
				}
				break;

			default:
				break;
			}
			super.handleMessage(msg);
		}
		
		
	};
	
	private int parserJsonDataResult(String strContent) {
		
		JSONObject temp = null;
		
		String result = null;
		
		try {

			
			temp =  new JSONObject(strContent);
			result = temp.getString("result");
			if(Integer.valueOf(result) == 1){
				sessionId = temp.getString("session_id");
				myApplication.setSessionID(sessionId);
				editor.putString("sessionID", sessionId);
				editor.putString("province", temp.getString("province"));
				editor.putString("city", temp.getString("city"));
				editor.putString("county", temp.getString("county"));
				editor.commit();
				myApplication.setSessionID(sessionId);
				myApplication.setIsLogin(true);
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return Integer.valueOf(result);
}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		boolean temp = sp.getBoolean("isSave", false);
		if(temp){
			et_username.setText(sp.getString("username", ""));
			et_password.setText(sp.getString("password", ""));
			cb_save.setChecked(true);
		}
	}
	
	
}
