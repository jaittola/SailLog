package com.ja.saillog;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class SailLogActivity extends Activity implements LocationSink {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        setupDbInterfaces();
        setupWidgets();
        
        LinkedList<LocationSink> sinks = new LinkedList<LocationSink>();
        sinks.add(this);
        sinks.add(new DBLocationSink(db));
        locationTracker = new LocationTracker(this, sinks);

        trackingStatusChanged(false);  // We start with everything turned off.
        							   // This may need changing.
        
        sla = this;  // Shortcut used in asynchronous messaging.
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.export:
            exportData();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
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
    	// engineButton.setEnabled(isEnabled);	
       	locationTracker.setEnabled(isEnabled);

       	if (false == isEnabled) {
       		setLocationAvailable(false);
       	}
    }
    
    private void setupWidgets() {
    	trackLocationButton = (CompoundButton) findViewById(R.id.trackLocationButton);
    	// engineButton = (CompoundButton) findViewById(R.id.engineButton);
    	speedView = (TextView) findViewById(R.id.speedView);
    	headingView = (TextView) findViewById(R.id.headingView);
    	latitudeView = (TextView) findViewById(R.id.latitudeView);
    	longitudeView = (TextView) findViewById(R.id.longitudeView);
    	
    	trackLocationButton.setOnCheckedChangeListener(locationTrackStartListener);
    	
    	progressBar = (ProgressBar) findViewById(R.id.progressBar1);
    	showSpinner(false);
    }
    
    private void exportData() {
        exportFile = new ExportFile("db");
        if (false == exportFile.isExportDirAvailable()) {
            toast("Exporting failed: MMC file system not available");
            return;
        }
                
        showSpinner(true);
        allowLocationTracking(false);
        
        new Thread(new Runnable() {
            public void run() {
                             
               resultMsg = String.format("Exported to %s", exportFile.fileName());
               try {
                   db.exportDbAsSQLite(exportFile);
                } catch (IOException ex) {
                    resultMsg = String.format("Exporting to %s failed: %s", 
                                              exportFile.fileName(), 
                                              ex.getLocalizedMessage());
                }
                
                sla.runOnUiThread(new Runnable() {
                    public void run() {
                        sla.exportDone();
                    }
                });
            }
        }).start();
    }
    
    private void exportDone() {
        showSpinner(false);
        allowLocationTracking(true);
        toast(resultMsg);
    }
    
    private void allowLocationTracking(boolean allow) {
        
        if (false == allow) {
        trackLocationButton.setChecked(false);  // Enforce off
        trackLocationButton.setEnabled(false);
        } else {
            trackLocationButton.setEnabled(true);
        }
    }
    
    private void setupDbInterfaces() {
    	db = new DB(this, "SLDB.db");
    }
    
    private void showSpinner(boolean show) {
        int visibility = View.INVISIBLE;
        
        if (true == show) {
            visibility = View.VISIBLE;
        }
        
        progressBar.setIndeterminate(show);
        progressBar.setVisibility(visibility);
    }
    
    private void toast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }
    
    private DB db;
    private LocationTracker locationTracker;
    
    private CompoundButton trackLocationButton;
    // private CompoundButton engineButton;
    private TextView speedView;
    private TextView headingView;
    private TextView latitudeView;
    private TextView longitudeView;
    
    private ProgressBar progressBar;
    
    // TODO, these are horrendous hacks.
    SailLogActivity sla;
    ExportFile exportFile;
    String resultMsg;
    
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