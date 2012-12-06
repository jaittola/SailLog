package com.ja.saillog;

import java.io.IOException;
import java.util.LinkedList;

import com.ja.saillog.TripDB.TripInfo;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
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

        setupWidgets();

        locationTracker = new LocationTracker(this);
        setupTripInfo();
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
        // TODO, remove hard-coded speed unit.
        String speedUnit = "kn";
        double speedUnitConversion = 1.8;
        
        speedView.setText(String.format("%.1f %s",
                          speedUnitConversion * speed,
                          speedUnit));
        headingView.setText(String.format("%.0f¡",
                                          bearing));
        latView.setText(LocationFormatter.formatLatitude(latitude));
        lonView.setText(LocationFormatter.formatLongitude(longitude));
        setLocationAvailable(true);   // THis also is wrong. But the fake location data seems
        // to have the location availability status with random content.
    }

    public void setLocationAvailable(boolean isAvailable) {
        if (false == isAvailable) {
            speedView.setText("");
            headingView.setText("");
            latView.setText("");
            lonView.setText("");
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
        int engineStatus = engineStatusCheckbox.isChecked() ? 1 : 0;
        int sailPlan = ((mainSailCheckbox.isChecked() ? 1 : 0) |
                        (jibCheckbox.isChecked() ? 1 << 1 : 0) |
                        (spinnakerCheckbox.isChecked() ? 1 << 2 : 0));
        dbSink.insertEvent(engineStatus, sailPlan);
    }

    private void setupWidgets() {
        trackLocationButton = (CompoundButton) findViewById(R.id.trackLocationButton);
        engineStatusCheckbox = (CompoundButton) findViewById(R.id.engineStatusButton);
        mainSailCheckbox = (CompoundButton) findViewById(R.id.mainSailCheckbox);
        jibCheckbox = (CompoundButton) findViewById(R.id.jibCheckbox);
        spinnakerCheckbox = (CompoundButton) findViewById(R.id.spinnakerCheckbox);
        speedView = (TextView) findViewById(R.id.speedText);
        headingView = (TextView) findViewById(R.id.headingText);
        latView = (TextView) findViewById(R.id.latText);
        lonView = (TextView) findViewById(R.id.lonText);
        tripNameView = (TextView) findViewById(R.id.tripNameText);

        trackLocationButton.setOnCheckedChangeListener(locationTrackStartListener);
        engineStatusCheckbox.setOnCheckedChangeListener(sailingEventsListener);
        mainSailCheckbox.setOnCheckedChangeListener(sailingEventsListener);
        jibCheckbox.setOnCheckedChangeListener(sailingEventsListener);
        spinnakerCheckbox.setOnCheckedChangeListener(sailingEventsListener);
        tripNameView.setOnClickListener(tripSelectClickListener);

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
            trackDB.exportDbAsSQLite(exportFile);
        }
    }

    private class ExportDbAsKMLTask extends ExportDbTask {
        public ExportDbAsKMLTask() {
            super();
            exportFile = new ExportFile("kml");
        }

        protected void doExport() throws IOException {
            // TODO, should go elsewhere
            trackDB.exportDbAsKML(exportFile);
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

    private void showTripSelector() {
        startActivityForResult(new Intent("com.ja.saillog.tripSelector"),
                               tripSelectionRequestCode);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (tripSelectionRequestCode == requestCode &&
            RESULT_OK == resultCode) {
            setupTripInfo();
        }
    }

    private void setupTripInfo() {
        TripDB tripDB = new TripDB(this);
        TripInfo ti = tripDB.getSelectedTrip();

        // Set db sink to NULL to prevent any further updates.
        if (null != dbSink) {
            dbSink.setDb(null);
            dbSink = null;
        }
        if (null != trackDB) {
            trackDB.close();
            trackDB = null;
        }

        if (null == ti) {
            tripNameView.setText("");
            enableControls(false);

            locationTracker.setSinks(null);
        }
        else {
            trackDB = new TrackDB(this, ti.dbFileName);
            dbSink = new DBLocationSink(trackDB);

            tripNameView.setText(ti.tripName);
            enableControls(true);

            LinkedList<LocationSink> sinks = new LinkedList<LocationSink>();
            sinks.add(this);
            sinks.add(dbSink);
            locationTracker.setSinks(sinks);
        }

        tripDB.close();
    }

    private void enableControls(boolean enabled) {
        trackLocationButton.setChecked(false);
        engineStatusCheckbox.setChecked(false);
        mainSailCheckbox.setChecked(false);
        jibCheckbox.setChecked(false);
        spinnakerCheckbox.setChecked(false);

        trackLocationButton.setEnabled(enabled);
        engineStatusCheckbox.setEnabled(enabled);
        mainSailCheckbox.setEnabled(enabled);
        jibCheckbox.setEnabled(enabled);
        spinnakerCheckbox.setEnabled(enabled);
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

    private OnClickListener tripSelectClickListener = new OnClickListener() {
            public void onClick(View v) {
                showTripSelector();
            }
        };

    private DBLocationSink dbSink;
    private TrackDB trackDB;
    private LocationTracker locationTracker;

    private CompoundButton trackLocationButton;
    private CompoundButton engineStatusCheckbox;
    private CompoundButton mainSailCheckbox;
    private CompoundButton jibCheckbox;
    private CompoundButton spinnakerCheckbox;
    private TextView speedView;
    private TextView headingView;
    private TextView latView;
    private TextView lonView;
    private TextView tripNameView;

    private ProgressBar progressBar;

    final private static int tripSelectionRequestCode = 1;
}
