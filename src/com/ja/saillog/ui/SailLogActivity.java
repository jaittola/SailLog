package com.ja.saillog.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.ja.saillog.R;
import com.ja.saillog.database.DBProvider;
import com.ja.saillog.database.TrackDBInterface;
import com.ja.saillog.database.TripDBInterface;
import com.ja.saillog.database.TripDBInterface.TripInfo;
import com.ja.saillog.quantity.quantity.QuantityFactory;
import com.ja.saillog.quantity.quantity.Speed;
import com.ja.saillog.utilities.DBLocationSink;
import com.ja.saillog.utilities.LocationFormatter;
import com.ja.saillog.utilities.LocationServiceProvider;
import com.ja.saillog.utilities.LocationSink;
import com.ja.saillog.utilities.LocationSinkAdapter;
import com.ja.saillog.utilities.SailPlan;
import com.ja.saillog.utilities.StaticStrings;

public class SailLogActivity
    extends SailLogActivityBase
    implements LocationSink {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        StaticStrings.setup(this); // App-wide generic setup.

        setupSailPlan();
        setupWidgets();

        dbSink = new DBLocationSink(null);

        setupTripInfo();
        setLocationAvailable(false);  // We start with everything turned off.
                                      // This may need changing.
    }

    @Override
    public void onStart() {
        eventsSaved = 0;

        super.onStart();
    }

    @Override
    public void onStop() {
        if (0 != eventsSaved) {
            // Do a sailing events status update
            // before leaving this view if there were
            // events saved while this view was visible.
            // This needs to be done in a smarter way.
            sailingEvents();
        }

        super.onStop();
    }

    @Override
    public void onDestroy() {
        if (null != trackDB) {
            trackDB.close();
        }
        dbSink.setDb(null);

        super.onDestroy();
    }

    public void updateLocation(double latitude,
                               double longitude,
                               Speed speed,
                               double bearing,
                               double accuracy,
                               long time) {

        // TODO, remove hard-coded speed unit.
        speedView.setText(QuantityFactory.knots(speed).withUnit());
        headingView.setText(String.format("%.0f¡",
                                          bearing));
        latView.setText(LocationFormatter.formatLatitude(latitude));
        lonView.setText(LocationFormatter.formatLongitude(longitude));

        // This is wrong. But the fake location data seems to have the
        // location availability status with random content.
        setLocationAvailable(true);
    }

    // TODO, would need connecting to the provider.
    public void setLocationAvailable(boolean isAvailable) {
        if (false == isAvailable) {
            speedView.setText("");
            headingView.setText("");
            latView.setText("");
            lonView.setText("");
        }

        // TODO, make a GPS status widget.
    }

    private void trackingStatusChanged(boolean isChecked) {
        // Enable or disable location tracking.
        if (true == isChecked) {
            if (null == trackDB) {
                toast("Cannot start tracking when no trip has been selected");
                trackLocationButton.setChecked(false);
                isChecked = false;
            }
            else {
                if (null == uiSinkAdapter) {
                    uiSinkAdapter = new LocationSinkAdapter(this);
                    LocationServiceProvider.get(this).requestUpdates(uiSinkAdapter);
                }
                if (null == dbSinkAdapter) {
                    dbSinkAdapter = new LocationSinkAdapter(dbSink);
                    LocationServiceProvider.get(this).requestUpdates(dbSinkAdapter);
                }
            }
        }
        else {
            if (null != uiSinkAdapter) {
                LocationServiceProvider.get(this).stopUpdates(uiSinkAdapter);
                uiSinkAdapter = null;
            }
            if (null != dbSinkAdapter) {
                LocationServiceProvider.get(this).stopUpdates(dbSinkAdapter);
                dbSinkAdapter = null;
            }
        }

        setLocationAvailable(isChecked);
    }

    private void sailingEvents() {
        // Accumulate statuses from all event widgets.

        // We should actually combine several sail change events
        // to the same one. That may have to be done on the db
        // level, though.
        int engineStatus = engineStatusCheckbox.isChecked() ? 1 : 0;

        sp.setSail(mainSailId, mainSailCheckbox.isChecked());
        sp.setSail(jibId, jibCheckbox.isChecked());
        sp.setSail(spinnakerId, spinnakerCheckbox.isChecked());

        dbSink.insertEvent(engineStatus, sp.getSailPlan());

        eventsSaved++;
    }

    private void setupSailPlan() {
        mainSailId = SailPlan.addSail(getString(R.string.main_sail));
        jibId = SailPlan.addSail(getString(R.string.jib));
        spinnakerId = SailPlan.addSail(getString(R.string.spinnaker));

        sp = new SailPlan();
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
    }


    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent data) {
        if (TripSelectorActivity.myIntentRequestCode == requestCode) {
            setupTripInfo();
        }
    }

    private void setupTripInfo() {
        // Set db info to NULL to prevent any further updates.
        if (null != trackDB) {
            dbSink.setDb(null);
            trackDB.close();
            trackDB = null;
        }

        TripDBInterface tripDB = DBProvider.getTripDB(this);
        TripInfo ti = tripDB.getActiveTrip();

        if (null == ti) {
            tripNameView.setText("");
            enableControls(false);
        }
        else {
            trackDB = DBProvider.getTrackDB(this, ti.dbFileName);

            tripNameView.setText(ti.tripName);
            enableControls(true);

            dbSink.setDb(trackDB);
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
                startActivityForResult(new Intent(TripSelectorActivity.myIntentName),
                                       TripSelectorActivity.myIntentRequestCode);
            }
        };

    // These are public to make testing easier. You should regard them
    // as private though.
    public SailPlan sp;
    public int mainSailId = -1;
    public int jibId = -1;
    public int spinnakerId = -1;

    private TrackDBInterface trackDB;

    private DBLocationSink dbSink;
    private LocationSinkAdapter dbSinkAdapter;
    private LocationSinkAdapter uiSinkAdapter;

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

    private long eventsSaved = 0;
}
