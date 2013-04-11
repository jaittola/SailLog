package com.ja.saillog.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
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
import com.ja.saillog.serv.TrackSavingServiceConstants;
import com.ja.saillog.utilities.LocationServiceProvider;
import com.ja.saillog.utilities.LocationSink;
import com.ja.saillog.utilities.LocationSinkAdapter;
import com.ja.saillog.utilities.Propulsion;
import com.ja.saillog.utilities.StaticStrings;

public class SailLogActivity
    extends SailLogActivityBase
    implements LocationSink {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        StaticStrings.setup(this); // App-wide generic setup.

        setupSailPlan();  // TODO: this might be problematic because of the service.
        setupWidgets();

        setupTripInfo();
        setLocationAvailable(false);
    }

    @Override
    public void onStart() {
        super.onStart();
        bindToTrackSaver();
    }

    @Override
    public void onStop() {
        unbindFromTrackSaver();
        super.onStop();
    }

    private void bindToTrackSaver() {
        if (null == trackSaverConnection) {
            trackSaverConnection = new ServiceConnection() {

                public void onServiceConnected(ComponentName name,
                        IBinder binder) {
                    System.err.println("onServiceConnected");
                    trackSaverMessenger = new Messenger(binder);
                    trackSaverConnection = this;
                    
                    // TODO, maybe also disable & enable buttons?
                }

                public void onServiceDisconnected(ComponentName name) {
                    trackSaverMessenger = null;
                    trackSaverConnection = null;
                }
            };
        }
        
        startService(trackSaverIntent);
        bindService(trackSaverIntent, trackSaverConnection, Context.BIND_AUTO_CREATE);
    }

    private void unbindFromTrackSaver() {
        if (null != trackSaverConnection) {
            this.unbindService(trackSaverConnection);
        }
        trackSaverMessenger = null;
        trackSaverConnection = null;
    }

    @Override
    public void onDestroy() {
        if (null != trackDB) {
            trackDB.close();
        }

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
        latView.setText(QuantityFactory.dmsLatitude(latitude).withUnit());
        lonView.setText(QuantityFactory.dmsLongitude(longitude).withUnit());

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
        boolean msgResult = true;

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

                msgResult = sendToTrackSaver(TrackSavingServiceConstants.MSG_START_SAVING);
            }
        }
        else {
            if (null != uiSinkAdapter) {
                LocationServiceProvider.get(this).stopUpdates(uiSinkAdapter);
                uiSinkAdapter = null;
            }

            msgResult = sendToTrackSaver(TrackSavingServiceConstants.MSG_STOP_SAVING);
        }

        if (false == msgResult) {
            toast(getString(R.string.start_saving_failed));
            isChecked = false;
        }

        enablePropulsionControls(isChecked);
        setLocationAvailable(isChecked);
    }

    private boolean sendToTrackSaver(Message message) {
        if (null == trackSaverMessenger) {
            System.err.println("Messenger is null");
            return false;
        }
        
        try {
            trackSaverMessenger.send(message);
            System.err.println("Message sent, type " + message.what);
            return true;
        } catch (RemoteException rex) {
            System.err.println("Exception in remote operation: " + rex);
            return false;
        }
    }
    
    private boolean sendToTrackSaver(int msgType) {
       return sendToTrackSaver(Message.obtain(null, msgType));
    }

    private void sailingEvents() {
        propulsion.setEngine(engineStatusCheckbox.isChecked());
        propulsion.setSail(mainSailId, mainSailCheckbox.isChecked());
        propulsion.setSail(jibId, jibCheckbox.isChecked());
        propulsion.setSail(spinnakerId, spinnakerCheckbox.isChecked());

        Message msg = Message.obtain(null, TrackSavingServiceConstants.MSG_CHANGE_PROPULSION);
        msg.setData(propulsion.toBundle());
        sendToTrackSaver(msg);
    }

    private void setupSailPlan() {
        mainSailId = Propulsion.addSail(getString(R.string.main_sail));
        jibId = Propulsion.addSail(getString(R.string.jib));
        spinnakerId = Propulsion.addSail(getString(R.string.spinnaker));

        propulsion = new Propulsion();
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


    public void onActivityResult(int requestCode,
                                 int resultCode,
                                 Intent data) {
        if (TripSelectorActivity.myIntentRequestCode == requestCode) {
            setupTripInfo();
        }
    }

    private void setupTripInfo() {

        TripDBInterface tripDB = DBProvider.getTripDB(this);
        TripInfo ti = tripDB.getActiveTrip();

        if (null == ti) {
            tripNameView.setText("");
            sendToTrackSaver(TrackSavingServiceConstants.MSG_STOP_SAVING);
            enableControls(false);
        }
        else {
            if (null == activeTrip ||
                false == activeTrip.isSame(ti)) {

                enableControls(false);
                
                // Stop saving location info.
                if (null != trackDB) {
                    sailingEvents();
                    sendToTrackSaver(TrackSavingServiceConstants.MSG_STOP_SAVING);
                    trackDB.close();
                    trackDB = null;
                }

                trackDB = DBProvider.getTrackDB(this, ti.dbFileName);

                enableControls(true);
            }

            // Update trip name always, it could have been changed.
            tripNameView.setText(ti.tripName);
        }

        activeTrip = ti;
        tripDB.close();
    }

    private void enableControls(boolean enabled) {
        if (false == enabled) {
            trackLocationButton.setChecked(false);
        }

        trackLocationButton.setEnabled(enabled);
        enablePropulsionControls(false);  // Propulsion widgets get enabled
                                          // only after enabling track saving.
    }

    private void enablePropulsionControls(boolean enabled) {
        engineStatusCheckbox.setChecked(false);
        mainSailCheckbox.setChecked(false);
        jibCheckbox.setChecked(false);
        spinnakerCheckbox.setChecked(false);

        engineStatusCheckbox.setEnabled(enabled);
        mainSailCheckbox.setEnabled(enabled);
        jibCheckbox.setEnabled(enabled);
        spinnakerCheckbox.setEnabled(enabled);
    }

    private OnCheckedChangeListener locationTrackStartListener = new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sailingEvents();
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
                sailingEvents();
                startActivityForResult(new Intent(TripSelectorActivity.myIntentName),
                                       TripSelectorActivity.myIntentRequestCode);
            }
        };

    // These are public to make testing easier. You should regard them
    // as private though.
    public Propulsion propulsion;
    public int mainSailId = -1;
    public int jibId = -1;
    public int spinnakerId = -1;

    private TrackDBInterface trackDB;
    private TripInfo activeTrip;

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

    private Intent trackSaverIntent = new Intent(TrackSavingServiceConstants.intentName);
    private ServiceConnection trackSaverConnection;
    private Messenger trackSaverMessenger;
}
