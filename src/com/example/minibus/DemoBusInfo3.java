package com.example.minibus;

import java.io.IOException;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import util.FireMissilesDialogFragment;
import util.FuncLib;
import util.LocationUtils;
import util.onClickListenerWithParam;
import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.ActionBarSherlock;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.LatLng;

public class DemoBusInfo3 extends FragmentActivity implements
LocationListener,
TextToSpeech.OnInitListener {
	private TextToSpeech tts;
	
	private String[] Stops ;
	private String[] chiName;
	private String[] engName;
	private String[] price;
	private Location[] stopsLatLng;
	private String[] S_E;
	private int numStops;
	private double minD=999999999;
	private int lastPos=0;
	//private static int tmpA=0;
	
//	private boolean mIsRunning = true;
	
	private GradientDrawable bgShape;
	//////////////////////////////////////
//	private LocationRequest mLocationRequest;
//    private LocationClient mLocationClient;
    boolean mUpdatesRequested = false;
    SharedPreferences mPrefs;
    SharedPreferences.Editor mEditor;
	///////////////////////////////////////
    private TextView currentLoc_tag;
    private ActionBarSherlock mSherlock;
    public final ActionBarSherlock getSherlock() {
        if (mSherlock == null) {
            mSherlock = ActionBarSherlock.wrap(this, ActionBarSherlock.FLAG_DELEGATE);
        }
        return mSherlock;
    }


	private boolean expand=false;

	private LocationManager locationManager;

	private String provider;

	
	public void onCreate(Bundle savedInstanceState) {
		Log.v("testing","BusInfo onCreate");
		// progress dialog
		getSherlock().requestFeature((int)Window.FEATURE_INDETERMINATE_PROGRESS);
		getSherlock().setProgressBarIndeterminate(true);
		getBusInfo();
				
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bus_info);
		tts = new TextToSpeech(this, this);
		//set basic info
		setBusInfo();

		TableLayout t1 = (TableLayout)findViewById(R.id.table1);
		currentLoc_tag= (TextView)((TableRow)t1.getChildAt(0)).getChildAt(1);
		
		trackCurrentLoc();
	}

	private void setBusInfo() {			
		bgShape=(GradientDrawable) getResources().getDrawable(R.drawable.roundedtr);
//		Log.v("testing","setBusInfo");
//		TextView SP=(TextView)findViewById(R.id.SP);
//		TextView DP=(TextView)findViewById(R.id.DP);
//		SP.setText(chiName[Integer.valueOf(S_E[0])]);
//		DP.setText(chiName[Integer.valueOf(S_E[1])]);
		//tmpA=Integer.valueOf(S_E[0]);
		
		final TableLayout t2=(TableLayout)findViewById(R.id.table2);

		for (int i=0;i<numStops;i++){
			TextView tmp1=new TextView(this);
			TextView tmp2=new TextView(this);
			tmp1.setText(chiName[i]);
//			tmp2.setText(price[i]);
			if(i<(Integer.valueOf(S_E[0]))||i>(Integer.valueOf(S_E[1]))){
				tmp1.setLayoutParams(new TableRow.LayoutParams(0, 0, 3));
				tmp2.setLayoutParams(new TableRow.LayoutParams(0, 0, 1));
			}else{
				tmp1.setLayoutParams(new TableRow.LayoutParams(0, 60, 3));
				tmp2.setLayoutParams(new TableRow.LayoutParams(0, 60, 1));
			}

			TableRow row=new TableRow(this);
			row.addView(tmp1);
			row.addView(tmp2);
//			setLocationOnClick(row);
			t2.addView(row);

			if(i==(Integer.valueOf(S_E[0]))){
				bgShape.setColor(0xbbf1c40f);
				row.setBackgroundResource(R.drawable.roundedtr);	
			}
			else if(i==(Integer.valueOf(S_E[1]))){
				row.setBackgroundResource(R.drawable.roundedtr);
				GradientDrawable bgShape = (GradientDrawable)row.getBackground();
				bgShape.setColor(0xaae74c3c);
			}
			else if(i>(Integer.valueOf(S_E[0]))&&i<(Integer.valueOf(S_E[1]))){
//				bgShape.setColor(0xFF58BAED);
				bgShape.setColor(0x4458BAED);
				row.setBackgroundResource(R.drawable.roundedtr);
			}
			
			row.setOnClickListener(new onClickListenerWithParam(i){
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(v.getContext(), Path.class);
					intent.putExtra("onclickedStopNum", this.onclickedStopNum);
					intent.putExtra("S_E", S_E);
					startActivity(intent);
				}	
			});
		}
		
		
		RelativeLayout expandOrSimplify=(RelativeLayout)findViewById(R.id.expandOrSimplify);
		expandOrSimplify.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				ImageView expandOrSimplify_img=(ImageView)findViewById(R.id.expandOrSimplify_img);
				if(!expand){
					for (int i=0;i<Integer.valueOf(S_E[0]);i++){
						((TextView)((TableRow)t2.getChildAt(i)).getChildAt(0)).setLayoutParams(new TableRow.LayoutParams(0, 50, 3));
						((TextView)((TableRow)t2.getChildAt(i)).getChildAt(1)).setLayoutParams(new TableRow.LayoutParams(0, 50, 1));
					}
					for (int i=Integer.valueOf(S_E[1])+1;i<numStops;i++){
						((TextView)((TableRow)t2.getChildAt(i)).getChildAt(0)).setLayoutParams(new TableRow.LayoutParams(0, 50, 3));
						((TextView)((TableRow)t2.getChildAt(i)).getChildAt(1)).setLayoutParams(new TableRow.LayoutParams(0, 50, 1));
					}
					expandOrSimplify_img.setImageResource(R.drawable.simplify);
					expand=true;
				}else{
					for (int i=0;i<Integer.valueOf(S_E[0]);i++){
						((TextView)((TableRow)t2.getChildAt(i)).getChildAt(0)).setLayoutParams(new TableRow.LayoutParams(0, 0, 3));
						((TextView)((TableRow)t2.getChildAt(i)).getChildAt(1)).setLayoutParams(new TableRow.LayoutParams(0, 0, 1));
					}
					for (int i=Integer.valueOf(S_E[1])+1;i<numStops;i++){
						((TextView)((TableRow)t2.getChildAt(i)).getChildAt(0)).setLayoutParams(new TableRow.LayoutParams(0, 0, 3));
						((TextView)((TableRow)t2.getChildAt(i)).getChildAt(1)).setLayoutParams(new TableRow.LayoutParams(0, 0, 1));
					}
					expandOrSimplify_img.setImageResource(R.drawable.expand);
					expand=false;
				}

			}		
		});
	}

	private void setLocationOnClick(TableRow row) {
		row.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
              try
              {
            	    DialogFragment newFragment = new FireMissilesDialogFragment();
            	    newFragment.show(getFragmentManager(), "missiles");
//            	  Dialog dialog = new Dialog(getBaseContext());
//            	  TextView txt = (TextView)dialog.findViewById(R.id.textbox);
            	  Log.v("testing","setLocationOnClick onLongClick");
//          		Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
//          		startActivity(intent);
              }catch(Exception e){
                  return false;
              }
                return false;
            }
        });
	}

	private void getBusInfo() {
		Log.v("testing","getBusInfo");
		String[] busNum_S_E = null;
		String bookInfo = null;
		if (getIntent().getExtras() != null) {
			busNum_S_E=getIntent().getExtras().getString("bus").split("&S_E=");
			bookInfo= getIntent().getExtras().getString("busInfo");
		}else{
			//for testing
			 SharedPreferences sharedPref =this.getSharedPreferences("busInfoVariables",Context.MODE_WORLD_READABLE);
			 if(sharedPref.getString("busInfo", null)!=null){
				 bookInfo = sharedPref.getString("busInfo",null);
			 }
			 Log.v("testing","bookInfo = "+ bookInfo);
			 if(sharedPref.getString("bus", null)!=null){
				 busNum_S_E = sharedPref.getString("bus",null).split("&S_E=");
			 }
			 Log.v("testing","busNum_S_E = "+ sharedPref.getString("bus",null));
		}
/////////////////only for testing
		busNum_S_E="RED_287&S_E=0;;2".split("&S_E=");
//		bookInfo="®Ô»¨§{¤½¦@¤p¤ÚÁ`¯¸;;;;0;;22.3186;;114.169;;{}¯ïªK¨¤Âå°|;;Kwai Chung Hospital Road;;0;;22.3424;;114.133;;{}µØ­ûh;;Wah Yuen Chuen;;0;;22.3492;;114.135;;{}¹l¶®°a;;The Apex;;0;;22.3662;;114.137;;{}¸ª¯Fªá¶é;;Kwai Chung;;0;;22.3695;;114.14;;{}¤W¸ª¯F©x¥ß¤¤¾Ç;;Kwai Chung;;0;;22.3665;;114.142;;{}¥ÛÆXh11®y;;Shek Lei (ii) Estate Block 11;;0;;22.3658;;114.141;;{}¥Û±ùµó¡A¥ÛÆX°Ó³õ¤G´Á;;;;0;;22.3659;;114.139;;{}";
		bookInfo="¦è¬vµæ«nµó¡A©ô¨¤¹D;;Sai Yeung Choi Street South & Mong Kok Road;;0;;22.3208;;114.17;;{}¤E¨{¤s;;Kau To Shan;;0;;22.4113;;114.202;;{}­»´ä¤¤¤å¤j¾Ç;;Chinese University of Hong Kong;;0;;22.4202;;114.207;;{}¼Ì¾ðÅy;;Cheung Shue Tan;;0;;22.4262;;114.201;;{}³À¯ô¤s²ø;;Deerhill Bay;;0;;22.4292;;114.197;;{}¯ïªK§|¡@¡@¡@¡@¡@¡@¡@¡@¡@¡@[ º°ÀÜ¤s ];;Lai Chi Hang;;0;;22.432;;114.18;;{}Å|»A»¨®x¡@¡@¡@¡@¡@¡@¡@¡@[ »B»Aªá¶é ];;Emerald Palace;;0;;22.4392;;114.183;;{}¹l©É¶®­b;;Chaleau Royale;;0;;22.4405;;114.178;;{}¼sºÖh;;Kwong Fuk Estate;;0;;22.4464;;114.173;;{}¹BÀY¨¤¨½;;Wan Tau Kok Lane;;0;;22.4462;;114.169;;{}¹BÀYµó;;Wan Tau Street;;0;;22.4464;;114.168;;{}Ä_¶m¾ô;;Po Heung Bridge;;0;;22.4501;;114.167;;{}¤j®HÂÂ¼V¹C¼Ö³õ;;Tai Po Old Market Playground;;0;;22.4519;;114.166;;{}¤Ó©Mh;;Tai Wo Estate;;0;;22.4524;;114.158;;{}¤j®H¬F©²¦X¸p;;Tai Po Government Offices;;0;;22.451;;114.164;;{}¤j®HÂÂ¼V¹C¼Ö³õ¡@¡@¡@¡@¡@[ ¤K¸¹ªá¶é ];;Tai Po Old Market Playground;;0;;22.4519;;114.166;;{}¤j®H¤¤¤ß;;Tai Po Centre Block 22;;0;;22.45;;114.169;;{}";	
//		bookInfo="¥Û±ùµó¡A¥ÛÆX°Ó³õ¤G´Á;;;;0;;22.3659;;114.139;;{}¥Û¨©µó;;Shek Pui Street;;0;;22.3641;;114.136;;{}¥Û­^®|;;Shek Ying Path;;0;;22.3647;;114.136;;{}¹l¶®°a;;The Apex;;0;;22.3662;;114.137;;{}ÄR´¹¤¤¤ß;;Regent Centre Car Park;;0;;22.3679;;114.138;;{}¥Û©y¸ô;;Shek Yi Road;;0;;22.3684;;114.139;;{}ÂÅ¥Ðµó;;Lam Tin Street;;0;;22.3691;;114.138;;{}¤jºÛ¤fªo¯¸¡@¡@¡@¡@¡@[ ´äÅK¤jºÛ¤f¯¸ ];;Tai Wo Hau Station;;0;;22.3708;;114.125;;{}¯þÆWªá¶é;;Tsuen Wan Garden;;0;;22.37;;114.122;;{}¯þÆW«°¥«¼s³õ;;Tsuen Wan Town Square;;0;;22.3709;;114.117;;{}¯þÆWµó¥«;;Tsuen Wan Market;;0;;22.3714;;114.117;;{}¤tÀsµó¡AÆM¦a§{;;Chuen Lung Street & Hau Tei Square;;0;;22.3706;;114.116;;{}";
		///////////////////only for testing
		S_E= busNum_S_E[1].split(";;");
		setTitle(busNum_S_E[0]);
		Stops=bookInfo.split("\\{\\}");
		numStops=Stops.length;
		chiName = new String[numStops];
		engName = new String[numStops];
		price = new String[numStops];
		stopsLatLng = new Location[numStops];
		for (int i=0;i<numStops;i++){
			String[] cols=Stops[i].split(";;");
			chiName[i]=cols[0];			
			engName[i]=cols[1];
			price[i]=cols[2];
			stopsLatLng[i]= new Location(cols[0]); 
			stopsLatLng[i].setLatitude(Double.parseDouble(cols[3]));
			stopsLatLng[i].setLongitude(Double.parseDouble(cols[4]));
		}
		
		FuncLib.saveArray(Stops,"Stops", this, "busInfoVariables");	
	}

    private void updateDistance(Location currentLocation) {
//    	Log.v("testing","updateDistance");
        Log.e("testing",currentLocation.getLatitude()+"&"+currentLocation.getLongitude());
        TextView tmp = null;
		TableLayout t2=(TableLayout)findViewById(R.id.table2);
		minD=999999999;
		for (int i=0;i<numStops;i++){				
			tmp=(TextView)((TableRow)t2.getChildAt(i)).getChildAt(1);
			double finalValue = Math.round( currentLocation.distanceTo(stopsLatLng[i])/10.0) / 100.0;
			tmp.setText(Double.toString(finalValue));
			lastPos=closestLoc(finalValue,i);
//		Log.d("testing","displacement="+currentLocation.)
		}
		setCurrentColor((TableRow)t2.getChildAt(lastPos));
	}

    private int closestLoc(double finalValue, int pos) {
//    	Log.v("testing","closestLoc");
    	if(finalValue<=minD){
    		minD=finalValue;
    		Log.e("testing","chiName="+chiName[pos]+"; minD = "+minD);
    		return pos;
    	}
    	return lastPos;
	}

	private void setCurrentColor(TableRow tableRow) {
//		Log.v("testing","tmpA & S_E[0] ="+tmpA+" , "+Integer.valueOf(S_E[0]));
		for(int i=Integer.valueOf(S_E[0])+1;i<Integer.valueOf(S_E[1]);i++){
			bgShape = (GradientDrawable)((TableLayout)tableRow.getParent()).getChildAt(i).getBackground();
			if(bgShape!=null)
				bgShape.setColor(0x4458BAED);
		}
		if(lastPos==(Integer.valueOf(S_E[0]))){
//			ColorDrawable bgShape = (ColorDrawable)tableRow.getBackground();
			bgShape = (GradientDrawable)tableRow.getBackground();
			if(bgShape!=null)
				bgShape.setColor(0xaaf1c40f);
		}else if(lastPos==Integer.valueOf(S_E[1])){
			bgShape = (GradientDrawable)tableRow.getBackground();
			if(bgShape!=null)
				bgShape.setColor(0xFF1dd2af);
			Log.v("testing","Arrived!!!");
			popUpArrived();
//        	mIsRunning = false;
//        	mHandler.removeCallbacks(myTask);
		}else{
//			tableRow.setBackgroundResource(R.color.honeycombish_blue);
			bgShape = (GradientDrawable)tableRow.getBackground();
			if(bgShape!=null)
				bgShape.setColor(0x9958BAED);
		}
		
	
	}

	
	@SuppressLint("NewApi")
    public void getAddress(View v, Location location) {
    	Log.v("testing","getAddress");
        // In Gingerbread and later, use Geocoder.isPresent() to see if a geocoder is available.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && !Geocoder.isPresent()) {
            // No geocoder is present. Issue an error message
            Toast.makeText(this, R.string.no_geocoder_available, Toast.LENGTH_LONG).show();
            return;
        }

        getSherlock().setProgressBarIndeterminateVisibility(true);

        (new DemoBusInfo3.GetAddressTask(this)).execute(location);

    }
	
    protected class GetAddressTask extends AsyncTask<Location, Void, String> {
        // Store the context passed to the AsyncTask when the system instantiates it.
        Context localContext;

        // Constructor called by the system to instantiate the task
        public GetAddressTask(Context context) {
            // Required by the semantics of AsyncTask
            super();
            // Set a Context for the background task
            localContext = context;
        }

        /**
         * Get a geocoding service instance, pass latitude and longitude to it, format the returned
         * address, and return the address to the UI thread.
         */
        @Override
        protected String doInBackground(Location... params) {
        	Log.v("testing","GetAddressTask doInBackground");
            Geocoder geocoder = new Geocoder(localContext, Locale.getDefault());

            Location location = params[0];

            List <Address> addresses = null;

            try {
                addresses = geocoder.getFromLocation(location.getLatitude(),
                    location.getLongitude(), 1
                );

                // Catch network or other I/O problems.
                } catch (IOException exception1) {
                    Log.e(LocationUtils.APPTAG, getString(R.string.IO_Exception_getFromLocation));
                    exception1.printStackTrace();
                    return (getString(R.string.IO_Exception_getFromLocation));
                } catch (IllegalArgumentException exception2) {
                    String errorString = getString(
                            R.string.illegal_argument_exception,
                            location.getLatitude(),
                            location.getLongitude()
                    );
                    Log.e(LocationUtils.APPTAG, errorString);
                    exception2.printStackTrace();

                    //
                    return errorString;
                }
                if (addresses != null && addresses.size() > 0) {
//Log.v("testing", addresses.toString());
                    Address address = addresses.get(0);
                    String addressText = getString(R.string.address_output_string,
                            address.getMaxAddressLineIndex() > 0 ?
                                    address.getAddressLine(0) : "",
                            address.getLocality(),
                            address.getCountryName()
                    );
                    return addressText;
                } else {
                  return getString(R.string.no_address_found);
                }
        }

        /**
         * A method that's called once doInBackground() completes. Set the text of the
         * UI element that displays the address. This method runs on the UI thread.
         */
        @Override
        protected void onPostExecute(String address) {
        	Log.v("testing","onPostExecute");
        	getSherlock().setProgressBarIndeterminateVisibility(false);
    		currentLoc_tag.setText(address);
        }
    }
	
///////////////////
/////////////////// trackCurrentLocOnMap
///////////////////
	private void trackCurrentLoc() {
		LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
		boolean enabledGPS = service
		  .isProviderEnabled(LocationManager.GPS_PROVIDER);
		boolean enabledWiFi = service
		  .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

		if (!enabledGPS) {
			Toast.makeText(this, "GPS signal not found", Toast.LENGTH_LONG).show();
			Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivity(intent);
		}

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		Criteria criteria = new Criteria();
		provider = locationManager.getBestProvider(criteria, false);
		Location location = locationManager.getLastKnownLocation(provider);
		
		// Initialize the location fields
		if (location != null) {
			Toast.makeText(this, "Selected Provider " + provider,
			      Toast.LENGTH_SHORT).show();
			
			getAddress(currentLoc_tag,location);
			
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
		Date d=new Date(new Timestamp(System.currentTimeMillis()).getTime());
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        Log.v("testing","lastPos = "+lastPos+" at Time = "+sdf.format(d));
        updateDistance(location);
		
		double lat =  location.getLatitude();
		double lng = location.getLongitude();
		LatLng coordinate = new LatLng(lat, lng);
		Toast.makeText(this, "Location " + coordinate.latitude+","+coordinate.longitude,
		  Toast.LENGTH_LONG).show();
//		currentLoc.setPosition(coordinate);
	}
	
	
	@Override
	public void onProviderDisabled(String provider) {
		Toast.makeText(this, "Enabled new provider " + provider,Toast.LENGTH_SHORT).show();
	}
	
	
	@Override
	public void onProviderEnabled(String provider) {
		Toast.makeText(this, "Disabled provider " + provider,Toast.LENGTH_SHORT).show();	
	}
	
	
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	// TODO Auto-generated method stub
	
	}
	
	////////////////////////////////////TTS
	@Override
	public void onInit(int status) {
		Log.e("testing", "TTS Initilization!");
        if (status == TextToSpeech.SUCCESS) {
        	Locale defaultLang=tts.getLanguage();
        	 Log.e("TTS", "tts.getLanguage() ="+defaultLang.getDisplayName());
        	
            int result = tts.setLanguage(defaultLang);
           
            
 
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            }
 
        } else {
            Log.e("TTS", "Initilization Failed!");
        }
	}
	
	@Override
    public void onDestroy() {
        // Don't forget to shutdown tts!
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
	
	public void speakOut(String text) {
		Log.v("testing","speech = "+text);
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }
	
	public void popUpArrived(){
		DialogFragment F = new FireMissilesDialogFragment();
  		Bundle args = new Bundle();
  		args.putString("type","voiceOutDestination");
  		args.putString("destination", engName[Integer.valueOf(S_E[1])]);
  		F.setArguments(args);
  	    F.show(getFragmentManager(), "missiles");
	}


}