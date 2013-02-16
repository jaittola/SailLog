package com.ja.saillog.test.android;

import junit.framework.Assert;
import android.content.Context;
import android.content.Intent;
import android.test.ActivityUnitTestCase;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.ja.saillog.R;
import com.ja.saillog.database.DBProvider;
import com.ja.saillog.database.TrackDBInterface;
import com.ja.saillog.database.TripDB;
import com.ja.saillog.database.TripDBInterface;
import com.ja.saillog.quantity.quantity.QuantityFactory;
import com.ja.saillog.test.purejava.FakeTrackDB;
import com.ja.saillog.test.purejava.FakeTripDB;
import com.ja.saillog.test.purejava.SailPlanTestHelper;
import com.ja.saillog.ui.SailLogActivity;
import com.ja.saillog.ui.TripSelectorActivity;
import com.ja.saillog.utilities.Propulsion;


/*!
 * This class contains some tests for the SailLogActivity class.
 *
 * This is by no means a comprehensive test class. The cases herein
 * cover only some aspects of the functionality.
 *
 * TODO, we could check that the events go correctly
 * to the db using a fake sink.
 */
public class TestSailLogActivity extends ActivityUnitTestCase<SailLogActivity> {

    public TestSailLogActivity() {
        super(SailLogActivity.class);
    }

    protected void setUp() throws Exception {
        tripdb = new FakeTripDB();
        trackdb = new FakeTrackDB();
        prevTrackdb = null;

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
        Propulsion.clearSails();
        tripdb = null;
        trackdb = null;
        prevTrackdb = null;

        super.tearDown();
    }

    public void testUIStateWhenNoTrip() {
        // Verify that the buttons are disabled when
        // we do not have a trip.
        runSl(withoutTrip);

        ensureButtonsEnabled(false);
        ensureStaticWidgetStates();
    }

    public void testUIStateWhenHaveTrip() {
        // Verify that the buttons are in right
        // states when we have a trip.
        runSl(withTrip);

        ensureButtonsEnabled(true);
        ensureStaticWidgetStates();
    }

    public void testLocationDataReception() {
        // Check that updated location data values
        // end up in the right fields.
        runSl(withTrip);

        trackLocationButton.performClick();
        Assert.assertTrue(trackLocationButton.isChecked());

        // Expected values for the location update.
        // Various oddities herein:
        //  * We do not check the lat/lon formats very throughly
        //    as there are separate tests for that
        //    (see LocationFormatter).
        //  * Speed has hard-coded units. TODO.
        double speed = 42;
        double heading = 351;
        double lat = 60;
        double lon = 25;
        String expSpeedStr = String.format("%.1f kn", speed * 1.943844);
        String expHeadingStr = String.format("%.0f¡", heading);
        String expLatStr = String.format("N %.0f¡ ", lat);
        String expLonStr = String.format("E %.0f¡ ", lon);

        sl.updateLocation(lat, lon,
                          QuantityFactory.metersPerSecond(42), heading, 21, 3);

        Assert.assertEquals(expSpeedStr, speedText.getText().toString());
        Assert.assertEquals(expHeadingStr, headingText.getText().toString());
        Assert.assertTrue(String.format("Got latitude %s, expected to begin with %s",
                                        latText.getText().toString(), expLatStr),
                          latText.getText().toString().startsWith(expLatStr));
        Assert.assertTrue(String.format("Got longitude %s, expected to begin with %s",
                                        lonText.getText().toString(), expLonStr),
                          lonText.getText().toString().startsWith(expLonStr));

        // Disable location reception, ensure that fields get emptied.
        trackLocationButton.performClick();
        Assert.assertFalse(trackLocationButton.isChecked());

        Assert.assertTrue(speedText.getText().toString().length() == 0);
        Assert.assertTrue(headingText.getText().toString().length() == 0);
        Assert.assertTrue(latText.getText().toString().length() == 0);
        Assert.assertTrue(lonText.getText().toString().length() == 0);
    }

    public void testTripSelectionWithTextField() {
        runSl(withoutTrip);

        tripNameText.performClick();

        Assert.assertEquals(TripSelectorActivity.myIntentRequestCode,
                            getStartedActivityRequest());
        Assert.assertEquals(TripSelectorActivity.myIntentName,
                            getStartedActivityIntent().getAction());
    }

    // This shows a testing anti-pattern: one case
    // should not verify many different cases.
    // This stuff is rather simple and hence
    // an exception here.
    public void testSailEngineEvents() {
        runSl(withTrip);

        mainButton.performClick();
        verifyEventsDb(0, Propulsion.up, Propulsion.down, Propulsion.down);

        jibButton.performClick();
        verifyEventsDb(0, Propulsion.up, Propulsion.up, Propulsion.down);

        spinnakerButton.performClick();
        verifyEventsDb(0, Propulsion.up, Propulsion.up, Propulsion.up);

        jibButton.performClick();
        verifyEventsDb(0, Propulsion.up, Propulsion.down, Propulsion.up);

        spinnakerButton.performClick();
        engineStatusButton.performClick();
        verifyEventsDb(1, Propulsion.up, Propulsion.down, Propulsion.down);
    }

    // Test returning from the trip selection views
    // with the trip changed.
    public void testTripChange() {
        simulateTripListViewVisitAndVerify(FakeTripDB.defaultSelectedTripId + 1,
                                           "somethingtoo");

        // The trip database must have been closed and a new one
        // been taken into use.
        Assert.assertTrue(prevTrackdb.isClosed);
    }

    // Test returning from the trip selection views
    // without changing the current selected trip.
    public void testTripSelectionViewReturnSameTrip() {

        simulateTripListViewVisitAndVerify(null, "something");

        Assert.assertEquals(tripdb.selectedTripName(),
                            tripNameText.getText().toString());
    }

    private void simulateTripListViewVisitAndVerify(Long newSelectedTripId,
                                                    String newNameId) {

        runSl(withTrip);

        // Make these selections checked.
        trackLocationButton.performClick();
        engineStatusButton.performClick();

        // Simulate new trip selection, and going and returning from another view.
        if (null != newSelectedTripId) {
            tripdb.selectTrip(newSelectedTripId);
            prevTrackdb = trackdb;
            trackdb = new FakeTrackDB();
        }
        if (null != newNameId) {
            tripdb.selectedTripNameIdentifier = newNameId;
        }

        sl.onActivityResult(TripSelectorActivity.myIntentRequestCode, -1, null);

        // The buttons must keep the same states as before.
        Assert.assertTrue(trackLocationButton.isChecked());
        Assert.assertTrue(engineStatusButton.isChecked());

        // Close must not have been called for the current track db:
        //  - if the track db was changed, the current one is the new one,
        //    and it must  be open.
        //  - if it was not closed, the same one must be in use and remain
        //    open.
        Assert.assertFalse(trackdb.isClosed);
    }

    private void verifyEventsDb(int engine,
                                boolean mainUp,
                                boolean jibUp,
                                boolean spinUp) {
        th.verifySailPlan(mainUp, jibUp, spinUp);
        Assert.assertEquals(th.sp.getSailPlan(), trackdb.mSailPlan);
        Assert.assertEquals(engine, trackdb.mEngineStatus);
    }

    private void runSl(boolean haveTrip) {
        if (withTrip == haveTrip) {
            tripdb.setupTrips();
        }
        sl = startActivity(new Intent(), null, null);

        // Check own consistency.
        if (withTrip == haveTrip) {
            Assert.assertNotNull(DBProvider.getTripDB(sl).getActiveTrip());
        }
        else {
            Assert.assertNull(DBProvider.getTripDB(sl).getActiveTrip());
        }

        findViews();

        setupSailPlanTestHelper();
    }

    private void setupSailPlanTestHelper() {
        th = new SailPlanTestHelper(sl.getString(R.string.main_sail),
                                    sl.getString(R.string.jib),
                                    sl.getString(R.string.spinnaker),
                                    sl.getString(R.string.sail_up),
                                    sl.getString(R.string.sail_down),
                                    sl.mainSailId,
                                    sl.jibId,
                                    sl.spinnakerId,
                                    sl.sp);
    }

    private void ensureStaticWidgetStates() {
        // These widgets must not change their states (readability,
        // editability, and so on).
        Assert.assertTrue(tripNameText.isClickable());
        Assert.assertFalse(tripNameText.isFocusable());

        View [] gpsDataFields = {
            speedText,
            headingText,
            latText,
            lonText,
        };

        int item = 0;
        for (View view: gpsDataFields) {
            Assert.assertEquals(String.format("View %d (%dth item) has wrong enable status",
                                              view.getId(), item),
                                true, view.isEnabled());
            Assert.assertEquals(String.format("View %d (%dth item) has wrong clickability status",
                                              view.getId(), item),
                                false, view.isClickable());
            Assert.assertEquals(String.format("View %d (%dth item) has wrong focusability status",
                                              view.getId(), item),
                                false, view.isFocusable());
            ++item;
        }
    }

    private void ensureButtonsEnabled(boolean canBeClicked) {
        Assert.assertEquals(canBeClicked, trackLocationButton.isEnabled());
        Assert.assertEquals(canBeClicked, engineStatusButton.isEnabled());
        Assert.assertEquals(canBeClicked, mainButton.isEnabled());
        Assert.assertEquals(canBeClicked, jibButton.isEnabled());
        Assert.assertEquals(canBeClicked, spinnakerButton.isEnabled());
    }

    private void findViews() {
        tripNameText = (EditText) sl.findViewById(R.id.tripNameText);
        trackLocationButton = (CheckBox)
            sl.findViewById(R.id.trackLocationButton);
        engineStatusButton = (CheckBox)
            sl.findViewById(R.id.engineStatusButton);
        mainButton = (CheckBox) sl.findViewById(R.id.mainSailCheckbox);
        jibButton = (CheckBox) sl.findViewById(R.id.jibCheckbox);
        spinnakerButton = (CheckBox) sl.findViewById(R.id.spinnakerCheckbox);
        speedText = (EditText) sl.findViewById(R.id.speedText);
        headingText = (EditText) sl.findViewById(R.id.headingText);
        latText = (EditText) sl.findViewById(R.id.latText);
        lonText = (EditText) sl.findViewById(R.id.lonText);
    }

    private SailLogActivity sl = null;
    private FakeTripDB tripdb = null;
    private FakeTrackDB trackdb = null;
    private FakeTrackDB prevTrackdb = null;

    private EditText tripNameText;
    private CheckBox trackLocationButton;
    private CheckBox engineStatusButton;
    private CheckBox mainButton;
    private CheckBox jibButton;
    private CheckBox spinnakerButton;
    private EditText speedText;
    private EditText headingText;
    private EditText latText;
    private EditText lonText;

    private SailPlanTestHelper th;

    public static boolean withTrip = true;
    public static boolean withoutTrip = false;
}
