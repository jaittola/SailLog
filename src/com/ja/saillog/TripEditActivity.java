package com.ja.saillog;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ja.saillog.TripDBInterface.TripInfo;

public class TripEditActivity extends SailLogActivityBase {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trip_edit);

        getWidgets();

        selectButton.setOnClickListener(tripSelectClickListener);
        saveButton.setOnClickListener(tripSaveClickListener);
        deleteButton.setOnClickListener(tripDeleteClickListener);
    }

    @Override
    public void onStart() {
        super.onStart();

        tripDB = DBProvider.getTripDB(this);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        Long tripId = null;
        if (null != extras) {
            tripId = extras.getLong("tripId");
        }

        if (null != tripId) {
            // Existing trip: load from database.
            ti = tripDB.getTripById(tripId);

            if (ti != null) {
                tripNameText.setText(ti.tripName);
            }
            else {
                // Well, the trip should exist.
                // Make a new TripInfo structure with this
                // trip id so that the trip gets
                // inserted as a new one (if the user wishes to save).
                ti = new TripInfo(tripId, "", null);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        tripDB.close();
        tripDB = null;

        ti = null;

        tripNameText.setText("");
        fromText.setText("");
        toText.setText("");
        tripNameText.setText("");
        startTime.setText("");
        endTime.setText("");
        totalDistanceText.setText("");
        totalEngineTimeText.setText("");
        totalSailingTimeText.setText("");
    }

    private void getWidgets() {
        tripNameText = (EditText) findViewById(R.id.tripNameText);
        fromText = (EditText) findViewById(R.id.fromText);
        toText = (EditText) findViewById(R.id.toText);
        tripNameText = (EditText) findViewById(R.id.legNameText);
        startTime = (EditText) findViewById(R.id.startTime);
        endTime = (EditText) findViewById(R.id.endTime);
        totalDistanceText = (EditText) findViewById(R.id.totalDistanceText);
        totalEngineTimeText = (EditText) findViewById(R.id.totalEngineTimeText);
        totalSailingTimeText = (EditText) findViewById(R.id.totalSailingTimeText);
        selectButton = (Button) findViewById(R.id.selectThisButton);
        saveButton = (Button) findViewById(R.id.saveTripButton);
        deleteButton = (Button) findViewById(R.id.deleteThisButton);
    }

    private OnClickListener tripSelectClickListener = new OnClickListener() {
        public void onClick(View v) {
            if (false == performSave()) {
                return;
            }
            tripDB.selectTrip(ti.tripId);
            finish();
        }
    };

    private OnClickListener tripSaveClickListener = new OnClickListener() {
        public void onClick(View v) {
            performSave();
        }
    };

    private OnClickListener tripDeleteClickListener = new OnClickListener() {
        public void onClick(View v) {
            if (null != ti) {
                if (false == alreadyConfirmed) {
                    // TODO, display confirmation.
                }
                
                tripDB.deleteTrip(ti.tripId);
            }
            
            finish();
        }
    };

    private boolean performSave() {
        // The trip name (or start and end locations) cannot be empty.
        if (0 == tripNameText.getText().length() &&
            0 == fromText.getText().length() &&
            0 == toText.getText().length()) {
            toast(getString(R.string.trip_all_fields_cannot_be_empty));
            return false;
        }

        if (null == ti) {
            // Insert new.
            ti = tripDB.insertTrip(tripNameText.getText().toString());
        }
        else {
            ti.tripName = tripNameText.getText().toString();
            tripDB.updateTrip(ti);
        }

        return true;
    }

    // A hack for testing. Should be removed if there is a way to confirm
    // a confirmation dialog.
    public boolean alreadyConfirmed = false;

    private EditText tripNameText;
    private EditText fromText;
    private EditText toText;
    private EditText startTime;
    private EditText endTime;
    private EditText totalDistanceText;
    private EditText totalEngineTimeText;
    private EditText totalSailingTimeText;
    private Button selectButton;
    private Button saveButton;
    private Button deleteButton;

    private TripDBInterface tripDB;
    private TripInfo ti;

    final public static int myIntentRequestCode = 2;
    final public static String myIntentName = "com.ja.saillog.tripEdit";
}
