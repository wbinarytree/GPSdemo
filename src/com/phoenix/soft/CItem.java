package com.phoenix.soft;

/**
 * 用于存储在Adapter工具类
 * 在adapter中存储键值对
 * @author phoenix
 *
 */


public class CItem {

	private String ID = "";
	private String Value = "";

	public CItem () {
		ID = "";
		Value = "";
	}

	public CItem (String _ID, String _Value) {
		ID = _ID;
		Value = _Value;
	}

	@Override
	public String toString() {          
	// TODO Auto-generated method stub
		return Value;
	}

	public String GetID() {
		return ID;
	}

	public String GetValue() {
		return Value;
	}
}
