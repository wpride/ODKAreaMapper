package com.example.myapp;

//import com.example.mapdemo.R;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class MainActivity extends FragmentActivity 
						  implements OnMapClickListener, OnClickListener,
						  GooglePlayServicesClient.ConnectionCallbacks,
					        GooglePlayServicesClient.OnConnectionFailedListener{

	//main activity that runs the app
	
	private GoogleMap mMap;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	private LatLng firstClick;
	private LatLng lastClick;
	private LocationClient mLocationClient;
	private Location mCurrentLocation; 
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
        // Try to obtain the map from the SupportMapFragment.
        mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                .getMap();
        mMap.setOnMapClickListener(this);
        
    	Button doneButton = (Button) findViewById(R.id.submit_button);
    	doneButton.setOnClickListener(this);
    	
        mLocationClient = new LocationClient(this, this, this);
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

    /*
     * Called when the Activity is no longer visible.
     */
    @Override
    protected void onStop() {
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

	@Override
	public void onClick(View v) 
	{
		if(firstClick != null && lastClick != null)
		{
			PolylineOptions line = new PolylineOptions().add(
					firstClick,
					lastClick);
			mMap.addPolyline(line);
			System.out.println(findDistance(firstClick, lastClick));
		}
		else
		{
			System.out.println("you need more points!");
		}
	}
	
    @Override
    public void onConnected(Bundle dataBundle) {
        // Display the connection status
        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
        getLocation();
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
     * Invoked by the "Get Location" button.
     *
     * Calls getLastLocation() to get the current location
     *
     * @param v The view object associated with this method, in this case a Button.
     */
    public void getLocation() {

        // If Google Play Services is available
        if (servicesConnected()) {

            // Get the current location
            mCurrentLocation = mLocationClient.getLastLocation();
            System.out.println("current location");
            System.out.println(mCurrentLocation.toString());
        }
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
