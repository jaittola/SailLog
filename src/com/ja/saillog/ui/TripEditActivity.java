package com.ja.saillog.ui;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.ja.saillog.R;
import com.ja.saillog.database.DBProvider;
import com.ja.saillog.database.TrackDBInterface;
import com.ja.saillog.database.TrackDBInterface.TripStats;
import com.ja.saillog.database.TripDBInterface;
import com.ja.saillog.database.TripDBInterface.TripInfo;
import com.ja.saillog.quantity.quantity.QuantityFactory;
import com.ja.saillog.utilities.ExportFile;


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
    public void onDestroy() {
        super.onDestroy();
        
        deleteTemporaryFiles();
    }
    
    @Override
    public void onStart() {
        super.onStart();

        deleteTemporaryFiles();
        
        tripDB = DBProvider.getTripDB(this);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        Long tripId = null;
        if (null != extras) {
            tripId = extras.getLong(tripIdInIntent);
        }

        if (null == tripId) {
            // No trip. Put a default string there.
            tripNameText.setText(defaultTripName());
        }
        else {
            // Existing trip: load from database.
            ti = tripDB.getTripById(tripId);

            if (ti != null) {
                tripNameText.setText(ti.tripName);
                fromText.setText(ti.startLocation);
                toText.setText(ti.endLocation);

                // TODO, this should update live.
                trackDB = DBProvider.getTrackDB(this,
                                                ti.dbFileName);
                TripStats ts = trackDB.getTripStats();

                totalDistanceText.setText(QuantityFactory
                                          .nauticalMiles(ts.distance)
                                          .withUnit());
                totalSailingTimeText.setText(QuantityFactory.hourMinSec(ts.sailingTime).withUnit());
                totalEngineTimeText.setText(QuantityFactory.hourMinSec(ts.engineTime).withUnit());
                
                if (null != ts.firstEntry) {
                    startTime.setText(SimpleDateFormat.getDateTimeInstance().format(ts.firstEntry));
                }
                if (null != ts.lastEntry) {
                    endTime.setText(SimpleDateFormat.getDateTimeInstance().format(ts.lastEntry));
                }
            }
            else {
                // Well, the trip should exist.
                // Make a new TripInfo structure with this
                // trip id so that the trip gets
                // inserted as a new one (if the user wishes to save).
                ti = new TripInfo(tripId, defaultTripName(), "", "", null, TripInfo.notSelected);
            }
        }
            
        setDeleteButtonState();
    }

    @Override
    protected void onStop() {
        super.onStop();

        tripDB.close();
        tripDB = null;

        if (null != trackDB) {
            trackDB.close();
        }
        trackDB = null;

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

    private void deleteTemporaryFiles() {
        
        for (String filename: temporaryFiles) {
            new File(filename).delete();
        }
 
        temporaryFiles.clear();
    }
    
    private void getWidgets() {
        tripNameText = (EditText) findViewById(R.id.legNameText);
        fromText = (EditText) findViewById(R.id.fromText);
        toText = (EditText) findViewById(R.id.toText);
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
                    // Deleting the selected trip is not permitted.
                    // Verify here again.
                    if (TripInfo.selected == ti.selectionStatus) {
                        toast(getString(R.string.cannot_delete_selected));
                        return;
                    }
                    
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
                                   public void onClick(DialogInterface dialog,
                                                       int id) {
                                       doDelete();
                                   }
                               })
            .setNegativeButton(getString(R.string.no),
                               new DialogInterface.OnClickListener() {
                                   public void onClick(DialogInterface dialog,
                                                       int id) {
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

        setDeleteButtonState();
        
        return true;
    }

    public String defaultTripName() {
        return DateFormat.getDateInstance().format(new Date());
    }
    
    private void setDeleteButtonState() {
        // You can delete only if the trip is not selected 
        // (i.e., not saving current data into it).
        if (null != ti &&
            TripInfo.notSelected == ti.selectionStatus) {
            deleteButton.setEnabled(true);
        }
        else {
            deleteButton.setEnabled(false);
        }
    }
    
    // Menu below.
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.export_db:
            exportData();
            return true;
        case R.id.export_kml:
            exportDataAsKML();
            return true;
        case R.id.export_and_open_google_earth:
            exportAndShowInGoogleEarth();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private abstract class ExportDbTask extends AsyncTask<Void, Void, String> {

        public ExportDbTask() {
            super();
        }

        @Override
        protected void onPreExecute() {
            if (true == exporting) {
                preExecError = getString(R.string.already_exporting);
            }
            
            if (false == exportFile.isExportDirAvailable()) {
                preExecError = getString(R.string.export_no_mmc);
                return;
            }

            if (null == trackDB) {
                preExecError = getString(R.string.nothing_to_export);
                return;
            }
            
            exporting = true;
        }

        @Override
        protected String doInBackground(Void... ignore) {
            if (null != preExecError) {
                return preExecError;
            }

            try {
                doExport();
            } catch (IOException ex) {
                return String.format(getString(R.string.gen_export_failed),
                                     ex.getLocalizedMessage());
            }

            return String.format(getString(R.string.export_ok), 
                                 exportFile.fileName());
        }

        @Override
        protected void onPostExecute(String result) {
            toast(result);
            exporting = false;
        }

        protected abstract void doExport() throws IOException;

        protected ExportFile exportFile;
        protected String preExecError;
    }

    private class ExportDbAsSQLiteTask extends ExportDbTask {
        public ExportDbAsSQLiteTask() {
            super();
            exportFile = new ExportFile("db");
        }

        protected void doExport() throws IOException {
            trackDB.exportDbAsSQLite(exportFile);
        }
    }

    private class ExportDbAsKMLTask extends ExportDbTask {
        public ExportDbAsKMLTask() {
            super();
            exportFile = new ExportFile("kml", "kmz");
        }

        protected void doExport() throws IOException {
            trackDB.exportDbAsKML(exportFile);
        }
    }

    private class ExportGoogleEarthTask extends ExportDbAsKMLTask {
        @Override
        protected String doInBackground(Void... ignore) {
            if (null != preExecError) {
                return preExecError;
            }

            try {
                doExport();
            } catch (IOException ex) {
                return String.format(getString(R.string.gen_export_failed),
                                     ex.getLocalizedMessage());
            }

            return String.format(getString(R.string.starting_google_earth));
        }
        
        protected void doExport() throws IOException {
            // Write to file.
            super.doExport();
            
            // Put the file to the temp files list so it gets
            // wiped out.
            temporaryFiles.add(exportFile.compressedFileName());
            
            // Launch earth.
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(new File(exportFile.compressedFileName())),
                                  "application/vnd.google-earth.kmz");
            startActivity(intent);
        }
    }
    
    private void exportData() {
        new ExportDbAsSQLiteTask().execute();
    }

    private void exportDataAsKML() {
        new ExportDbAsKMLTask().execute();
    }
    
    private void exportAndShowInGoogleEarth() {
        new ExportGoogleEarthTask().execute();
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

    private TripDBInterface tripDB = null;
    private TripInfo ti;
    private TrackDBInterface trackDB = null;
    private boolean exporting = false;
    
    private List<String> temporaryFiles = new LinkedList<String>();

    final public static int myIntentRequestCode = 2;
    final public static String myIntentName = "com.ja.saillog.ui.tripEdit";
    final public static String tripIdInIntent = "tripId";
    final public static int selectedResult = RESULT_FIRST_USER + 1;
}
