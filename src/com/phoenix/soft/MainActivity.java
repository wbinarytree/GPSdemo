package com.phoenix.soft;

import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.mapapi.SDKInitializer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnKeyListener;
import android.content.SharedPreferences.Editor;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;

/**
 * MainActivity
 * Including GPS&Buttons
 * GPS using physical GPS module and Baidu API
 * @author Phoenix
 *
 */
public class MainActivity extends Activity {
	
	/**
	 * View Group Members
	 */
	private Button btn_getGPS;
	private Button btn_record;
	private Button btn_store;
	private Button btn_wx;
	private Button btn_recordGPS;
	private Button btn_getBGPS;
	private TextView tv;
	private LocationManager locationManager;
	private MyApplication myApplication;
	private SharedPreferences sp;
	private Editor editor;
	/**
	 * Flags 
	 */
	private int flag = 0;
	private Dialog mDialog;
	/**
	 * for double kick back
	 */
    private int back;
    private Long time = (long) 0;
	private Long time_new = (long) 0;

	/**
	 * URL String
	 */
	private String url_brand = "http://www.wx45.com/json.php?mod=app&act=querybrand";
	private final static String TAG = "GPS";
	/**
	 * BaiduMap View
	 */
	private LocationClient mLocationClient = null;
	private BDLocationListener myListener =  new MyLocationListener();
	private LocationListener locationListener;
	private GpsStatus.Listener listener;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        
        //Construct Activity and Views
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());  
        setContentView(R.layout.activity_main);
        Construct();
        
        //Internet Test
        if(myApplication.getIsBrandeGeted() == false){
			if(!myApplication.isNetConnected()){
				ToastUtil.showToast(MainActivity.this, "����δ���ӣ�����������", Toast.LENGTH_SHORT);
				mDialog.cancel();
			}else{
        	Thread thread = new Thread(new HttpJsonThread(handler,0xF2,url_brand));
        	thread.start();
			}
        }
        
        //Button listener for GPS reporter
        btn_recordGPS.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Log.d("Button",btn_recordGPS.getId() + "");
				LayoutInflater inflater = getLayoutInflater();
				final View layout = inflater.inflate(R.layout.dialog_record,(ViewGroup) findViewById(R.id.dialog));
			    final EditText et_lat= (EditText) layout.findViewById(R.id.et_lat);
			    final EditText et_lon= (EditText) layout.findViewById(R.id.et_lon);
			    final RadioGroup rg = (RadioGroup) layout.findViewById(R.id.radioGroup1);
			    Dialog dialog = new AlertDialog.Builder(MainActivity.this).setTitle("�����뾭γ��").setView(layout)
			     .setNegativeButton("ȡ��", null)
			     .setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						
						myApplication.setLat(et_lat.getText().toString());
						myApplication.setLon(et_lon.getText().toString());
						myApplication.setIsGPSGeted(true);
						switch (rg.getCheckedRadioButtonId()) {
						case R.id.radio0:
							myApplication.setLltype(1);
							break;
						case R.id.radio1:
							myApplication.setLltype(0);
						default:
							break;
						}
						String temp = "��ǰλ��(����:"
									+ et_lon.getText().toString()
									+ ",γ��:"
									+ et_lat.getText().toString()
									+ ")";
						tv.setText(temp);
						btn_record.setEnabled(true);
					}
				}).create();
			   dialog.show();
			}
		});
        
        //Button lisner for getting Baidu GPS data 
        btn_getBGPS.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Log.d("Button",btn_getBGPS.getId() + "");
				LocationClientOption option = new LocationClientOption();
				option.setLocationMode(LocationMode.Hight_Accuracy);//���ö�λģʽ
				option.setCoorType("bd09ll");//���صĶ�λ����ǰٶȾ�γ��,Ĭ��ֵgcj02
				option.setScanSpan(1000);//���÷���λ����ļ��ʱ��Ϊ5000ms
				option.setIsNeedAddress(true);//���صĶ�λ���������ַ��Ϣ
				option.setNeedDeviceDirect(true);//���صĶ�λ��������ֻ���ͷ�ķ���
				option.setOpenGps(true);
				mLocationClient.setLocOption(option);
				mLocationClient.start();
				mLocationClient.registerLocationListener(myListener);
				Log.d("LocSDK4",mLocationClient.isStarted() + "");
				if (mLocationClient != null && mLocationClient.isStarted()){
					mLocationClient.requestLocation();
					tv.setText("���ڲ�ѯ...");
				}
				else 
					Log.d("LocSDK4", "locClient is null or not started");
			}
		});
        
        // Button for Calling  RecordActivity
        btn_record.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MainActivity.this,RecordActivity.class);
				startActivity(intent);
				
			}
		});

        // Button for calling StoreActivity
        btn_store.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MainActivity.this,StoreActivity.class);
				startActivity(intent);
				
			}
		});
        // Button for calling MapActivity
        btn_wx.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MainActivity.this,MapActivity.class);
				startActivity(intent);
				
			}
		});
        
		
        // Button for getting physical GPS data
        btn_getGPS.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				flag = 0;
				tv.setText("���ڲ�ѯ�����Ժ�...");
				locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 
						1000, 0,locationListener);
		        locationManager.addGpsStatusListener(listener);
			}

        
        });
        
        //Physical GPS locationListener
        locationListener = new LocationListener() {
			
			public void onLocationChanged(Location location) {
				// TODO Auto-generated method stub
				if(flag == 0){
					flag++;
					Log.d("GPS","FLAG:"+flag);
		            Log.d("GPS", "ʱ�䣺"+location.getTime()); 
		            Log.d("GPS", "���ȣ�"+location.getLongitude()); 
		            Log.d("GPS", "γ�ȣ�"+location.getLatitude()); 
		            Log.d("GPS", "���Σ�"+location.getAltitude()); 
					showLocation(location);
				}
				else{
					btn_record.setEnabled(true);
					myApplication.setLat(location.getLatitude() +"");
					myApplication.setLon(location.getLongitude() + "");
					myApplication.setIsGPSGeted(true);
					myApplication.setLltype(0);
					Log.d("GPS","FLAG:"+flag);
		            Log.d("GPS", "ʱ�䣺"+location.getTime()); 
		            Log.d("GPS", "���ȣ�"+location.getLongitude()); 
		            Log.d("GPS", "γ�ȣ�"+location.getLatitude()); 
		            Log.d("GPS", "���Σ�"+location.getAltitude()); 
					showLocation(location);
					ToastUtil.showToast(MainActivity.this, "��ȡ�ɹ��������¼����Ϣ", Toast.LENGTH_SHORT);
					locationManager.removeUpdates(this);
				}

			}

			public void onProviderDisabled(String provider) {
				// TODO Auto-generated method stub
				showLocation(null);
			}

			public void onProviderEnabled(String provider) {
				// TODO Auto-generated method stub
				showLocation(locationManager.getLastKnownLocation(provider));
			}

			public void onStatusChanged(String provider, int status, Bundle extras) {
				// TODO Auto-generated method stub
				switch (status) {
				//GPS״̬Ϊ�ɼ�ʱ
	            case LocationProvider.AVAILABLE:
	                Log.i("GPS", "��ǰGPS״̬Ϊ�ɼ�״̬");
	                break;
	            //GPS״̬Ϊ��������ʱ
	            case LocationProvider.OUT_OF_SERVICE:
	                Log.i("GPS", "��ǰGPS״̬Ϊ��������״̬");
	                break;
	            //GPS״̬Ϊ��ͣ����ʱ
	            case LocationProvider.TEMPORARILY_UNAVAILABLE:
	                Log.i("GPS", "��ǰGPS״̬Ϊ��ͣ����״̬");
	                break;
	            }
			}
		};
    
		// Physical GpsStatus use for test
		listener = new GpsStatus.Listener() {
        public void onGpsStatusChanged(int event) {
            switch (event) {
            //��һ�ζ�λ
            case GpsStatus.GPS_EVENT_FIRST_FIX:
                Log.i(TAG, "��һ�ζ�λ");
                break;
            //����״̬�ı�
            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                Log.i(TAG, "����״̬�ı�");
                //��ȡ��ǰ״̬
                GpsStatus gpsStatus=locationManager.getGpsStatus(null);
                //��ȡ���ǿ�����Ĭ�����ֵ
                int maxSatellites = gpsStatus.getMaxSatellites();
                //����һ�������������������� 
                Iterator<GpsSatellite> iters = gpsStatus.getSatellites().iterator();
                int count = 0;     
                while (iters.hasNext() && count <= maxSatellites) {     
                   // GpsSatellite s = iters.next();     
                    count++;     
                }   
                System.out.println("��������"+count+"������");
                break;
            //��λ����
            case GpsStatus.GPS_EVENT_STARTED:
                Log.i(TAG, "��λ����");
                break;
            //��λ����
            case GpsStatus.GPS_EVENT_STOPPED:
                Log.i(TAG, "��λ����");
                break;
            	}
        	}
    	};
    }

    //Log GPS data
    public void showLocation(Location currentLocation){
    	if(currentLocation != null){
	    	String s = "";
	    	s += " ��ǰλ��: (����:";
	    	s += currentLocation.getLongitude();
	    	s += ",γ��:";
	    	s += currentLocation.getLatitude();
	    	s += ")";
	    	tv.setText(s);
    	}
    	else{
    		tv.setText("���ڲ�ѯ�����Ժ�...");
    	}
    }
    
    //Handler Member
    @SuppressLint("HandlerLeak") private Handler handler = new  Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case 0xF2:
				parserJsonDataResult(msg.obj.toString());
				mDialog.cancel();
				break;

			default:
				break;
			}
		}
    	
    };

    //ParserJSON
	private int parserJsonDataResult(String strContent) {
			
			JSONObject temp = null;
			
			String result = null;
			
			try {

				temp =  new JSONObject(strContent);
				result = temp.getString("result");
				if(Integer.valueOf(result) == 1){
					JSONArray brand = new JSONArray(temp.getString("brands"));
					Log.d("BRANDS ARRAY", brand.toString());
					myApplication.setBrand(brand.toString());
					myApplication.setIsBrandeGeted(true);
					editor.putString("brands", brand.toString());
					editor.commit();
				}
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return Integer.valueOf(result);
	}
    
	//View Constructor
    private void Construct(){
        myApplication = (MyApplication) MainActivity.this.getApplicationContext();
        btn_record = (Button) findViewById(R.id.btn_record);
        btn_getGPS = (Button) findViewById(R.id.button1);
        btn_store = (Button) findViewById(R.id.btn_store);
        btn_wx = (Button) findViewById(R.id.btn_wx);
        btn_recordGPS = (Button) findViewById(R.id.btn_recordGPS);
        btn_getBGPS = (Button) findViewById(R.id.btn_getBGPS);
        tv = (TextView) findViewById(R.id.tx);
        sp = this.getPreferences(MODE_PRIVATE);
        editor = sp.edit();
        mLocationClient = new LocationClient(MainActivity.this);
        mLocationClient.registerLocationListener(myListener); 
        mDialog = ProgressDialog.show(MainActivity.this,"", "���Ժ�...");
		mDialog.setOnKeyListener(new OnKeyListener() {
			
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				if (keyCode == KeyEvent.KEYCODE_BACK  
                        && event.getRepeatCount() == 0)
					handler.removeMessages(0xF2);
					
				
				mDialog.cancel();
				return false;
			}
		});
    }
    
    //Double kick
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub

		if(back == 0){
			back++;
			ToastUtil.showToast(MainActivity.this, "�ٰ�һ���˳�Ӧ��", Toast.LENGTH_SHORT);
			time = System.currentTimeMillis();
		}
		else{
			time_new = System.currentTimeMillis();
			if(time_new - time < 2000){
			back = 0;
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_HOME);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			}else{
				back = 0;
			}
		}

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Log.d("Acitivity Life Cycle", "onPause");
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Boolean tmp = myApplication.getIsGPSGeted();
		if(!tmp){
			btn_record.setEnabled(false);
			tv.setText("GPS��Ϣ");
			Log.d("View","RESET");
		}
		Log.d("Acitivity Life Cycle", "onResume");
	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		Log.d("Acitivity Life Cycle", "onRestart");
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		Log.d("Acitivity Life Cycle", "onStart");
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		Log.d("Acitivity Life Cycle", "onStop");
	}
	
	//BaiduGPS listener
	public class MyLocationListener implements BDLocationListener {
		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null)
		            return ;
			StringBuffer sb = new StringBuffer(256);
			sb.append("time : ");
			sb.append(location.getTime());
			sb.append("\nerror code : ");
			sb.append(location.getLocType());
			sb.append("\nlatitude : ");
			sb.append(location.getLatitude());
			sb.append("\nlontitude : ");
			sb.append(location.getLongitude());
			sb.append("\nradius : ");
			sb.append(location.getRadius());
			if (location.getLocType() == BDLocation.TypeGpsLocation){
				sb.append("\nspeed : ");
				sb.append(location.getSpeed());
				sb.append("\nsatellite : ");
				sb.append(location.getSatelliteNumber());
			} else if (location.getLocType() == BDLocation.TypeNetWorkLocation){
				sb.append("\naddr : ");
				sb.append(location.getAddrStr());
			} 
 
			
			Log.d("BAIDU GPS",sb.toString());
			myApplication.setLat(location.getLatitude() + "");
			myApplication.setLon(location.getLongitude()+ "");
			myApplication.setLltype(1);
			String temp = "��ǰλ��(����:"
					+ location.getLongitude()
					+ ",γ��:"
					+ location.getLatitude()
					+ ")";
			tv.setText(temp);
			btn_record.setEnabled(true);
			mLocationClient.stop();
		}
	}
}
