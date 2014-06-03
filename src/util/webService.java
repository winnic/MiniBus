package util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.minibus.Amap;
import com.example.minibus.BusInfo;
import com.example.minibus.DemoAmap;
import com.example.minibus.DemoBusInfo;
import com.example.minibus.ShowBus;

public class webService {
	private View outputText;
    private int method=1;
    private String ip="123.255.110.2";
    private String bus="";
    private String err=null;
    //123.255.104.87/redmini/api.php?action=Select_Bus

	 public void contextStartBrowerTo_URL(Context context,View v,String url,int searchMethod) {
		 method=searchMethod;
		 if (method==1){
			 outputText=(TextView)v;
		 }else if(method==2){
			 outputText=v;
			 bus=url.split("bus=")[1];
		 }else if(method==3){
			 outputText=(TextView)v;
		 }
		 
		 SharedPreferences sharedPref = ((Activity) outputText.getContext()).getSharedPreferences("IP",Context.MODE_WORLD_READABLE);
		 if(sharedPref.getString("ip", null)!=null){
			 ip = sharedPref.getString("ip",null);
			 Log.v("testing",ip);
		 }
		 
		 url="http://" +ip+url;

	        // Gets the URL from the UI's text field.
	        ConnectivityManager connMgr = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
	        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
	        if (networkInfo != null && networkInfo.isConnected()) {
	        	Log.v("testing", "contextStartBrowerTo_URL: network is okay");
	            new DownloadWebpageTask().execute(url);
	        } else {
	        	((TextView) outputText).setText("No network connection available.");
	        }
	    }

	    public class DownloadWebpageTask extends AsyncTask<String, Void, String> {

			@Override
	        protected String doInBackground(String... urls) {
	        	Log.v("testing", "doInBackground");
	        	Log.w("testing", urls[0]);
	        	String response = "";
	        	          
                response=getPageContent(urls[0]);
                //!!cannot get context
//                ((SearchPanel) outputText.getContext()).activateProgressBar(false);
//	            if(response.equals("")){
//	            	return "Unable to retrieve web page. URL may be invalid.";
//	            }
	            return response;
	        }

			private String getPageContent(String url) {
        		String response = "";
        		HttpParams httpParameters = new BasicHttpParams();
        		int timeoutConnection = 10000;
        		int timeoutSocket = 10000;
        		HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
        		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);	
        		DefaultHttpClient client = new DefaultHttpClient(httpParameters);
                HttpGet httpGet = new HttpGet(url);
                try {
                    HttpResponse execute = client.execute(httpGet);
                    InputStream content = execute.getEntity().getContent();

                    BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                    String s = "";
                    while ((s = buffer.readLine()) != null) {
                        response += s;
                    }
                } catch (Exception e) {
               	    err=e.getMessage();
                    //e.printStackTrace();
                }
                return response;
			}
			// onPostExecute displays the results of the AsyncTask.
	        @Override
	        protected void onPostExecute(String result) {
	        	if(err!=null){
	        		Log.v("testing", "response = "+result);
	        		Log.v("testing", "connectionn error  : "+err);
	        		//connectionn error
	        		if(err.matches("Connection to [^\\ ]+ refused")||err.matches("Connect to [^\\ ]+ timed out")){
	        			DialogFragment F = new FireMissilesDialogFragment();
		        		Bundle args = new Bundle();
		        		args.putString("type","connectionErr");
		        	    args.putString("err", err);
		        		F.setArguments(args);
	            	    F.show(((Activity) outputText.getContext()).getFragmentManager(), "missiles");
	            	    ((Amap) outputText.getContext()).activateProgressBar(false);
		        		return;
	        		}else{
	        			Log.v("testing","unkown connection error");
	        			DialogFragment F = new FireMissilesDialogFragment();
	        			F.show(((Activity) outputText.getContext()).getFragmentManager(), "missiles");
	        			((Amap) outputText.getContext()).activateProgressBar(false);
	        		}
	        	}
	        	
	        	Log.v("testing", "onPostExecute as connection suceeds");
	        	
	        	if(method==1){
//	 	        	outputText.setText(result);	
//	        		 ((SearchPanel) outputText.getContext()).activateProgressBar(false); 	        	
	 				Intent intent = new Intent(outputText.getContext(), ShowBus.class);
	 				intent.putExtra("buslist_str", result);
	 				intent.putExtra("method", 1);
	 				Log.v("testing","going in to ShowBus activity");
	 				outputText.getContext().startActivity(intent);
	        	 }else if(method==2){		    
	        		if(outputText.getContext().getClass().getCanonicalName().matches(".*\\.Amap.*"))
	        			 ((Amap) outputText.getContext()).activateProgressBar(false);
	        		else if(outputText.getContext().getClass().getCanonicalName().matches(".*\\.DemoAmap.*"))
	        			((DemoAmap) outputText.getContext()).activateProgressBar(false);
	        		else
	        			((ShowBus) outputText.getContext()).activateProgressBar(false); 
	 				Intent intent = new Intent(outputText.getContext(), DemoBusInfo.class);
	 				intent.putExtra("busInfo", result);
	 				intent.putExtra("bus", bus);
	 				SharedPreferences sharedPref =  ((Activity) outputText.getContext()).getSharedPreferences("busInfoVariables",Context.MODE_PRIVATE);
                	SharedPreferences.Editor editor = sharedPref.edit();
                	editor.putString("busInfo", result);
                	editor.putString("bus", bus);
                	editor.commit();
	 				Log.v("testing","going in to busInfo activity");
	 				outputText.getContext().startActivity(intent);
	        	 }else if(method==3){
	        		 ((Amap) outputText.getContext()).activateProgressBar(false); 
					Intent intent = new Intent(outputText.getContext(), ShowBus.class);
					intent.putExtra("buslist_str", result);
					intent.putExtra("method", 3);
					Log.v("testing","going in to ShowBus activity");
					outputText.getContext().startActivity(intent);
	        	 }else{
	        		 ((TextView) outputText).setText("Sorry! Wrong method.");	
	        	 }
	        }
	    }

}
