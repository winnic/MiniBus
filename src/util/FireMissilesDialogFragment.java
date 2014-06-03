package util;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.minibus.Amap;
import com.example.minibus.DemoAmap;
import com.example.minibus.BusInfo;
import com.example.minibus.DemoBusInfo;
import com.example.minibus.DemoBusInfo2;
import com.example.minibus.DemoBusInfo3;
import com.google.android.gms.maps.model.LatLng;

public class FireMissilesDialogFragment extends DialogFragment {
	String type=null;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        
        
        
        if((type=getArguments().getString("type"))!= null){
    		if(type=="connectionErr"){
    			builder.setTitle(getArguments().getString("err"));
    			builder.setMessage("Please input the server IP address");
    			// Set up the input
    	        final EditText input = new EditText(getActivity());
    	        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
    	        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER);
    	        builder.setView(input);
    	        
    			builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) { 
                    	final Pattern IP_ADDRESS = Pattern.compile(
                            "((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(25[0-5]|2[0-4]"
                            + "[0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]"
                            + "[0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}"
                            + "|[1-9][0-9]|[0-9]))");
	                    Matcher matcher = IP_ADDRESS.matcher(input.getText().toString());
	                    
	                    if (matcher.matches()||input.getText().toString().equals("localhost")) {
	                    	SharedPreferences sharedPref = getActivity().getSharedPreferences("IP",Context.MODE_PRIVATE);
	                    	SharedPreferences.Editor editor = sharedPref.edit();
	                    	editor.putString("ip", input.getText().toString());
	                    	editor.commit();
	                    }

                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
    		}else if(type=="voiceOutDestination"){
    			builder.setTitle("Arrived!");
    			Log.v("testing",getArguments().getString("destination"));
    			 final TextView input = new TextView(getActivity());
    			 input.setText("Calling out in chinese: Take off at \n\n \t\t\t\t"+ getArguments().getString("destination")+"\n");
    			 input.setPadding(20, 10, 10, 0);
    			builder.setView(input);
    			builder.setCancelable(false);
    			builder.setPositiveButton("Call out", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) { 
            		    if( getActivity().getClass().getCanonicalName().matches(".*\\.BusInfo.*"))
            		    	((BusInfo) getActivity()).speakOut("唔該司機,, 下個站有落.");
            		    else if( getActivity().getClass().getCanonicalName().matches(".*\\.DemoBusInfo2.*")){ 
            		    	((DemoBusInfo2) getActivity()).speakOut("唔該司機,, 下個站有落.");	
            		    }else if( getActivity().getClass().getCanonicalName().matches(".*\\.DemoBusInfo3.*")){
            		    	((DemoBusInfo3) getActivity()).speakOut("唔該司機,, 下個站有落.");	
            		    }          		    
            		    else if( getActivity().getClass().getCanonicalName().matches(".*\\.DemoBusInfo.*"))
            		    	((DemoBusInfo) getActivity()).speakOut("唔該司機,, 下個站有落.");	
            		    else{
            		    	return;
            		    }
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
    		}else if(type=="addressInputs"){
    	        final EditText o = new EditText(getActivity());
    	        final EditText d = new EditText(getActivity());
    	        final LinearLayout l = new LinearLayout(getActivity());
    	        l.setOrientation(LinearLayout.VERTICAL);
    	        l.addView(o);
    	        l.addView(d);
    	        o.setHint("Choose starting point");
    	        d.setHint("Choose destination point");
    	        o.setInputType(InputType.TYPE_CLASS_TEXT);
    	        d.setInputType(InputType.TYPE_CLASS_TEXT);
    	        builder.setView(l);
    	        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) { 
                    	Toast.makeText(getActivity(), "From : " +o.getText().toString()+" to "+d.getText().toString(),
                    			  Toast.LENGTH_LONG).show();
                        ((DemoAmap) getActivity()).setProgressBarIndeterminateVisibility(true);
                        
//                        (new GetAddressTask(getActivity())).execute("hong kong mong kok station");
                        (new GetAddressTask(getActivity(),"o")).execute(o.getText().toString());
                        (new GetAddressTask(getActivity(),"d")).execute(d.getText().toString());
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
    		}
        }else{
            builder.setMessage("dialog_fire_missiles")
            .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // FIRE ZE MISSILES!
                }
            })
            .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                }
            });
     // Create the AlertDialog object and return it
        }

        return builder.create();
    }
    
    
    protected class GetAddressTask extends AsyncTask<String, Void, JSONObject> {
        // Store the context passed to the AsyncTask when the system instantiates it.
        Context localContext;
        String O_D=null;

        // Constructor called by the system to instantiate the task
        public GetAddressTask(Context context,String O_D) {
            // Required by the semantics of AsyncTask
            super();
            // Set a Context for the background task
            localContext = context;
            this.O_D=O_D;
        }

        /**
         * Get a geocoding service instance, pass latitude and longitude to it, format the returned
         * address, and return the address to the UI thread.
         */
        @Override
        protected JSONObject doInBackground(String... locations) {
        	Log.v("testing","dialog GetAddressTask doInBackground");
        	StringBuilder stringBuilder = new StringBuilder();
            try {

            String address = locations[0].replaceAll(" ","%20");    

            HttpPost httppost = new HttpPost("http://maps.google.com/maps/api/geocode/json?address=" + address + "&sensor=false");
            HttpClient client = new DefaultHttpClient();
            HttpResponse response;
            stringBuilder = new StringBuilder();


                response = client.execute(httppost);
                HttpEntity entity = response.getEntity();
                InputStream stream = entity.getContent();
                int b;
                while ((b = stream.read()) != -1) {
                    stringBuilder.append((char) b);
                }
            } catch (ClientProtocolException e) {
            } catch (IOException e) {
            }

            JSONObject jsonObject = new JSONObject();
            Log.v("testing", "stringBuilder.toString()"+stringBuilder.toString());
            try {
                jsonObject = new JSONObject(stringBuilder.toString());
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return jsonObject;
        }

        /**
         * A method that's called once doInBackground() completes. Set the text of the
         * UI element that displays the address. This method runs on the UI thread.
         */
        @Override
        protected void onPostExecute(JSONObject jsonObject) {
        	JSONObject ret = jsonObject; 
        	JSONObject location;
        	Double lat = null;
        	Double lng = null;

        	try {
        		if(ret.get("status").equals("ZERO_RESULTS")){
               	 	Toast.makeText(localContext, "Sorry No Red MiniBus matched nearby", Toast.LENGTH_LONG).show();
               	 	return;
        		}
        	    location = ret.getJSONArray("results").getJSONObject(0);
        	    lat= location.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
        	    lng = ret.getJSONArray("results").getJSONObject(0)
                        .getJSONObject("geometry").getJSONObject("location")
                        .getDouble("lng");
        	} catch (JSONException e1) {
        	    e1.printStackTrace();
        	}
        	Log.v("testing", "lat : " +Double.toString(lat)+" ; lng : "+Double.toString(lng));
        	Toast.makeText(localContext, "lat : " +Double.toString(lat)+" ; lng : "+Double.toString(lng),
      			  Toast.LENGTH_LONG).show();
        	((DemoAmap) localContext).setProgressBarIndeterminateVisibility(false);
        	if(O_D.equals("o"))
        		((DemoAmap) localContext).origin.setPosition(new LatLng(lat,lng));
        	else if(O_D.equals("d")){
        		((DemoAmap) localContext).destination.setPosition(new LatLng(lat,lng));
        		((DemoAmap) localContext).mapCameraToFitScreen();
        	}
        }
    }
}