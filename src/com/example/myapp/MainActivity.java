package com.example.myapp;

//import com.example.mapdemo.R;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class MainActivity extends FragmentActivity implements OnMapClickListener, OnClickListener {

	//main activity that runs the app
	
	private GoogleMap mMap;
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


//		if (savedInstanceState == null) {
//			getSupportFragmentManager().beginTransaction()
//					.add(R.id.container, new PlaceholderFragment()).commit();
//		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	private LatLng firstClick;
	private LatLng lastClick;
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




}
