package com.example.minibus;

import util.FuncLib;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;




public class Path extends SherlockActivity implements LocationListener{
	static LatLng mapCenter = new LatLng(22.3964,114.109);
    private String[] chiName;
	private LatLng[] stopsLatLng;
	private int[] S_E=new int[2];
	private Marker origin;
	private Marker destination;
	private int onclickedStopNum;
	private PolylineOptions rectLine = new PolylineOptions().width(5).color(R.color.flatui_green);
	private View mapView;	
	private GoogleMap map;
	//////////////////
    private LocationManager locationManager;
    private String provider;
    private Marker currentLoc;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        getBusInfo();
        setContentView(R.layout.map);
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

        if (getIntent().getExtras() != null) {
        	String[] tmp=(String[]) getIntent().getExtras().getSerializable("S_E");
        	S_E[0]=Integer.valueOf(tmp[0]);
        	S_E[1]=Integer.valueOf(tmp[1]);
        	onclickedStopNum=getIntent().getExtras().getInt("onclickedStopNum");
        	mapCenter=stopsLatLng[onclickedStopNum];
		}

        for(int i=0;i<stopsLatLng.length;i++){
        	if(i==S_E[0]){
        		origin=map.addMarker(new MarkerOptions().position(stopsLatLng[i]).title(i+"."+chiName[i]).icon(BitmapDescriptorFactory.fromResource(R.drawable.o)));
        		rectLine.add(stopsLatLng[i]);
        	}else if(i==S_E[1]){
        		destination=map.addMarker(new MarkerOptions().position(stopsLatLng[i]).title(i+"."+chiName[i]).icon(BitmapDescriptorFactory.fromResource(R.drawable.d)));
        		rectLine.add(stopsLatLng[i]);
        	}
        	else if(i==onclickedStopNum){
        		map.addMarker(new MarkerOptions().position(stopsLatLng[i]).title(i+"."+chiName[i]).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).alpha(0.5f)).showInfoWindow();
        		rectLine.add(stopsLatLng[i]);
        	}
        	else if(i>S_E[0]&&i<S_E[1]){
        		map.addMarker(new MarkerOptions().position(stopsLatLng[i]).title(i+"."+chiName[i]).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).alpha(0.5f));
        		rectLine.add(stopsLatLng[i]);
        	}
        	else{
        		map.addMarker(new MarkerOptions().position(stopsLatLng[i]).title(i+"."+chiName[i]).alpha(0.5f));
        	}
        }
    	Polyline polyline = map.addPolyline(rectLine);

    	
        mapView = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getView();
        if (mapView.getViewTreeObserver().isAlive()) {
            mapView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                private LatLngBounds.Builder builder= new LatLngBounds.Builder();

				@Override
                public void onGlobalLayout() {
                    mapView.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                    for(int i=S_E[0];i<=S_E[1];i++){
                    	builder.include(stopsLatLng[i]);
                    }
                    LatLngBounds bounds = builder.build();
                    int padding = 100; // offset from edges of the map in pixels
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                    map.moveCamera(cu);
                    map.animateCamera(cu);
                }
            });
        }
        
        
        trackCurrentLocOnMap();
    }
	
	private void getBusInfo() {
        String Stops[] = FuncLib.loadArray("Stops", this, "busInfoVariables");
        Log.v("testing", "Stops[0]="+Stops[0]);
        chiName=new String[Stops.length];
        stopsLatLng=new LatLng[Stops.length];
		for (int i=0;i<Stops.length;i++){
			String[] cols=Stops[i].split(";;");
			chiName[i]=cols[0];			
			stopsLatLng[i]=new LatLng(Double.parseDouble(cols[3]),Double.parseDouble(cols[4]));
		}
	}
	
///////////////////
/////////////////// trackCurrentLocOnMap
///////////////////
	private void trackCurrentLocOnMap() {
		map.setMyLocationEnabled(true);
        
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean enabledGPS = service
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean enabledWiFi = service
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        // Check if enabled and if not send user to the GSP settings
        // Better solution would be to display a dialog and suggesting to 
        // go to the settings
        if (!enabledGPS) {
            Toast.makeText(this, "GPS signal not found", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Define the criteria how to select the locatioin provider -> use
        // default
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        Location location = locationManager.getLastKnownLocation(provider);

        // Initialize the location fields
        if (location != null) {
            Toast.makeText(this, "Selected Provider " + provider,
                    Toast.LENGTH_SHORT).show();
            currentLoc= map.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude()))
            		.icon(BitmapDescriptorFactory.fromResource(R.drawable.redmini)));
            onLocationChanged(location);
        } else {

            //do something
        }

	}
	
	 /* Request updates at startup */
    @Override
    protected void onResume() {
        super.onResume();
        locationManager.requestLocationUpdates(provider, 400, 1, this);
    }

    /* Remove the locationlistener updates when Activity is paused */
    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        double lat =  location.getLatitude();
        double lng = location.getLongitude();
        Toast.makeText(this, "Location " + lat+","+lng,
                Toast.LENGTH_LONG).show();
        LatLng coordinate = new LatLng(lat, lng);
        Toast.makeText(this, "Location " + coordinate.latitude+","+coordinate.longitude,
                Toast.LENGTH_LONG).show();
        currentLoc.setPosition(coordinate);
//        currentLoc= map.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude()))
//        		.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher)));
    }


    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, "Enabled new provider " + provider,
                Toast.LENGTH_SHORT).show();

    }


    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(this, "Disabled provider " + provider,
                Toast.LENGTH_SHORT).show();

    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

}
