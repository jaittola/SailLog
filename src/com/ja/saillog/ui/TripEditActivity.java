package com.ja.saillog.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.ja.saillog.R;
import com.ja.saillog.R.id;
import com.ja.saillog.R.layout;
import com.ja.saillog.R.string;
import com.ja.saillog.database.DBProvider;
import com.ja.saillog.database.TrackDBInterface;
import com.ja.saillog.database.TripDBInterface;
import com.ja.saillog.database.TrackDBInterface.TripStats;
import com.ja.saillog.database.TripDBInterface.TripInfo;


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
            tripId = extras.getLong(tripIdInIntent);
        }

        if (null != tripId) {
            // Existing trip: load from database.
            ti = tripDB.getTripById(tripId);

            if (ti != null) {
                tripNameText.setText(ti.tripName);
                fromText.setText(ti.startLocation);
                toText.setText(ti.endLocation);

                // TODO, this should update live.
                TrackDBInterface tdb = DBProvider.getTrackDB(this, 
                        ti.dbFileName);
                TripStats ts = tdb.getTripStats();
                tdb.close();
                
                totalDistanceText.setText(String.format("%.1f", ts.distance));
                totalSailingTimeText.setText(String.format("%.1f", ts.sailingTime));
                totalEngineTimeText.setText(String.format("%.1f", ts.engineTime));
            }
            else {
                // Well, the trip should exist.
                // Make a new TripInfo structure with this
                // trip id so that the trip gets
                // inserted as a new one (if the user wishes to save).
                ti = new TripInfo(tripId, "", "", "", null);
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
                done(selectedResult);
            }
        };

    private OnClickListener tripSaveClickListener = new OnClickListener() {
            public void onClick(View v) {
                performSave();
            }
        };

    private OnClickListener tripDeleteClickListener = new OnClickListener() {
            public void onClick(View v) {
                if (null == ti) {
                    setResult(RESULT_OK);
                    finish();
                } else {
                    if (true == alreadyConfirmed) {
                        doDelete();
                    }
                    else {
                        showDeleteConfirmationDialog();
                    }
                }
            }
        };

    private void doDelete() {
        tripDB.deleteTrip(ti.tripId);
        done(RESULT_OK);
    }
    
    private void done(int result) {
        setResult(result);
        finish();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.delete_confirmation))
            .setPositiveButton(getString(R.string.yes), 
                 new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        doDelete();
                    }
                })
            .setNegativeButton(getString(R.string.no), 
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Not confirmed -> do nothing.
                    }
                })
            .create().show();
    }

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
           ti = tripDB.insertTrip(tripNameText.getText().toString(), 
                                   fromText.getText().toString(),
                                   toText.getText().toString());
        }
        else {
            // Update.
            ti.tripName = tripNameText.getText().toString();
            ti.startLocation = fromText.getText().toString();
            ti.endLocation = toText.getText().toString();
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
    final public static String myIntentName = "com.ja.saillog.ui.tripEdit";
    final public static String tripIdInIntent = "tripId";
    final public static int selectedResult = RESULT_FIRST_USER + 1;
}
