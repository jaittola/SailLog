package com.ja.saillog;

import java.io.IOException;
import java.util.LinkedList;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
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
        speedHeadingView.setText(String.format("Speed: %.1f, Heading: %.0f¡",
                                               speed,
                                               bearing));
        positionView.setText(String.format("Position: %s %s", 
                                           LocationFormatter.formatLatitude(latitude),
                                           LocationFormatter.formatLongitude(longitude)));
        setLocationAvailable(true);   // THis also is wrong. But the fake location data seems
        // to have the location availability status with random content.
    }

    public void setLocationAvailable(boolean isAvailable) {
        if (false == isAvailable) {
            speedHeadingView.setText(getResources().getText(R.string.no_speed_heading_data));
            positionView.setText(getResources().getText(R.string.no_speed_heading_data));
        }
        
        // TODO, make a GPS status widget.
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
        speedHeadingView = (TextView) findViewById(R.id.speedHeading);
        positionView = (TextView) findViewById(R.id.position);

        trackLocationButton.setOnCheckedChangeListener(locationTrackStartListener);

        progressBar = (ProgressBar) findViewById(R.id.progressBar1);
        showSpinner(false);
    }

    private class ExportDbTask extends AsyncTask<Void, Void, String> {

        public ExportDbTask() {
            super(); 
            exportFile = new ExportFile("db");
        }

        @Override
        protected void onPreExecute() {
            if (false == exportFile.isExportDirAvailable()) {
                preExecError = "Exporting failed: MMC file system not available";
                return;
            }

            showSpinner(true);
            allowLocationTracking(false);
        }

        @Override
        protected String doInBackground(Void... ignore) {
            if (null != preExecError) {
                return preExecError;
            }

            try {
                db.exportDbAsSQLite(exportFile);
            } catch (IOException ex) {
                return String.format("Exporting to %s failed: %s", 
                                     exportFile.fileName(), 
                                     ex.getLocalizedMessage());
            }

            return "Exported to " + exportFile.fileName();
        }

        @Override
        protected void onPostExecute(String result) {
            showSpinner(false);
            allowLocationTracking(true);
            toast(result);
        }

        private ExportFile exportFile;
        private String preExecError;
    }

    private void exportData() {
        new ExportDbTask().execute();
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
    private TextView speedHeadingView;
    private TextView positionView;

    private ProgressBar progressBar;


    private OnCheckedChangeListener locationTrackStartListener = new OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            trackingStatusChanged(isChecked);			
        }
    };

    /*
	private OnCheckedChangeListener engineStateListener = new OnCheckedChangeListener() {
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			// TODO Auto-generated method stub
		}
	};
     */
}