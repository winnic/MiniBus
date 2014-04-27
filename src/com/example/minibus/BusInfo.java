package com.example.minibus;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import util.FireMissilesDialogFragment;
import util.LocationUtils;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.ActionBarSherlock;
import com.actionbarsherlock.view.Window;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

public class BusInfo extends FragmentActivity implements
LocationListener,
GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener {
	private String[] Stops ;
	private String[] chiName;
	private String[] engName;
	private String[] price;
	private Location[] stopsLatLng;
	private String[] S_E;
	private int numStops;
	private double minD=999999999;
	private int lastPos=0;
	private static int tmpA=0;
	
	private boolean mIsRunning = true;
	
	private GradientDrawable bgShape;
	//////////////////////////////////////
	private LocationRequest mLocationRequest;
    private LocationClient mLocationClient;
    boolean mUpdatesRequested = false;
    SharedPreferences mPrefs;
    SharedPreferences.Editor mEditor;
	///////////////////////////////////////
    private TextView currentLoc_tag;
    private ActionBarSherlock mSherlock;
    protected final ActionBarSherlock getSherlock() {
        if (mSherlock == null) {
            mSherlock = ActionBarSherlock.wrap(this, ActionBarSherlock.FLAG_DELEGATE);
        }
        return mSherlock;
    }
    ///////////////
    private Handler mHandler= new Handler();
    Runnable myTask = new Runnable() {
      @Override
      public void run() {
    	  if (!mIsRunning) {
    		  //!!Add a status to your code to stop respawning new tasks:
              return; // stop when told to stop
          }
    	  Log.v("testing","myTask");
    	  getAddress(currentLoc_tag);
//  		startUpdates(currentLoc_tag);
  		  mHandler.postDelayed(myTask,1000);  
      }
    };
	
	public void onCreate(Bundle savedInstanceState) {
		Log.v("testing","BusInfo onCreate");
		// progress dialog
		getSherlock().requestFeature((int)Window.FEATURE_INDETERMINATE_PROGRESS);
		getSherlock().setProgressBarIndeterminate(true);
		
		getBusInfo();
				
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bus_info);
	
		//set basic info
		setBusInfo();
		
		locationServiceInit();
		TableLayout t1 = (TableLayout)findViewById(R.id.table1);
		currentLoc_tag= (TextView)((TableRow)t1.getChildAt(0)).getChildAt(1);
	}

	private void setBusInfo() {	
		bgShape=(GradientDrawable) getResources().getDrawable(R.drawable.roundedtr);
//		Log.v("testing","setBusInfo");
		TextView SP=(TextView)findViewById(R.id.SP);
		TextView DP=(TextView)findViewById(R.id.DP);
		SP.setText(chiName[Integer.valueOf(S_E[0])]);
		DP.setText(chiName[Integer.valueOf(S_E[1])]);
		tmpA=Integer.valueOf(S_E[0]);
		
		TableLayout t2=(TableLayout)findViewById(R.id.table2);

		for (int i=0;i<numStops;i++){
			TextView tmp1=new TextView(this);
			TextView tmp2=new TextView(this);
			tmp1.setText(chiName[i]);
//			tmp2.setText(price[i]);
			tmp1.setLayoutParams(new TableRow.LayoutParams(0, 60, 3));
			tmp2.setLayoutParams(new TableRow.LayoutParams(0, 60, 1));

			TableRow row=new TableRow(this);
			row.addView(tmp1);
			row.addView(tmp2);
			setLocationOnClick(row);
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
				bgShape.setColor(0xFF58BAED);
				row.setBackgroundResource(R.drawable.roundedtr);
			}
		}
		
	}

	private void setLocationOnClick(TableRow row) {
		row.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
              try
              {
            	    DialogFragment newFragment = new FireMissilesDialogFragment();
            	    newFragment.show(getSupportFragmentManager(), "missiles");
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
		if (getIntent().getExtras() != null) {
			String[] busNum_S_E=getIntent().getExtras().getString("bus").split("&S_E=");
			S_E= busNum_S_E[1].split(";;");
			Log.v("testing",S_E[0]);
			setTitle(busNum_S_E[0]);
			String bookInfo= getIntent().getExtras().getString("busInfo");
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
		}
	}

    private void updateDistance(Location currentLocation) {
//    	Log.v("testing","updateDistance");
        Log.e("testing",currentLocation.getLatitude()+"&"+currentLocation.getLongitude());
        TextView tmp = null;
		TableLayout t2=(TableLayout)findViewById(R.id.table2);
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
//    		Log.e("testing","chiName="+chiName[pos]);
    		return pos;
    	}
    	return lastPos;
	}

	private void setCurrentColor(TableRow tableRow) {
		Log.v("testing","tmpA & S_E[0] ="+tmpA+" , "+Integer.valueOf(S_E[0]));
		if(tmpA==(Integer.valueOf(S_E[0]))){
//			ColorDrawable bgShape = (ColorDrawable)tableRow.getBackground();
			bgShape.setColor(0xaaf1c40f);
		}else if(tmpA==Integer.valueOf(S_E[1])){
			GradientDrawable bgShape = (GradientDrawable)tableRow.getBackground();
			bgShape.setColor(0xFF1dd2af);
			Log.v("testing","Arrived!!!");
			DialogFragment F = new FireMissilesDialogFragment();
	  		Bundle args = new Bundle();
	  		args.putString("type","voiceOutDestination");
	  	    args.putString("destination", chiName[tmpA]);
	  		F.setArguments(args);
	  	    F.show(getSupportFragmentManager(), "missiles");
	  	    
        	mIsRunning = false;
        	mHandler.removeCallbacks(myTask);
		}else{
//			tableRow.setBackgroundResource(R.color.honeycombish_blue);
			GradientDrawable bgShape = (GradientDrawable)tableRow.getBackground();
			bgShape.setColor(0xEE58BAED);
		}
		
	
	}

	public void getLocation(View v) {
		Log.v("testing","getLocation");
        if (servicesConnected()) {
            Location currentLocation = mLocationClient.getLastLocation();
            //  mLatLng.setText(LocationUtils.getLatLng(this, currentLocation));
        }
    }

    // For Eclipse with ADT, suppress warnings about Geocoder.isPresent()
    @SuppressLint("NewApi")
    public void getAddress(View v) {
    	Log.v("testing","getAddress");
        // In Gingerbread and later, use Geocoder.isPresent() to see if a geocoder is available.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && !Geocoder.isPresent()) {
            // No geocoder is present. Issue an error message
            Toast.makeText(this, R.string.no_geocoder_available, Toast.LENGTH_LONG).show();
            return;
        }

        if (servicesConnected()) {
            // Get the current location
            Location currentLocation = mLocationClient.getLastLocation();
            currentLocation = stopsLatLng[tmpA];
            Log.v("testing","tmpA==Stops.length"+tmpA+" "+Stops.length);

            updateDistance(currentLocation);
            // Turn the indefinite activity indicator on
//            mActivityIndicator.setVisibility(View.VISIBLE);
            getSherlock().setProgressBarIndeterminateVisibility(true);
            if(tmpA+1<(Stops.length)){
            	tmpA++;
            }
            // Start the background task
            (new BusInfo.GetAddressTask(this)).execute(currentLocation);
        }
    }

	public void startUpdates(View v) {
    	Log.v("testing","startUpdates");
        mUpdatesRequested = true;

        if (servicesConnected()) {
            startPeriodicUpdates();
        }
    }

    public void stopUpdates(View v) {
    	Log.v("testing","stopUpdates");
        mUpdatesRequested = false;

        if (servicesConnected()) {
            stopPeriodicUpdates();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
    	Log.v("testing","onConnected");
//        mConnectionStatus.setText(R.string.connected);

        if (mUpdatesRequested) {
        	mIsRunning = true;
        	myTask.run();
//            startPeriodicUpdates();
        }
    }

    @Override
    public void onDisconnected() {
    	Log.v("testing","onDisconnected");
//        mConnectionStatus.setText(R.string.disconnected);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    	Log.v("testing","onConnectionFailed");
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(
                        this,
                        LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            showErrorDialog(connectionResult.getErrorCode());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
    	Log.v("testing","onLocationChanged");
  		getAddress(currentLoc_tag);
    }

    private void startPeriodicUpdates() {
    	Log.v("testing","startPeriodicUpdates");
        mLocationClient.requestLocationUpdates(mLocationRequest, this);
//        mConnectionState.setText(R.string.location_requested);
    }

    private void stopPeriodicUpdates() {
    	Log.v("testing","stopPeriodicUpdates");
        mLocationClient.removeLocationUpdates(this);
//        mConnectionState.setText(R.string.location_updates_stopped);
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
        	Log.v("testing","GetAddressTask");
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

    private boolean servicesConnected() {
    	Log.v("testing","servicesConnected");
        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d("testing", getString(R.string.play_services_available));

            // Continue
            return true;
        // Google Play services was not available for some reason
        } else {
            // Display an error dialog
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
            if (dialog != null) {
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(dialog);
                errorFragment.show(getSupportFragmentManager(), LocationUtils.APPTAG);
            }
            return false;
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	Log.v("testing","onActivityResult");
        switch (requestCode) {
            case LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST :

                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.d(LocationUtils.APPTAG, getString(R.string.resolved));
                    break;
                    default:
                        Log.d(LocationUtils.APPTAG, getString(R.string.no_resolution));
                        // Display the result
//                        mConnectionState.setText(R.string.disconnected);
//                        mConnectionStatus.setText(R.string.no_resolution);
                    break;
                }
            default:
               Log.d(LocationUtils.APPTAG,
                       getString(R.string.unknown_activity_request_code, requestCode));
               break;
        }
    }
   
    @Override
    public void onStop() {

        super.onStop();
        mIsRunning = false;
        mHandler.removeCallbacks(myTask);
        Log.v("testing","onStop");
    }
   
    @Override
    public void onPause() {

        // Save the current setting for updates
        mEditor.putBoolean(LocationUtils.KEY_UPDATES_REQUESTED, mUpdatesRequested);
        mEditor.commit();
        if (mLocationClient.isConnected()) {
            stopPeriodicUpdates();
        }
        mLocationClient.disconnect();
        tmpA=0;
        super.onPause();
        Log.v("testing","onPause");
        mIsRunning = false;
        mHandler.removeCallbacks(myTask);
    }

    @Override
    public void onStart() {

        super.onStart();
        Log.v("testing","onStart");
    }
    
    @Override
    public void onResume() {
        super.onResume();
        Log.v("testing","onResume");

        // If the app already has a setting for getting location updates, get it
        if (mPrefs.contains(LocationUtils.KEY_UPDATES_REQUESTED)) {
            mUpdatesRequested = mPrefs.getBoolean(LocationUtils.KEY_UPDATES_REQUESTED, false);

        // Otherwise, turn off location updates until requested
        } else {
            mEditor.putBoolean(LocationUtils.KEY_UPDATES_REQUESTED, false);
            mEditor.commit();
        }
        mLocationClient.connect();
//        mHandler.postDelayed(myTask, 5000);

    }

	private void locationServiceInit() {
		Log.v("testing","locationServiceInit");
		 mLocationRequest = LocationRequest.create();
		 mLocationRequest.setInterval(LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS);
		 mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		 mLocationRequest.setFastestInterval(LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS);
		 mUpdatesRequested = true;
		 mPrefs = getSharedPreferences(LocationUtils.SHARED_PREFERENCES, Context.MODE_PRIVATE);
		 mEditor = mPrefs.edit();
		 mLocationClient = new LocationClient(this, this, this);
	}
    
    private void showErrorDialog(int errorCode) {
    	Log.v("testing","showErrorDialog");
        Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
            errorCode,
            this,
            LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

        // If Google Play services can provide an error dialog
        if (errorDialog != null) {

            // Create a new DialogFragment in which to show the error dialog
            ErrorDialogFragment errorFragment = new ErrorDialogFragment();

            // Set the dialog in the DialogFragment
            errorFragment.setDialog(errorDialog);

            // Show the error dialog in the DialogFragment
            errorFragment.show(getSupportFragmentManager(), LocationUtils.APPTAG);
        }
    }

    public static class ErrorDialogFragment extends DialogFragment {

        // Global field to contain the error dialog
        private Dialog mDialog;

        /**
         * Default constructor. Sets the dialog field to null
         */
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        /**
         * Set the dialog to display
         *
         * @param dialog An error dialog
         */
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        /*
         * This method must return a Dialog to the DialogFragment.
         */
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
        	Log.v("testing","onCreateDialog");
            return mDialog;
        }
    }
}