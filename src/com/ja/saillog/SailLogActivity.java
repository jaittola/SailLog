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
        sinks.add(dbSink);
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
        case R.id.export_db:
            exportData();
            return true;
        case R.id.export_kml:
            exportDataAsKML();
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    public void updateLocation(double latitude,
                               double longitude,
                               double speed,
                               double bearing,
                               long time) {
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
            positionView.setText(getResources().getText(R.string.no_position_data));
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
    
    private void sailingEvents() {
        // Accumulate statuses from all event widgets.
        // We should actually combine several sail change events
        // to the same one. That may have to be done on the db
        // level, though.
        int engineStatus = engineStatusButton.isChecked() ? 1 : 0;
        int sailPlan = ((mainSailCheckbox.isChecked() ? 1 : 0) |
                        (jibCheckbox.isChecked() ? 1 << 1 : 0) |
                        (spinnakerCheckbox.isChecked() ? 1 << 2 : 0));
        dbSink.insertEvent(engineStatus, sailPlan);
    }

    private void setupWidgets() {
        trackLocationButton = (CompoundButton) findViewById(R.id.trackLocationButton);
        engineStatusButton = (CompoundButton) findViewById(R.id.engineStatusButton);
        mainSailCheckbox = (CompoundButton) findViewById(R.id.mainSailCheckbox);
        jibCheckbox = (CompoundButton) findViewById(R.id.jibCheckbox);
        spinnakerCheckbox = (CompoundButton) findViewById(R.id.spinnakerCheckbox);
        speedHeadingView = (TextView) findViewById(R.id.speedHeading);
        positionView = (TextView) findViewById(R.id.position);

        trackLocationButton.setOnCheckedChangeListener(locationTrackStartListener);
        engineStatusButton.setOnCheckedChangeListener(sailingEventsListener);
        mainSailCheckbox.setOnCheckedChangeListener(sailingEventsListener);
        jibCheckbox.setOnCheckedChangeListener(sailingEventsListener);
        spinnakerCheckbox.setOnCheckedChangeListener(sailingEventsListener);

        progressBar = (ProgressBar) findViewById(R.id.progressBar1);
        showSpinner(false);
    }

    private abstract class ExportDbTask extends AsyncTask<Void, Void, String> {

        public ExportDbTask() {
            super();
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
                doExport();
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
        
        protected abstract void doExport() throws IOException;

        protected ExportFile exportFile;
        private String preExecError;
    }
    
    private class ExportDbAsSQLiteTask extends ExportDbTask {
        public ExportDbAsSQLiteTask() {
            super();
            exportFile = new ExportFile("db");
        }
        
        protected void doExport() throws IOException {
            // TODO, should go elsewhere.
            dbFactory.realTrackDB().exportDbAsSQLite(exportFile);
        }
    }
    
    private class ExportDbAsKMLTask extends ExportDbTask {
        public ExportDbAsKMLTask() {
            super();
            exportFile = new ExportFile("kml");
        }
  
        protected void doExport() throws IOException {
            // TODO, should go elsewhere
            dbFactory.realTrackDB().exportDbAsKML(exportFile);
        }
    }

    private void exportData() {
        new ExportDbAsSQLiteTask().execute();
    }
    
    private void exportDataAsKML() {
        new ExportDbAsKMLTask().execute();
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
        dbFactory = new DBFactory(this);
        dbSink = new DBLocationSink(dbFactory);
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
    
    private OnCheckedChangeListener locationTrackStartListener = new OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
          trackingStatusChanged(isChecked);           
        }
    };
    
    private OnCheckedChangeListener sailingEventsListener = new OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            sailingEvents();
        }
    };
    
    private DBFactory dbFactory;
    private DBLocationSink dbSink;
    private LocationTracker locationTracker;

    private CompoundButton trackLocationButton;
    private CompoundButton engineStatusButton;
    private CompoundButton mainSailCheckbox;
    private CompoundButton jibCheckbox;
    private CompoundButton spinnakerCheckbox;
    private TextView speedHeadingView;
    private TextView positionView;

    private ProgressBar progressBar;
}


 