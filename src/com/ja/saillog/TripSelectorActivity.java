package com.ja.saillog;

import android.content.Intent;
import android.database.sqlite.SQLiteCursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;

public class TripSelectorActivity extends SailLogActivityBase {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trip_selector);

        newTripButton = (ImageButton) findViewById(R.id.addNewTripButton);
        tripListView = (ListView) findViewById(R.id.tripListView);

        newTripButton.setOnClickListener(newTripListener);
        tripListView.setOnItemClickListener(tripClickedListener);
    }

    public void onStart() {
        super.onStart();

        tripDB = new TripDB(this);
        tla = new TripListAdapter(this, tripDB.listTrips(), false);
        tripListView.setAdapter(tla);
    }

    public void onStop() {
        tripListView.setAdapter(null);

        tla.done();
        tla = null;

        tripDB.close();
        tripDB = null;

        super.onStop();
    }

    private OnClickListener newTripListener = new OnClickListener() {
            public void onClick(View v) {
                startActivityForResult(new Intent(TripEditActivity.myIntentName),
                                       TripEditActivity.myIntentRequestCode);
            }
        };

    private OnItemClickListener tripClickedListener = new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent,
                                    View view,
                                    int position,
                                    long id) {
                SQLiteCursor c = (SQLiteCursor) parent.getItemAtPosition(position);
                c.moveToPosition(position);

                Intent intent = new Intent(TripEditActivity.myIntentName);
                intent.putExtra(TripEditActivity.tripIdInIntent,
                                c.getLong(c.getColumnIndex("trip_id")));

                startActivityForResult(intent, TripEditActivity.myIntentRequestCode);
            }
        };

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (TripEditActivity.myIntentRequestCode == requestCode &&
            TripEditActivity.selectedResult == resultCode) {
            finish();
        }
    }

    private TripDB tripDB;
    private TripListAdapter tla;

    private ImageButton newTripButton;
    private ListView tripListView;

    final public static int myIntentRequestCode = 1;
    final public static String myIntentName = "com.ja.saillog.tripSelector";
}
