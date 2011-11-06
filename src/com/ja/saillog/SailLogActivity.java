package com.ja.saillog;

import android.app.Activity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class SailLogActivity extends Activity implements SailLogLocationSink {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
                
        setupWidgetListeners();
        
        locationTracker = new SailLogLocationTracker(this);

        trackingStatusChanged(false);  // We start with everything turned off.
        							   // This may need changing.
    }
    
    public void updateLocation(double latitude,
			   				   double longitude,
			   				   double speed,
			   				   double bearing) {
    	speedView.setText(String.format("%.1f", speed));
    	headingView.setText(String.format("%.0f", bearing));
    	latitudeView.setText(LocationFormatter.formatLatitude(latitude));
    	longitudeView.setText(LocationFormatter.formatLongitude(longitude));
    	setLocationAvailable(true);   // THis also is wrong. But the fake location data seems
    	                              // to have the location availability status with random content.
    }
    
    public void setLocationAvailable(boolean isAvailable) {
    	speedView.setEnabled(isAvailable);
    	headingView.setEnabled(isAvailable);
    	latitudeView.setEnabled(isAvailable);
    	longitudeView.setEnabled(isAvailable);
    }
    
    private void trackingStatusChanged(boolean isEnabled) {
    	engineButton.setEnabled(isEnabled);	
       	locationTracker.setEnabled(isEnabled);

       	if (false == isEnabled) {
       		setLocationAvailable(false);
       	}
    }
    
    private SailLogLocationTracker locationTracker;
 
    private CompoundButton trackLocationButton;
    private CompoundButton engineButton;
    private TextView speedView;
    private TextView headingView;
    private TextView latitudeView;
    private TextView longitudeView;
    
    private void setupWidgetListeners() {
    	trackLocationButton = (CompoundButton) findViewById(R.id.trackLocationButton);
    	engineButton = (CompoundButton) findViewById(R.id.engineButton);
    	speedView = (TextView) findViewById(R.id.speedView);
    	headingView = (TextView) findViewById(R.id.headingView);
    	latitudeView = (TextView) findViewById(R.id.latitudeView);
    	longitudeView = (TextView) findViewById(R.id.longitudeView);
    	
    	trackLocationButton.setOnCheckedChangeListener(locationTrackStartListener);
    }
    
    private OnCheckedChangeListener locationTrackStartListener = new OnCheckedChangeListener() {
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			trackingStatusChanged(isChecked);			
		}
	};
	
	private OnCheckedChangeListener engineStateListener = new OnCheckedChangeListener() {
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			// TODO Auto-generated method stub
		}
	};
}