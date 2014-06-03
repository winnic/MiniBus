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

public class DemoBusInfo extends FragmentActivity implements
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
			tmp1.setText(engName[i]);
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
//		bookInfo="®üÅòµó¡AºÖ¨Óh¥Ã®õ¼Ó;;Fuk Loi Estate Wing Tai House;;0;;22.3731;;114.114;;{}Ä_¥Û¤j·H;;Bo Shek Mansion;;0;;22.3687;;114.118;;{}¯þºaµó;;Tsuen Wing Street;;0;;22.3684;;114.121;;{}¦ò±ÐªL¬±ª¢¬ö©À¾Ç®Õ;;Buddhist Lam Bing Yim Memorial School;;0;;22.3696;;114.123;;{}¸ª½å­b;;Kwai Yin Court;;0;;22.37;;114.125;;{}¤jºÛ¤fh´I¶h¼Ó;;Tai Wo Hau Estate Fu Yat House;;0;;22.369;;114.125;;{}¤jºÛ¤fh´I¦w¼Ó;;Tai Wo Hau Estate Fu On House;;0;;22.3681;;114.124;;{}¤jºÛ¤fh´I¶Q¼Ó;;Tai Wo Hau Estate Fu Kwai House;;0;;22.3675;;114.125;;{}¤¤µØ°ò·þ±Ð·|¥þ§¹²Ä¤G¤p¾Ç;;The Church Of Christ In China Chuen Yuen Secondary Primary School;;0;;22.3682;;114.126;;{}¸ª¯F°Ó³õ;;Wonderland Villas Commerical Complex Car Park;;0;;22.3677;;114.127;;{}°·±dµó;;Kin Hong Street;;0;;22.3703;;114.13;;{}¥ú½÷³ò;;Kwong Fai Circuit;;0;;22.3683;;114.133;;{}¸ª¿³h;;Kwai Hing Estate Hing Kok House;;0;;22.3669;;114.132;;{}¸ª«T­b;;Kwai Chun Court;;0;;22.3646;;114.132;;{}´äÅK¸ª¿³¯¸;;MTR Kwai Hing Station;;0;;22.3635;;114.131;;{}¸ª¦w¤u¼t¤j·H;;Kwai On Factory Estate;;0;;22.3607;;114.133;;{}ª@®®©~¡@¡@¡@¡@¡@¡@¡@¡@¡@¡@[ ªl´º»O ];;Liberte Car Park;;0;;22.3346;;114.149;;{}²`¤ô–õ¹B°Ê³õ;;Sham Shui Po Sports Ground;;0;;22.3373;;114.151;;{}­»´ä±M·~±Ð¨|¾Ç°|(¶À§J¹¸);;Hong Kong Institute of Vocational Education (Haking Wong Campus);;0;;22.3354;;114.152;;{}µo²»µó;;Fat Tseung Street;;0;;22.336;;114.155;;{}ÄR»Õh;;Lai Kok Estate;;0;;22.3322;;114.157;;{}´Ü¦{µó¡@¡@¡@¡@¡@¡@¡@¡@[ ¦è¤EÀs¤¤¤ß ];;Yen Chow Street;;0;;22.3309;;114.16;;{}¥_ªeµó;;Pei Ho Street;;0;;22.3299;;114.162;;{}«n©÷µó;;Nam Cheong Street;;0;;22.3329;;114.167;;{}¬É­­µó;;Boundary Street;;0;;22.3267;;114.174;;{}¾~Äõµó¡@¡@¡@¡@¡@¡@¡@¡@¡@[ ©l³Ð¤¤¤ß ];;Arran Street;;0;;22.3227;;114.167;;{}§Ö´Iµó;;Fife Street;;0;;22.3203;;114.169;;{}# ®Ô»¨§{;;Langham Place Car Park;;0;;22.3176;;114.169;;{}³aªoµó¡@¡@¡@¡@¡@¡@¡@¡@¡@[ ³Ð¿³¼s³õ ];;Soy Street;;0;;22.3166;;114.17;;{}µn¥´¤hµó;;Dundas Street;;0;;22.3153;;114.17;;{}ºÛ¥´¦Ñ¹D;;Waterloo Road;;0;;22.3282;;114.178;;{}¥Ã¬P¨½;;Wing Sing Lane;;0;;22.3108;;114.171;;{}¥ÌµÂµó;;Kansu Street;;0;;22.3088;;114.17;;{}¹çªiµó¡A¹çªiµó¡þ¤W®üµó¥ð¾Í¤½¶é;;;;0;;22.3964;;114.109;;{}";
//		busNum_S_E="RED_404&S_E=0;;11".split("&S_E=");
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
	  	    speakOut("­ø¸Ó¥q¾÷,, ¤U­Ó¯¸¦³¸¨.");
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

        (new DemoBusInfo.GetAddressTask(this)).execute(location);

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