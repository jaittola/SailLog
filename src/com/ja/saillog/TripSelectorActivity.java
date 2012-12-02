package com.ja.saillog;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteCursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class TripSelectorActivity extends Activity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trip_selector);
        
        createActions();
    }
    
    public void onStart() {
        super.onStart();

        tripDB = new TripDB(this);
        tla = new TripListAdapter(this, tripDB.listTrips(), false);
        tripListView.setAdapter(tla);

        resetSelections();
    }
    
    public void onStop() {
        tripListView.setAdapter(null);

        tla.done();
        tla = null;

        tripDB.close();
        tripDB = null;
                
        super.onStop();
        
        newTripEntry.setText("");
    }
    
    private void createActions() {
        newTripButton = (ImageButton) findViewById(R.id.addNewTripButton);
        newTripEntry = (EditText) findViewById(R.id.newTripName);
        selectButton = (Button) findViewById(R.id.selectButton);
        tripListView = (ListView) findViewById(R.id.tripListView);
        
        newTripButton.setOnClickListener(newTripListener);
        selectButton.setOnClickListener(selectButtonListener);
        
        tripListView.setOnItemClickListener(tripClickedListener);
    }
    
    private void addNewTrip() {
        String newTripName = newTripEntry.getText().toString().trim();
        if (0 == newTripName.length()) {
            toast(getString(R.string.enter_trip_name));
        }
        
        tripDB.insertTrip(newTripName);
        
        tla.requery();
        newTripEntry.setText("");
        resetSelections();
    }
    
    private void resetSelections() {
        nextSelectionId = -1;
        
        selectButton.setClickable(false);
        selectButton.setText("");
    }
    
    private void tripSelected() {
        if (-1 == nextSelectionId) {
            // Should not happen really. Just ignore for now.
            return;
        }
        
        tripDB.selectTrip(nextSelectionId);
        setResult(RESULT_OK);
        finish();
    }
        
   
    private OnClickListener newTripListener = new OnClickListener() {
        public void onClick(View v) {
            addNewTrip();
        }
    };
    
    private OnClickListener selectButtonListener = new OnClickListener() {
        public void onClick(View v) {
            tripSelected();
        }
    };
    
    private OnItemClickListener tripClickedListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
            nextSelectionId = id;

            SQLiteCursor c = (SQLiteCursor) parent.getItemAtPosition(position);
            c.moveToPosition(position);
            selectButton.setText(String.format(getString(R.string.trip_to_select),
                                               c.getString(c.getColumnIndex("trip_name"))));
            selectButton.setClickable(true);
        }
    };  
    
    private void toast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }
    
    private TripDB tripDB;
    private TripListAdapter tla;
    
    private ImageButton newTripButton;
    private EditText newTripEntry;
    private Button selectButton;
    private ListView tripListView;
    
    private long nextSelectionId = -1;
}
