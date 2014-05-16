package com.example.myapp;

import java.util.ArrayList;

import android.content.IntentSender;
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
	private Location currentLocation; 
	private LocationRequest mLocationRequest;
	
	
	private TextView locationText;
	
	//default states for the two booleans
	private boolean isWalking = false;
	private boolean allowAutomaticUpdates = true;
	
	//array to store the locations from the periodic updates
	private ArrayList<Location> userPoints;
	
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
	    
	public enum UiState {idle, walking, plotting};
	public UiState uiState = UiState.idle;


    /**
     * determines what UI elements need to be present based on what state we're in
     */
	public void refreshView()
	{
		switch(uiState)
		{
			case idle:
				this.setModeToIdle();
				break;
			case walking:
				this.setModeToWalking();
				break;
			case plotting:
				this.setModeToPlotting();
		}
	}
	
	//TODO implement this UI mode
	private void setModeToIdle()
	{
		isWalking = false;
		//UI stuff goes here
	}
	
	//TODO implement this UI mode
	private void setModeToWalking()
	{
		isWalking = true;
		//UI stuff goes here
	}
	
	//TODO implement this UI mode
	private void setModeToPlotting()
	{
		isWalking = false;
		//UI stuff goes here
	}
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		

		
        // Try to obtain the map from the SupportMapFragment.
        mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                .getMap();
        mMap.setOnMapClickListener(this); 

//		  TODO center map around user's location (not working currently)
//        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(getCurrentLocation().getLatitude(), getCurrentLocation().getLongitude()),
//                10), 2000, null);
        mMap.setBuildingsEnabled(false);
        mMap.setMyLocationEnabled(true);
                
        locationText = (TextView) findViewById(R.id.LocationText);
    	Button doneButton = (Button) findViewById(R.id.connect_points_button);
    	doneButton.setOnClickListener(new View.OnClickListener() 
    	{
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
        			setModeToIdle();
    			}
       			else
    			{
    				Toast.makeText(v.getContext(), "you need more points!", Toast.LENGTH_SHORT).show();
    			}    	    
    		}
    	});
    	
    	Button startWalkButton = (Button) findViewById(R.id.start_walk_button);
    	startWalkButton.setOnClickListener(new View.OnClickListener() 
    	{
			@Override
			public void onClick(View v) 
			{
				setModeToWalking();
			}
		});
   	
    	mLocationRequest = LocationRequest.create();
    	
    	mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    	mLocationRequest.setInterval(UPDATE_INTERVAL);
    	mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
    	
        mLocationClient = new LocationClient(this, this, this);
        
        if(savedInstanceState != null) 
        {
        	isWalking = savedInstanceState.getBoolean("is_walking");
        	allowAutomaticUpdates = savedInstanceState.getBoolean("allow_automatic_updates");
        	userPoints = savedInstanceState.getParcelableArrayList("user_points");
        	
        	//populate the map using previous points
        	if(userPoints != null)
        	{
            	for(int i = 0; i < userPoints.size()-2; i++)
            	{
    	    		PolylineOptions line = new PolylineOptions().add(
    						toLatLng(userPoints.get(i)),
    						toLatLng(userPoints.get(i+1))
    						);
    	    		mMap.addPolyline(line);
            	}
        	}
        }
    }

    /*
     * Called when the Activity becomes visible.
     */
    @Override
    protected void onStart() 
    {
        super.onStart();
        // Connect the client.
        mLocationClient.connect();
    }

    @Override
    protected void onPause() 
    {
//        // Save the current setting for updates
//        mEditor.putBoolean("KEY_UPDATES_ON", mUpdatesRequested);
//        mEditor.commit();
        super.onPause();
    }
    
    //TODO: Finish this implementation
    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putBoolean("is_walking", isWalking);
        outState.putBoolean("allow_automatic_updates", allowAutomaticUpdates);
        outState.putParcelableArrayList("user_points", userPoints);
    }
    
    @Override
    protected void onResume() 
    {
//        /*
//         * Get any previous setting for location updates
//         * Gets "false" if an error occurs
//         */
//
//        
//        // Otherwise, turn off location updates
//        else 
//        {
//            mEditor.putBoolean("KEY_UPDATES_ON", false);
//            mEditor.commit();
//        }
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
        if (allowAutomaticUpdates) 
        {
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
    @Override
    public void onLocationChanged(Location location) {
        // Report to the UI that the location is not certain -- debugging only
    	//TODO Tune this value, Remove this debugging check
    	if(location.getAccuracy() > 10000.0)
    	{
    		locationText.setText(String.valueOf(location.getAccuracy()));
    	}
    	
    	else
    	{
    		//if this is the first point that is collected
	    	if(currentLocation != null)
	    	{
	    		PolylineOptions line = new PolylineOptions().add(
						toLatLng(location),
						toLatLng(currentLocation));
	    		mMap.addPolyline(line);
	    	}
	    	else
	    	{
	    		//if no point has been collected, then userPoints wouldn't have been initialized yet, so initialize it!
	    		userPoints = new ArrayList<Location>();
	    	}
			currentLocation = location;
			
			userPoints.add(currentLocation);
			
	        String msg = "Updated Location: " +
	                Double.toString(location.getLatitude()) + ","  +
	                Double.toString(location.getLongitude());
	//        mLatLng.setText(msg);
	        
	        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    	}
    }
    
    /**
     * converts Location in to LatLng out
     */
    public LatLng toLatLng(Location location)
    {
    	return new LatLng(location.getLatitude(), location.getLongitude());
    }
    
    /**
     * gets current location if the service is available
	*/
    public Location getCurrentLocation()
    {
    	if(servicesConnected())
    	{
            return mLocationClient.getLastLocation();
    	}
    	return null;
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
	 * calculates the distance between two LatLng points using the haversine formula
	 */
	private double findDistance(LatLng firstPoint, LatLng secondPoint)
	{
		final double radiusOfEarth = 6378;
		double firstLatitude = firstPoint.latitude;
		double firstLongitude = firstPoint.longitude;
		double secondLatitude = secondPoint.latitude;
		double secondLongitude = secondPoint.longitude;
		
		return haversine(firstLatitude, firstLongitude, secondLatitude, secondLongitude, radiusOfEarth);
	}
	
	/**
	 * calculates the distance between two Locations using the haversine formula
	 */
	public double findDistance(Location firstPoint, Location secondPoint)
	{
		final double radiusOfEarth = 6378;
		double firstLatitude = firstPoint.getLatitude();
		double firstLongitude = firstPoint.getLongitude();
		double secondLatitude = secondPoint.getLatitude();
		double secondLongitude = secondPoint.getLongitude();
		return haversine(firstLatitude, firstLongitude, secondLatitude, secondLongitude, radiusOfEarth);
	} 
	
	/**
	 * Uses the haversine formula to determine what the spherical distance between two points are
	 * @param firstLat
	 * @param firstLong
	 * @param secondLat
	 * @param secondLong
	 * @param r
	 * @return spherical distance in kilometres
	 */
	public double haversine(double firstLat, double firstLong, double secondLat, double secondLong, double r)
	{
		double latitudeDistance = inRadians(firstLat - secondLat);
		double longitudeDistance = inRadians(firstLong - secondLong);
		double a = Math.pow((Math.sin(latitudeDistance / 2)) , 2) 
				+ Math.cos(inRadians(firstLat)) * Math.cos(inRadians(secondLat))
				  * Math.pow((Math.sin(longitudeDistance / 2)) , 2);
		return 2 * r * Math.atan2(Math.pow(a, 0.5), Math.pow(1 - a, 0.5));
	}
	
	/**
	 * converts degrees to radians
	 */
	private double inRadians(Double degree)
	{
		return degree * Math.PI / 180.0;
	}

	//TODO finish this implementation
	public void findArea()
	{
		//http://forum.worldwindcentral.com/showthread.php?20724-A-method-to-compute-the-area-of-a-spherical-polygon
	}


}
