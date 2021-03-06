package com.example.minibus;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONObject;

import util.FuncLib;
import util.webService;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class Amap extends SherlockActivity implements OnMarkerDragListener{
    static final LatLng HK = new LatLng(22.3964,114.109);
    private GoogleMap mMap;
//    private LatLng o= new LatLng(22.2704,114.236);
//    private LatLng d= new LatLng(22.2687,114.242);
    private LatLng o= new LatLng(22.318814357800562,114.17217057198286);
    private LatLng d= new LatLng(22.25324098853706,114.13960587233305);
    private LatLngBounds.Builder builder = new LatLngBounds.Builder();
    private View mapView;
    //////////////////
    private String[] busList;
    private String[] S_E;
    private String[] busLatLngs;
    private String ip="localhost";
    private final float[] colors={0.0f,30.0f,60.0f,120.0f,180.0f,210.0f,240.0f,270.0f,300.0f,330.0f};
	private Marker origin;
	private Marker destination;
	private Activity a;
	TextView v;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //!!progress dialog
     	requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
     	setSupportProgressBarIndeterminate(true);
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
        a=this;
        
        mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        
        origin = mMap.addMarker(new MarkerOptions()
                                  .position(o)
                                  .draggable(true)
                                  .title("Origin"));
        destination = mMap.addMarker(new MarkerOptions()
							        .position(d)
							        .draggable(true)
							        .title("Destintion"));
        
        mapView = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getView();
        if (mapView.getViewTreeObserver().isAlive()) {
            mapView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    // remove the listener
                    // ! before Jelly Bean:
                    mapView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    // ! for Jelly Bean and later:
                    //mapView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//                    // set map viewport
//                    // CENTER is LatLng object with the center of the map
//                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(origin.getPosition(), 12));
//                    // ! you can query Projection object here
//                    Point markerScreenPosition = mMap.getProjection().toScreenLocation(origin.getPosition());
//                    // ! example output in my test code: (356, 483)
//                    System.out.println(markerScreenPosition);
                    
                    origin.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.o));
                    destination.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.d));
                    builder.include(origin.getPosition());
                    builder.include(destination.getPosition());
                    LatLngBounds bounds = builder.build();
                    int padding = 150; // offset from edges of the map in pixels
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                    // Move the camera instantly to hamburg with a zoom of 15.
                    mMap.moveCamera(cu);

                    // Zoom in, animating the camera.
                    mMap.animateCamera(cu);
                }
            });
        }
        
        mMap.setOnMarkerDragListener(this);
        v=new TextView(this);
        mMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
        @Override
        public void onInfoWindowClick(Marker marker) {
        	activateProgressBar(true);
    		webService connectToUrl=new webService();
    		int pos=0;
    		for(int i=0;i<busList.length;i++) 
    			if(busList[i].equals(marker.getTitle())){
    				pos=i;
    			}
    		String url="/redmini/api.php?action=fetch_bus&bus="+busList[pos]+"&S_E="+S_E[pos];
    		Log.v("testing","onBookPressed pos = "+pos+" && url = "+url);
    		connectToUrl.contextStartBrowerTo_URL(getBaseContext(),v,url,2);
        }
    });
    }
    
	@Override
	public void onMarkerDragEnd(Marker m) {
		if(m.getTitle().equals("Origin")){
			o = m.getPosition();
		}else{
			d = m.getPosition();
		}
		Log.v("testing", o.toString()+" & " + d.toString());		
	}
	@Override
	public void onMarkerDragStart(Marker m) {
	}
    
	@Override
	public void onMarkerDrag(Marker m) {
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	//!!
        menu.add("Search")
            .setIcon(R.drawable.ic_search)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
     	
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
    	TextView output=new TextView(this);
        if(item.getTitle()=="Search") {
			String url="/redmini/api.php?action=theNearest&" +
					"spLat="+o.latitude+"&" +
					"spLng="+o.longitude+"&" +
					"dpLat="+d.latitude+"&" +
					"dpLng="+d.longitude;
			activateProgressBar(true);
//        	webService connectToUrl=new webService();
//			connectToUrl.contextStartBrowerTo_URL(this.getBaseContext(),output,url,3);
			SharedPreferences sharedPref = getSharedPreferences("IP",Context.MODE_WORLD_READABLE);
			if(sharedPref.getString("ip", null)!=null){
				ip = sharedPref.getString("ip",null);
				Log.v("testing",ip);
			}
			new Web(this).execute("http://"+ip+url);
        	return true;
        }else{
        	return super.onOptionsItemSelected(item);
        }
    }

	public void activateProgressBar(boolean activate){
	    setSupportProgressBarIndeterminateVisibility(activate);
	}
	
	private class Web extends AsyncTask<String, Void, String>{
		Context localContext;
		
		public Web(Context context) {
            // Required by the semantics of AsyncTask
            super();
            // Set a Context for the background task
            localContext = context;
        }
		@Override
		protected String doInBackground(String... urls) {Log.v("testing", "url= "+urls[0]);
			String response="";
       		HttpParams httpParameters = new BasicHttpParams();
       		HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);
    		HttpConnectionParams.setSoTimeout(httpParameters, 10000);
			HttpClient Client = new DefaultHttpClient(httpParameters);		
			HttpGet httpget = new HttpGet(urls[0]);
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			try {
				response = Client.execute(httpget, responseHandler);
			} catch (ClientProtocolException e) {
				return e.getMessage();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				return e.getMessage();
			}
			return response;
		}

		protected void onPostExecute(String response) {
			if(!FuncLib.connSuccess(response,a)){
				return;
			}
			activateProgressBar(false);
			if(response.equals("[]")){
				 Toast.makeText(localContext, "Sorry No Red MiniBus matched nearby", Toast.LENGTH_LONG).show();
				 return;
			}
			try {
				  JSONObject jsonObject = new JSONObject(response);
				  Iterator<Object> keys = jsonObject.keys();
				  busList=new String[jsonObject.length()];
				  busLatLngs=new String[jsonObject.length()];
				  S_E=new String[jsonObject.length()];
				  for(int i=0;keys.hasNext();i++) {
					  busList[i]=String.valueOf(keys.next());
					  JSONObject key=(JSONObject)jsonObject.get(busList[i].toString());
					  busLatLngs[i]=Double.toString((Double)key.get("takeUpLat"))+";;"+Double.toString((Double)key.get("takeUpLng"))+";;"+Double.toString((Double)key.get("takeOffLat"))+";;"+Double.toString((Double)key.get("takeOffLng"));
//			        	Log.w("testing","key = "+busList[i].toString());
					  S_E[i]=(String) key.get("takeUp")+";;"+(String) key.get("takeOff");
				  }
		    } catch (Exception e) {
		      e.printStackTrace();
		    }
			addBusMarkers();
			origin.setDraggable(false);
			origin.setTitle(null);
			Bitmap b=BitmapFactory.decodeResource(getResources(), R.drawable.o);
			Bitmap bhalfsize=Bitmap.createScaledBitmap(b, b.getWidth()*5/7,b.getHeight()*5/7, false);
			origin.setIcon(BitmapDescriptorFactory.fromBitmap(bhalfsize));
			destination.setTitle(null);
			destination.setDraggable(false);
			b=BitmapFactory.decodeResource(getResources(), R.drawable.d);
			bhalfsize=Bitmap.createScaledBitmap(b, b.getWidth()*5/7,b.getHeight()*5/7, false);
			destination.setIcon(BitmapDescriptorFactory.fromBitmap(bhalfsize));
			mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(origin.getPosition(), 18), 5000, null);
			
		}

		private void addBusMarkers() {
			Set<String> uniquUsers = new HashSet<String>();

	        for (int i = 0; i < busLatLngs.length; i++) {
	            if (!uniquUsers.add(busLatLngs[i]))
	            	busLatLngs[i] = "Duplicate"; // here I am assigning Duplicate instead if find duplicate
	        }
			
			LatLng markerTmp = new LatLng(0,0);
			for(int i=0;i<busLatLngs.length;i++) {
				Log.v("testing", busLatLngs[i]);
				if("Duplicate".equals(busLatLngs[i])){
					continue;
				}
				String[] tmp=busLatLngs[i].split(";;");
				markerTmp = new LatLng(Double.parseDouble(tmp[0]),Double.parseDouble(tmp[1]));
				mMap.addMarker(new MarkerOptions().position(markerTmp).title(busList[i]).alpha(0.7f)
						.icon(BitmapDescriptorFactory.defaultMarker(colors[i%busLatLngs.length])));
				markerTmp = new LatLng(Double.parseDouble(tmp[2]),Double.parseDouble(tmp[3]));
				mMap.addMarker(new MarkerOptions().position(markerTmp).title(busList[i]).alpha(0.7f)
						.icon(BitmapDescriptorFactory.defaultMarker(colors[i%busLatLngs.length])));
							
			}
		}
	}
}