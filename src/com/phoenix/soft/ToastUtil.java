package com.phoenix.soft;  
  
import android.content.Context;  
import android.os.Handler;
import android.widget.Toast;  
  
/** 
 * class name：ToastUtil
 * class description：显示toast的一个工具类
 */  
public class ToastUtil {  
	private static Toast mToast;
	private static Handler mhandler = new Handler();
	private static Runnable r = new Runnable(){
		public void run() {
			mToast.cancel();
		};
	};
	
	public static void showToast (Context context, String text, int duration) {
		mhandler.removeCallbacks(r);
		if (null != mToast) {
			mToast.setText(text);
		} else {
			mToast = Toast.makeText(context, text, duration);
		}
		mhandler.postDelayed(r, 5000);
		mToast.show();
	}
	
	public static void showToast (Context context, int strId, int duration) {
		showToast (context, context.getString(strId), duration);
	}
	
	

}  