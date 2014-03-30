package com.example.minibus;

import util.webService;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.actionbarsherlock.ActionBarSherlock;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class Amap extends SherlockActivity implements OnMarkerDragListener{
    static final LatLng HK = new LatLng(22.3964,114.109);
    private GoogleMap mMap;
    private LatLng o= new LatLng(22.3964,114.109);
    private LatLng d= new LatLng(22.3964,114.109);
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //!!progress dialog
     	requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
     	setSupportProgressBarIndeterminate(true);
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
        
        mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        
        Marker origin = mMap.addMarker(new MarkerOptions()
                                  .position(HK)
                                  .draggable(true)
                                  .title("Origin"));
        
        // Move the camera instantly to hamburg with a zoom of 15.
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(HK, 15));

        // Zoom in, animating the camera.
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11), 500, null);
        
        mMap.setOnMarkerDragListener(this);
        
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
			String url="/redmini/api.php?action=theNearest&lat=0&lng=0";
			activateProgressBar(true);
        	webService connectToUrl=new webService();
			connectToUrl.contextStartBrowerTo_URL(this.getBaseContext(),output,url,3);
        	return true;
        }else{
        	return super.onOptionsItemSelected(item);
        }
    }

	public void activateProgressBar(boolean activate){
	    setSupportProgressBarIndeterminateVisibility(activate);
	}
}