package com.ja.saillog;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ja.saillog.TripDBInterface.TripInfo;

public class TripEditActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trip_edit);

        getWidgets();
        
        selectButton.setOnClickListener(tripSelectClickListener);
    }

    @Override
    public void onStart() {
        super.onStart();

        tripDB = DBProvider.getTripDB(this);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (null != extras) {
            tripId = extras.getLong("tripId");
        }

        if (null != tripId) {
            // Existing trip: load from database.
            ti = tripDB.getTripById(tripId);

            if (ti == null) {
                // Well the trip should exist.
                // Clear up the tripId so that the trip gets
                // inserted as a new one (if the user wishes to save).
                tripId = null;
            }
            else {
                tripNameText.setText(ti.tripName);

                setDisplayName(ti.tripName);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        tripDB.close();
        tripDB = null;

        tripId = null;
    }

    private void setDisplayName(String displayName) {
        tripDisplayName.setText(displayName);
    }

    private void getWidgets() {
        tripDisplayName = (TextView) findViewById(R.id.tripDisplayName);
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
    }

    private OnClickListener tripSelectClickListener = new OnClickListener() {
        public void onClick(View v) {
            // TODO, does not work if there is no id.
            tripDB.selectTrip(tripId);
        }
    };

    
    private TextView tripDisplayName;
    private EditText tripNameText;
    private EditText fromText;
    private EditText toText;
    private EditText startTime;
    private EditText endTime;
    private EditText totalDistanceText;
    private EditText totalEngineTimeText;
    private EditText totalSailingTimeText;
    private Button selectButton;

    private TripDBInterface tripDB;
    private Long tripId;
    private TripInfo ti;
}
