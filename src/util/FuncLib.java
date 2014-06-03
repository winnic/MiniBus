package util;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.example.minibus.Amap;
import com.example.minibus.DemoAmap;

public class FuncLib {
	public static boolean saveArray(String[] array, String arrayName, Context mContext, String preferenceFolder) {   
	    SharedPreferences prefs = mContext.getSharedPreferences(preferenceFolder, Context.MODE_PRIVATE);  
	    SharedPreferences.Editor editor = prefs.edit();  
	    editor.putInt(arrayName +"_size", array.length);  
	    for(int i=0;i<array.length;i++)  
	        editor.putString(arrayName + "_" + i, array[i]);  
	    return editor.commit();  
	} 
	public static String[] loadArray(String arrayName, Context mContext,String preferenceFolder) {  
	    SharedPreferences prefs = mContext.getSharedPreferences(preferenceFolder, Context.MODE_PRIVATE);  
	    int size = prefs.getInt(arrayName + "_size", 0);  
	    String array[] = new String[size];  
	    for(int i=0;i<size;i++)  
	        array[i] = prefs.getString(arrayName + "_" + i, null);  
	    return array;  
	}  
	
	public static boolean connSuccess(String errMsg, Activity a) {  
		if(errMsg.matches("Connection to [^\\ ]+ refused")||errMsg.matches("Connect to [^\\ ]+ timed out")){
			DialogFragment F = new FireMissilesDialogFragment();
			Bundle args = new Bundle();
			args.putString("type","connectionErr");
		    args.putString("err", errMsg);
			F.setArguments(args);
		    F.show(a.getFragmentManager(), "missiles");
		    Log.v("testing",a.getClass().toString());
		    if(a.getClass().getCanonicalName().matches(".*\\.Amap.*"))
		    	((Amap) a).activateProgressBar(false);
		    else if(a.getClass().getCanonicalName().matches(".*\\.DemoAmap.*"))
		    	((DemoAmap) a).activateProgressBar(false);
			return false;
		} 
		return true;
	}

}
