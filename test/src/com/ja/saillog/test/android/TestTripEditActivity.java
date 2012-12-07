package com.ja.saillog.test.android;

import junit.framework.Assert;
import android.content.Context;
import android.content.Intent;
import android.test.ActivityUnitTestCase;
import android.widget.EditText;
import android.widget.TextView;

import com.ja.saillog.DBProvider;
import com.ja.saillog.R;
import com.ja.saillog.TrackDBInterface;
import com.ja.saillog.TripDBInterface;
import com.ja.saillog.TripEditActivity;
import com.ja.saillog.test.purejava.FakeTrackDB;
import com.ja.saillog.test.purejava.FakeTripDB;

public class TestTripEditActivity extends ActivityUnitTestCase<TripEditActivity> {

    public TestTripEditActivity() {
        super(TripEditActivity.class);
    }

    protected void setUp() throws Exception {
        DBProvider.setProvider(new DBProvider() {
                 protected TripDBInterface getTripDBInstance(Context context) {
                     return tripdb;
                 }
                 protected TrackDBInterface getTrackDBInstance(Context context, String dbname) {
                     return trackdb;
                 }
             });

         super.setUp();
     }

     protected void tearDown() throws Exception {
         super.tearDown();
     }

     public void testSetupFromEmpty() {
         runTea(null);

         for (TextView v: allViews) {
             Assert.assertEquals(v.getText().toString().length(), 0);
         }
     }
     
     public void testSetupWithId() {
         tripdb.setupTrip();

         runTea(tripdb.selectedTi.tripId);
         
         Assert.assertEquals(tripdb.selectedTi.tripName,
                             tripDisplayName.getText().toString());
         Assert.assertEquals(tripdb.selectedTi.tripName,
                             tripNameText.getText().toString());
         // TODO, should check the content of the rest of the fields.
     }  
     
     private void runTea(Long tripId) {
         Intent intent = new Intent();
         if (null != tripId) {
             intent.putExtra("tripId", tripId.longValue());
         }
         tea = startActivity(intent, null, null);
         tea.onStart();
         
         findViews();
     }

     private void findViews() {
         tripDisplayName = (TextView) tea.findViewById(R.id.tripDisplayName);
         tripNameText = (EditText) tea.findViewById(R.id.tripNameText);
         fromText = (EditText) tea.findViewById(R.id.fromText);
         toText = (EditText) tea.findViewById(R.id.toText);
         tripNameText = (EditText) tea.findViewById(R.id.legNameText);
         startTime = (EditText) tea.findViewById(R.id.startTime);
         endTime = (EditText) tea.findViewById(R.id.endTime);
         totalDistanceText = (EditText) tea.findViewById(R.id.totalDistanceText);
         totalEngineTimeText = (EditText) tea.findViewById(R.id.totalEngineTimeText);
         totalSailingTimeText = (EditText) tea.findViewById(R.id.totalSailingTimeText);

         TextView[] myAllViews = {
             tripDisplayName,
             tripNameText,
             fromText,
             toText,
             startTime,
             endTime,
             totalDistanceText,
             totalEngineTimeText,
             totalSailingTimeText,
         };
         allViews = myAllViews;
     }

     private TextView tripDisplayName;
     private EditText tripNameText;
     private EditText fromText;
     private EditText toText;
     private EditText startTime;
     private EditText endTime;
     private EditText totalDistanceText;
     private EditText totalEngineTimeText;
     private EditText totalSailingTimeText;

     private TextView [] allViews;

     private FakeTripDB tripdb = new FakeTripDB();
     private FakeTrackDB trackdb = new FakeTrackDB();

     private TripEditActivity tea;
}
