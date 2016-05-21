package com.phoenix.soft;

import com.baidu.mapapi.SDKInitializer;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * ��дApplication �洢ȫ�ֱ����Լ�������
 * @author phoenix
 *
 */
public class MyApplication extends Application {

	/**
	 * sessionID �Ի���Ϣ
	 * isLogin �Ƿ��Ѿ���¼
	 * username �û���
	 * brand Ʒ��json����
	 * isBrandeGeted Ʒ�������Ƿ��Ѿ���ȡ
	 * lon ����
	 * lat γ��
	 * isGPSGeted GPS�Ƿ��Ѿ���ȡ
	 * lltpye GPS����//0 ��׼GPS 1 �ٶ�GPS 
	 */
	private String sessionID = null;
	private Boolean isLogin = false;
	private String username = null;
	private String brand = null;
	private Boolean isBrandeGeted = false;
	private String lon = null;
	private String lat = null;
	private Boolean isGPSGeted = false;
	private int lltype = 0;
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		SDKInitializer.initialize(getApplicationContext());
	}

	public String getSessionID() {
		return sessionID;
	}

	public void setSessionID(String sessionID) {
		this.sessionID = sessionID;
	}

	public Boolean isLogin() {
		return isLogin;
	}

	public void setIsLogin(Boolean isLogin) {
		this.isLogin = isLogin;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public Boolean getIsBrandeGeted() {
		return isBrandeGeted;
	}

	public void setIsBrandeGeted(Boolean isBrandeGeted) {
		this.isBrandeGeted = isBrandeGeted;
	}

	public String getLon() {
		return lon;
	}

	public void setLon(String lon) {
		this.lon = lon;
	}

	public String getLat() {
		return lat;
	}

	public void setLat(String lat) {
		this.lat = lat;
	}
	
	/**
	 * �����⹤��
	 * @return �Ƿ�����������
	 */
    public boolean isNetConnected() {  
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);  
        if (cm != null) {  
            NetworkInfo[] infos = cm.getAllNetworkInfo();  
            if (infos != null) {  
                for (NetworkInfo ni : infos) {  
                    if (ni.isConnected()) {  
                        return true;  
                    }  
                }  
            }  
        }  
        return false;  
    }

	public Boolean getIsGPSGeted() {
		return isGPSGeted;
	}

	public void setIsGPSGeted(Boolean isGPSGeted) {
		this.isGPSGeted = isGPSGeted;
	}

	public int getLltype() {
		return lltype;
	}

	public void setLltype(int lltype) {
		this.lltype = lltype;
	}

}
