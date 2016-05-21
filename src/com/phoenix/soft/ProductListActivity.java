package com.phoenix.soft;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.InfoWindow.OnInfoWindowClickListener;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnKeyListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class ProductListActivity extends ListActivity {

	
	/**
	 * View Group 
	 * BaiduView: mBaiduMap mMapView
	 * Temp Url String url
	 * r_po : rightplace position
	 * 
	 */
	 private MapView mMapView = null;  
	 private BaiduMap mBaiduMap;
	 private OverlayOptions option;
	 private LatLng pt1;
	 private String name;
	 private MyApplication myApplication;
	 private Intent intent;
	 private String json_reslut;
	 private WxItem[] wxItems;
	 private ArrayList<Map<String,Object>> mData ;
	 private ListView lv;
	 private String url = "http://www.wx45.com/json.php?mod=app&act=querywxshop&session_id=";
	 private Dialog mdialog;
	 private int r_po;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_list);
		Construct();
		parserJsonResult(json_reslut);
//		pt1 = new LatLng(Double.valueOf(myApplication.getLat()).doubleValue(), 
//				Double.valueOf(myApplication.getLon()).doubleValue()); 
//		pt1 = new LatLng(31, 113);
		
		//get Intent Data
		Boolean btmp = intent.getBooleanExtra("show", false);
		Log.d("show",btmp.toString());
		if(btmp){
			for(int i = 0;i < wxItems.length;i++){
				LatLng pt = new LatLng(Double.valueOf(wxItems[i].getLat()).doubleValue(),
						 Double.valueOf(wxItems[i].getLon()).doubleValue());
				BitmapDescriptor bitmap = BitmapDescriptorFactory  
					    .fromResource(R.drawable.icon_marka);  
				//构建MarkerOption，用于在地图上添加Marker  
				option = new MarkerOptions()  
					    .position(pt)  
					    .icon(bitmap)
					    .title(wxItems[i].getName());
				//在地图上添加Marker，并显示  
				mBaiduMap.addOverlay(option);
			}
			// Locate to new position 
			MapStatusUpdate ms = MapStatusUpdateFactory.newLatLngZoom(new LatLng(Double.valueOf(wxItems[0].getLat()).doubleValue(),
					 Double.valueOf(wxItems[0].getLon()).doubleValue()),15);
			mBaiduMap.setMapStatus(ms);
			
		}
		
		// show marker information
		mBaiduMap.setOnMarkerClickListener(new OnMarkerClickListener() {
			
			@Override
			public boolean onMarkerClick(Marker arg0) {
				// TODO Auto-generated method stub
				//创建InfoWindow  
				Button tv = new Button(ProductListActivity.this);
				tv.setText(arg0.getTitle());
				tv.setBackgroundResource(R.drawable.pop);
				tv.setGravity(Gravity.CENTER);
				OnInfoWindowClickListener listener = new OnInfoWindowClickListener() {  
				    public void onInfoWindowClick() {    
				    	mBaiduMap.hideInfoWindow();
				    }  
				};  
				InfoWindow mInfoWindow = new InfoWindow(tv, arg0.getPosition(), listener);  
				//显示InfoWindow  
				mBaiduMap.showInfoWindow(mInfoWindow);
				return false;
			}
		});
		
		//get selected store information via Internet
		
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position,
				    long id) {
				// TODO Auto-generated method stub
				mdialog.show();
				String goods_id = wxItems[position].getId();
				String temp = url + myApplication.getSessionID()
							+ "&username=" + myApplication.getUsername()
							+ "&wxshop_id=" +goods_id;
				r_po = position;
				Log.d("URL",temp);
				if(!myApplication.isNetConnected()){
					mdialog.cancel();
					ToastUtil.showToast(ProductListActivity.this, "网络未连接，请联网重试", Toast.LENGTH_SHORT);
				}else{
					Thread thread = new Thread(new HttpJsonThread(handler, 0xD1,temp));
					thread.start();
				}
			}
		});
		
		
	}
	
	private void Construct(){
		myApplication = (MyApplication) ProductListActivity.this.getApplicationContext();
		mMapView = (MapView) findViewById(R.id.bmapView);  
		mBaiduMap = mMapView.getMap(); 
		
		intent = getIntent();
		json_reslut = intent.getStringExtra("result");
		mData = new ArrayList<Map<String,Object>>();
		lv = ProductListActivity.this.getListView();
		mdialog = ProgressDialog.show(ProductListActivity.this,"", "请稍候...");
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
		
	}
	
	private void parserJsonResult(String strContent) {
		
		
		JSONObject js;
		JSONArray arr;
		try {
			js = new JSONObject(strContent);
		
			if(js.getString("result").equals("1")){
			arr = new JSONArray(js.getString("wxshop"));
			wxItems = new WxItem[arr.length()];
			for(int i = 0;i<arr.length();i++){
				wxItems[i] = new WxItem();
				wxItems[i].setId(arr.getJSONObject(i).getString("id"));
				wxItems[i].setName(arr.getJSONObject(i).getString("wx_name"));
				wxItems[i].setProvince(arr.getJSONObject(i).getString("wx_province"));
				wxItems[i].setCity(arr.getJSONObject(i).getString("wx_city"));
				wxItems[i].setAddress(arr.getJSONObject(i).getString("wx_address"));
				wxItems[i].setResponsible(arr.getJSONObject(i).getString("wx_responsible"));
				wxItems[i].setTel(arr.getJSONObject(i).getString("wx_tel"));
				wxItems[i].setCounty(arr.getJSONObject(i).getString("wx_county"));
				wxItems[i].setLat(arr.getJSONObject(i).getString("wx_lat"));
				wxItems[i].setLon(arr.getJSONObject(i).getString("wx_lon"));
				
				}
			filledListView();
			}
		}
		catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// set ListView Adapter 
	private void filledListView(){
		mData.clear();
		for(int i = 0;i < wxItems.length; i ++){
			Map<String,Object> item = new HashMap<String,Object>();
			item.put("name",  wxItems[i].getName());
			item.put("address", "地址:" + wxItems[i].getAddress());
			item.put("local", wxItems[i].getProvince() + wxItems[i].getCity() + wxItems[i].getCounty());
			item.put("tel", "联系电话:" + wxItems[i].getTel());
			item.put("resp", "联系人\n" +wxItems[i].getResponsible());
			mData.add(item);	
		}
		SimpleAdapter adapter = new SimpleAdapter(this,mData,R.layout.list,
				new String[]{"name","address","local","tel","resp"},
				new int[]{R.id.name,R.id.tv_adress,R.id.tv_local,R.id.tv_tel,R.id.tv_res});
		setListAdapter(adapter);
		mdialog.cancel();
	}
	
	//handler get result from thread
	@SuppressLint("HandlerLeak") private Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case 0xD1:
				mdialog.cancel();
				parserJsonDetail(msg.obj.toString());
				BitmapDescriptor bitmap = BitmapDescriptorFactory  
					    .fromResource(R.drawable.icon_marka);  
					//构建MarkerOption，用于在地图上添加Marker  
				option = new MarkerOptions()  
					    .position(pt1)  
					    .icon(bitmap)
					    .title(name);
					//在地图上添加Marker，并显示  
				
				
			
				MapStatusUpdate ms = MapStatusUpdateFactory.newLatLng(pt1);
				mBaiduMap.setMapStatus(ms);
				mBaiduMap.addOverlay(option);

				break;
			
			default:
				break;
			}
		}
	};
	
	//parser json data to wxItems
	private void parserJsonDetail(String str){
		JSONObject js;
		JSONObject arr;
		try {
			js = new JSONObject(str);
		
		if(js.getString("result").equals("1")){
			 arr = new JSONObject(js.getString("wxshop"));
			 wxItems[r_po].setLat(arr.getString("wx_lat"));
			 wxItems[r_po].setLon(arr.getString("wx_lon"));
			 name = wxItems[r_po].getName();
			 pt1 = new LatLng(Double.valueOf(wxItems[r_po].getLat()).doubleValue(),
					 Double.valueOf(wxItems[r_po].getLon()).doubleValue());
		}
		
		}
		catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
