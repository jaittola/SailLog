package com.ja.saillog.test.android;

import junit.framework.Assert;
import android.content.Context;
import android.content.Intent;
import android.test.ActivityUnitTestCase;
import android.widget.Button;
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
         tripdb.setupTrips();
         runTea(tripdb.aTrip.tripId);
         
         Assert.assertEquals(tripdb.aTrip.tripName,
                             tripNameText.getText().toString());
         // TODO, should check the content of the rest of the fields.
     }  
     
     public void testSelectExistingTrip() {
         tripdb.setupTrips();
         runTea(tripdb.aTrip.tripId);
         
         selectButton.performClick();
         
         Assert.assertEquals(tripdb.aTrip.tripId, tripdb.selectedTrip.tripId);
         
         Assert.assertTrue(isFinishCalled());
     }
     
     
     public void testSaveExistingTrip() {
         tripdb.setupTrips();
         runTea(tripdb.aTrip.tripId);
         
         saveButton.performClick();
         
         Assert.assertEquals(tripdb.aTrip.tripId, tripdb.updatedTrip.tripId);

         Assert.assertFalse(isFinishCalled());
     }

     public void testSaveNewTrip() {
         String tripName = "My new trip";
         
         tripdb.setupTrips();
         runTea(null);
         
         tripNameText.setText(tripName);
         saveButton.performClick();
         
         Assert.assertEquals(tripdb.insertedTrip.tripName, tripName);

         Assert.assertFalse(isFinishCalled());
     }

     public void testSaveNewTripWithEmptyDetails() {        
         tripdb.setupTrips();
         runTea(null);

         saveButton.performClick();
         
         // TODO, how to verify the showing of the toast.
         
         Assert.assertNull(tripdb.insertedTrip);
         Assert.assertNull(tripdb.updatedTrip);

         Assert.assertFalse(isFinishCalled());
     }
     
     public void testSelectNewTrip() {
         String tripName = "My trip for select";
         
         tripdb.setupTrips();
         runTea(null);
         
         tripNameText.setText(tripName);
         selectButton.performClick();
         
         Assert.assertEquals(tripName, tripdb.insertedTrip.tripName);
         Assert.assertEquals(tripdb.insertedTrip.tripId, tripdb.selectedTrip.tripId);

         Assert.assertTrue(isFinishCalled());
     }
     
     public void testDeleteTrip() {
         tripdb.setupTrips();
         runTea(tripdb.aTrip.tripId);
         
         tea.alreadyConfirmed = true;
         deleteButton.performClick();
         
         Assert.assertEquals(tripdb.aTrip.tripId, tripdb.deletedTripId.longValue());
         
         Assert.assertTrue(isFinishCalled());
     }

     public void testDeleteWhenEmpty() {
         tripdb.setupTrips();
         runTea(null);

         deleteButton.performClick();

         Assert.assertNull(tripdb.deletedTripId);
         Assert.assertTrue(isFinishCalled());
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
         tripNameText = (EditText) tea.findViewById(R.id.tripNameText);
         fromText = (EditText) tea.findViewById(R.id.fromText);
         toText = (EditText) tea.findViewById(R.id.toText);
         tripNameText = (EditText) tea.findViewById(R.id.legNameText);
         startTime = (EditText) tea.findViewById(R.id.startTime);
         endTime = (EditText) tea.findViewById(R.id.endTime);
         totalDistanceText = (EditText) tea.findViewById(R.id.totalDistanceText);
         totalEngineTimeText = (EditText) tea.findViewById(R.id.totalEngineTimeText);
         totalSailingTimeText = (EditText) tea.findViewById(R.id.totalSailingTimeText);
         selectButton = (Button) tea.findViewById(R.id.selectThisButton);
         saveButton = (Button) tea.findViewById(R.id.saveTripButton);
         deleteButton = (Button) tea.findViewById(R.id.deleteThisButton);
  

         TextView[] myAllViews = {
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

     private TextView [] allViews;

     private FakeTripDB tripdb = new FakeTripDB();
     private FakeTrackDB trackdb = new FakeTrackDB();

     private TripEditActivity tea;
}
