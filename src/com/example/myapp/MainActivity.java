package com.example.myapp;

//import com.example.mapdemo.R;
import android.content.Context;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class MainActivity extends FragmentActivity 
						  implements OnMapClickListener,
						  GooglePlayServicesClient.ConnectionCallbacks,
					        GooglePlayServicesClient.OnConnectionFailedListener,
					        com.google.android.gms.location.LocationListener {

	//main activity that runs the app
	
	private GoogleMap mMap;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	private LatLng firstClick;
	private LatLng lastClick;
	private LocationClient mLocationClient;
	private Location mCurrentLocation; 
	private LocationRequest mLocationRequest;
	private boolean mUpdatesRequested;
	
	 // Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;
    // Update frequency in seconds
    public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
    // Update frequency in milliseconds
    private static final long UPDATE_INTERVAL =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    // The fastest update frequency, in seconds
    private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
    // A fast frequency ceiling in milliseconds
    private static final long FASTEST_INTERVAL =
            MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;
	
    private SharedPreferences mPrefs;
    private SharedPreferences.Editor mEditor;
    
    private TextView mLatLng;

    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
        mLatLng = (TextView) findViewById(R.id.label_lat_lng);

		
        // Try to obtain the map from the SupportMapFragment.
        mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                .getMap();
        mMap.setOnMapClickListener(this);
        
    	Button doneButton = (Button) findViewById(R.id.submit_button);
    	doneButton.setOnClickListener(new View.OnClickListener() {
    	    @Override
    	    public void onClick(View v) 
    	    {
    			if(firstClick != null && lastClick != null)
    			{
    				PolylineOptions line = new PolylineOptions().add(
    						firstClick,
    						lastClick);
    				mMap.addPolyline(line);
    				Toast.makeText(v.getContext(), String.valueOf(findDistance(firstClick, lastClick)), Toast.LENGTH_SHORT).show();
    			}
    			else
    			{
    				Toast.makeText(v.getContext(), "you need more points!", Toast.LENGTH_SHORT).show();
    			}    	    
    		}
    	});
    	
    	Button getLocationButton = (Button) findViewById(R.id.get_location_button);
    	getLocationButton.setOnClickListener(new View.OnClickListener() 
    	{
    	    @Override
    	    public void onClick(View v) 
    	    {
    	        // If Google Play Services is available
    	        if (servicesConnected()) 
    	        {
    	            // Get the current location
    	            mCurrentLocation = mLocationClient.getLastLocation();
    	            mLatLng.setText(mCurrentLocation.toString());
        	    	Toast.makeText(v.getContext(), mCurrentLocation.toString(), Toast.LENGTH_SHORT).show();
    	        }
    	    }
    	});
   	
    	mLocationRequest = LocationRequest.create();
    	
    	mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    	mLocationRequest.setInterval(UPDATE_INTERVAL);
    	mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
    	
        mLocationClient = new LocationClient(this, this, this);
        
        mPrefs = getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
        mEditor = mPrefs.edit();

        mUpdatesRequested = true;
	}

    /*
     * Called when the Activity becomes visible.
     */
    @Override
    protected void onStart() {
        super.onStart();
        // Connect the client.
        mLocationClient.connect();
    }

    @Override
    protected void onPause() {
        // Save the current setting for updates
        mEditor.putBoolean("KEY_UPDATES_ON", mUpdatesRequested);
        mEditor.commit();
        super.onPause();
    }
    
    @Override
    protected void onResume() {
        /*
         * Get any previous setting for location updates
         * Gets "false" if an error occurs
         */
        if (mPrefs.contains("KEY_UPDATES_ON")) {
            mUpdatesRequested =
                    mPrefs.getBoolean("KEY_UPDATES_ON", false);

        // Otherwise, turn off location updates
        } else {
            mEditor.putBoolean("KEY_UPDATES_ON", false);
            mEditor.commit();
        }
        super.onResume();
    }
    
    /*
     * Called when the Activity is no longer visible.
     */
    @Override
    protected void onStop() {
    	
    	if (mLocationClient.isConnected()) {
            /*
             * Remove location updates for a listener.
             * The current Activity is the listener, so
             * the argument is "this".
             */
            mLocationClient.removeLocationUpdates(this);
        }
        // Disconnecting the client invalidates it.
        mLocationClient.disconnect();
        super.onStop();
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	

	@Override
	public void onMapClick(LatLng position)
	{
		if(firstClick == null)
		{
			firstClick = position;
		}
		else
		{
			lastClick = position;
		}		
		mMap.addMarker(new MarkerOptions().position(position)
				  );
	}

//	@Override
//	public void onClick(View v) 
//	{

//	}
	
    @Override
    public void onConnected(Bundle dataBundle) {
        // Display the connection status
        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
        if (mUpdatesRequested) {
            startPeriodicUpdates();
        }
    }
        
    private void startPeriodicUpdates()
    {
    	mLocationClient.requestLocationUpdates(mLocationRequest, this);
    }

    /*
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
    @Override
    public void onDisconnected() {
        // Display the connection status
        Toast.makeText(this, "Disconnected. Please re-connect.",
                Toast.LENGTH_SHORT).show();
    }

    /*
     * Called by Location Services if the attempt to
     * Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            int errorCode = connectionResult.getErrorCode();
  		  	GooglePlayServicesUtil.getErrorDialog(errorCode, this, 0).show();
        }
    }
    
    // Define the callback method that receives location updates
    //TODO WHY ISN'T THIS WORKING
    @Override
    public void onLocationChanged(Location location) {
        // Report to the UI that the location was updated
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
//        mLatLng.setText(msg);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
    
    
	private boolean servicesConnected() {
        // Check that Google Play services is available
		int errorCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (errorCode != ConnectionResult.SUCCESS) 
		{
		  GooglePlayServicesUtil.getErrorDialog(errorCode, this, 0).show();
		  return false;
		}
		return true;
    }
	
	
	/**
	 * calculates the distance between two points using the haversine formula
	 */
	private double findDistance(LatLng firstPoint, LatLng secondPoint)
	{
		final double radiusOfEarth = 6378;
		double latitudeDistance = inRadians(firstPoint.latitude - secondPoint.latitude);
		double longitudeDistance = inRadians(firstPoint.longitude - secondPoint.longitude);
		double a = Math.pow((Math.sin(latitudeDistance / 2)) , 2) 
				+ Math.cos(inRadians(firstPoint.latitude)) * Math.cos(inRadians(secondPoint.latitude))
				  * Math.pow((Math.sin(longitudeDistance / 2)) , 2);
		return 2 * radiusOfEarth * Math.atan2(Math.pow(a, 0.5), Math.pow(1 - a, 0.5));
	}
	
	private double inRadians(Double degree)
	{
		return degree * Math.PI / 180.0;
	}


//	public void doCalculations()
//	{
//		
//	}

public void findArea()
{
	//http://forum.worldwindcentral.com/showthread.php?20724-A-method-to-compute-the-area-of-a-spherical-polygon
}


}
