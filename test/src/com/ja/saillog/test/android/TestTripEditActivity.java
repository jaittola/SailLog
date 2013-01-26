package com.ja.saillog.test.android;

import java.text.SimpleDateFormat;

import junit.framework.Assert;
import android.content.Context;
import android.content.Intent;
import android.test.ActivityUnitTestCase;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ja.saillog.R;
import com.ja.saillog.database.DBProvider;
import com.ja.saillog.database.TrackDBInterface;
import com.ja.saillog.database.TripDBInterface;
import com.ja.saillog.quantity.quantity.QuantityFactory;
import com.ja.saillog.test.purejava.FakeTrackDB;
import com.ja.saillog.test.purejava.FakeTripDB;
import com.ja.saillog.ui.TripEditActivity;


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
        runTea(withoutTripId);

        verifyFieldsHaveDefaults();
    }

    public void testSetupWithId() {
        runTea(withTripId);

        Assert.assertEquals(tripdb.aTrip.tripName,
                            tripNameText.getText().toString());
        Assert.assertEquals(tripdb.aTrip.startLocation,
                            fromText.getText().toString());
        Assert.assertEquals(tripdb.aTrip.endLocation,
                            toText.getText().toString());
        Assert.assertEquals(QuantityFactory.nauticalMiles(trackdb.mTotalDistance).withUnit(),
                            totalDistanceText.getText().toString());
        Assert.assertEquals(QuantityFactory.nauticalMiles(trackdb.mTotalDistance).withUnit(),
                            totalDistanceText.getText().toString());
        Assert.assertEquals(QuantityFactory.hourMinSec(trackdb.mSailingTime).withUnit(),
                            totalSailingTimeText.getText().toString());
        Assert.assertEquals(SimpleDateFormat.getDateTimeInstance().format(trackdb.mFirstEntry),
                            startTime.getText().toString());
        Assert.assertEquals(SimpleDateFormat.getDateTimeInstance().format(trackdb.mLastEntry),
                            endTime.getText().toString());
    }

    public void testSetupWithIdNoStartEndTimes() {
        trackdb.mFirstEntry = null;
        trackdb.mLastEntry = null;

        runTea(withTripId);

        // To avoid duplication with testSetupWithId(),
        // we test here only the values that are different.
        Assert.assertEquals("",
                            startTime.getText().toString());
        Assert.assertEquals("",
                            endTime.getText().toString());

    }

    public void testSelectExistingTrip() {
        runTea(withTripId);

        selectButton.performClick();

        Assert.assertEquals(tripdb.aTrip.tripId, tripdb.selectedTripId.longValue());

        Assert.assertTrue(isFinishCalled());
    }


    public void testSaveExistingTrip() {
        tripdb.setupTrips();
        runTea(withTripId);

        String tripName = "My updated trip";
        String startLocation = "Särkän satama";
        String endLocation = "Granlandetin saaritukikohta";

        tripNameText.setText(tripName);
        fromText.setText(startLocation);
        toText.setText(endLocation);

        saveButton.performClick();

        Assert.assertEquals(tripdb.aTrip.tripId, tripdb.updatedTrip.tripId);

        Assert.assertEquals(tripName, tripdb.updatedTrip.tripName);
        Assert.assertEquals(startLocation, tripdb.updatedTrip.startLocation);
        Assert.assertEquals(endLocation, tripdb.updatedTrip.endLocation);

        Assert.assertFalse(isFinishCalled());
    }

    public void testSaveNewTrip() {
        String tripName = "My new trip";
        String startLocation = "Särkkä";
        String endLocation = "Granlandet";

        runTea(withoutTripId);

        tripNameText.setText(tripName);
        fromText.setText(startLocation);
        toText.setText(endLocation);

        saveButton.performClick();

        Assert.assertEquals(tripdb.insertedTrip.tripName, tripName);
        Assert.assertEquals(tripdb.insertedTrip.startLocation, startLocation);
        Assert.assertEquals(tripdb.insertedTrip.endLocation, endLocation);

        Assert.assertFalse(isFinishCalled());
    }

    public void testSaveNewTripWithEmptyDetails() {
        runTea(withoutTripId);

        tripNameText.setText("");

        saveButton.performClick();

        // TODO, how to verify the showing of the confirmation dialog.

        Assert.assertNull(tripdb.insertedTrip);
        Assert.assertNull(tripdb.updatedTrip);

        Assert.assertFalse(isFinishCalled());
    }

    public void testUpdateTripWithEmptyDetails() {
        runTea(withTripId);

        tripNameText.setText("");
        fromText.setText("");
        toText.setText("");

        saveButton.performClick();

        // TODO, how to verify the showing of the confirmation dialog.

        Assert.assertNull(tripdb.insertedTrip);
        Assert.assertNull(tripdb.updatedTrip);

        Assert.assertFalse(isFinishCalled());
    }


    public void testSelectNewTrip() {
        String tripName = "My trip for select";

        runTea(withoutTripId);

        tripNameText.setText(tripName);
        selectButton.performClick();

        Assert.assertEquals(tripName, tripdb.insertedTrip.tripName);
        Assert.assertEquals(tripdb.insertedTrip.tripId, tripdb.selectedTripId.longValue());

        Assert.assertTrue(isFinishCalled());
    }

    public void testDeleteTrip() {
        runTea(withTripId);

        tea.alreadyConfirmed = true;
        deleteButton.performClick();

        Assert.assertEquals(tripdb.aTrip.tripId, tripdb.deletedTripId.longValue());

        Assert.assertTrue(isFinishCalled());
    }

    public void testDeleteWhenEmpty() {
        runTea(withoutTripId);

        deleteButton.performClick();

        Assert.assertNull(tripdb.deletedTripId);
        Assert.assertTrue(isFinishCalled());
    }

    private void runTea(boolean haveTripId) {
        tripdb.setupTrips();

        Intent intent = new Intent();
        if (true == haveTripId) {
            intent.putExtra("tripId", tripdb.aTrip.tripId);
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


        TextView[] myEmptyViewsAtStart = {
            fromText,
            toText,
            startTime,
            endTime,
            totalDistanceText,
            totalEngineTimeText,
            totalSailingTimeText,
        };
        allViews = myEmptyViewsAtStart;
    }

    private void verifyFieldsHaveDefaults() {
        for (TextView v: allViews) {
            Assert.assertEquals(v.getText().toString().length(), 0);
        }

        Assert.assertEquals(tea.defaultTripName(),
                            tripNameText.getText().toString());
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

    private static final boolean withTripId = true;
    private static final boolean withoutTripId = false;
}
