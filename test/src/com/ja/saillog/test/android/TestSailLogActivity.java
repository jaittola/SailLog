package com.ja.saillog.test.android;

import junit.framework.Assert;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.test.ActivityUnitTestCase;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;

import com.ja.saillog.DBProvider;
import com.ja.saillog.R;
import com.ja.saillog.SailLogActivity;
import com.ja.saillog.TrackDBInterface;
import com.ja.saillog.TripDBInterface;
import com.ja.saillog.TripSelectorActivity;
import com.ja.saillog.test.purejava.FakeTrackDB;
import com.ja.saillog.test.purejava.FakeTripDB;

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

        Location loc = new Location("xyzzy");
        loc.setSpeed((float) speed);
        loc.setBearing((float) heading);
        loc.setLatitude(lat);
        loc.setLongitude(lon);
        sl.locationTracker.onLocationChanged(loc);

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
        verifyTripSelectionStart();
    }
        
    public void verifyTripSelectionStart() {
        Assert.assertEquals(TripSelectorActivity.myIntentRequestCode, getStartedActivityRequest());
        Assert.assertEquals(TripSelectorActivity.myIntentName, getStartedActivityIntent().getAction());     
    }

    private void runSl(boolean haveTrip) {
        if (withTrip == haveTrip) {
            tripdb.setupTrips();
        }
        sl = startActivity(new Intent(), null, null);

        // Check own consistency.
        if (withTrip == haveTrip) {
            Assert.assertNotNull(DBProvider.getTripDB(sl).getSelectedTrip());
        }
        else {
            Assert.assertNull(DBProvider.getTripDB(sl).getSelectedTrip());
        }

        findViews();
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
        int [] buttonIds = {
           R.id.trackLocationButton,
           R.id.engineStatusButton,
           R.id.mainSailCheckbox,
           R.id.jibCheckbox,
           R.id.spinnakerCheckbox,
        };

        View view = null;

        for (int i = 0; i < buttonIds.length; ++i) {
            view = sl.findViewById(buttonIds[i]);
            Assert.assertNotNull(String.format("View for ID %d (%d th item) not found", buttonIds[i], i), view);
            Assert.assertEquals(String.format("View for ID %d (%d th item) has wrong enable status",
                                              buttonIds[i], i),
                                canBeClicked, view.isEnabled());
        }
   }

    private void findViews() {
        tripNameText = (EditText) sl.findViewById(R.id.tripNameText);
        trackLocationButton = (CheckBox) sl.findViewById(R.id.trackLocationButton);
        speedText = (EditText) sl.findViewById(R.id.speedText);
        headingText = (EditText) sl.findViewById(R.id.headingText);
        latText = (EditText) sl.findViewById(R.id.latText);
        lonText = (EditText) sl.findViewById(R.id.lonText);
    }

    SailLogActivity sl;
    FakeTripDB tripdb = new FakeTripDB();
    FakeTrackDB trackdb = new FakeTrackDB();

    EditText tripNameText;
    ImageButton selectTripButton;
    CheckBox trackLocationButton;
    EditText speedText;
    EditText headingText;
    EditText latText;
    EditText lonText;

    public static boolean withTrip = true;
    public static boolean withoutTrip = false;
}
