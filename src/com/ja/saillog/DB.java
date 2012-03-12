package com.ja.saillog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.util.ResourceBundle;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

public class DB extends SQLiteOpenHelper implements DBInterface {
	
    public DB(Context context, String databaseName) {
        super(context, databaseName, null, dbVersion);
        sqlBundle = ResourceBundle.getBundle("com.ja.saillog.sql");
    }
    
    @Override
    protected void finalize() throws Throwable {
    	if (null != insertPosStm) {
    		insertPosStm.close();
    	}
    	if (null != insertTripStm) {
    		insertTripStm.close();
    	}
    	
    	super.finalize();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    	// The strings in this array refer to SQL clauses
    	// in the sql.properties file.
    	String[] statements = {
    			"set_vacuum",
    			"drop_trip",
    			"create_trip",
    			"drop_pos",
    			"create_pos",
    			"drop_event",
    			"create_event",
    	};
 
       for (String s: statements) {
        	db.execSQL(sqlBundle.getString(s));
        }
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
    
    public void insertTrip(String tripName) {
    	SQLiteDatabase db = getWritableDatabase();
    	
    	if (null == insertTripStm) {
    		insertTripStm = db.compileStatement("INSERT INTO trip (trip_name) VALUES (?)");
    	}
    	
    	try {
    		db.beginTransaction();
    		insertTripStm.bindString(1, tripName);
    		insertTripStm.executeInsert();
    		db.setTransactionSuccessful();
    	} finally {
    		db.endTransaction();
    	}
    }
    
    public int fetchTripId(String tripName) {
    	int id = -1;
    	
    	SQLiteDatabase db = getReadableDatabase();
    	String [] selectionArgs = { tripName };
    	Cursor c = db.rawQuery("SELECT MAX(trip_id) FROM trip WHERE trip_name = ?", selectionArgs);
    	if (true == c.moveToNext()) {
    		if (!c.isNull(0)) {
    			id = c.getInt(0);
    		}
    	}
    	c.close();
    	  	
    	return id;
    }
    
    public void insertPosition(int tripId, double latitude, double longitude, double bearing, double speed) {
    	SQLiteDatabase db = getWritableDatabase();
    	
    	if (null == insertPosStm) {
    		insertPosStm = db.compileStatement("INSERT INTO position (trip_id, latitude, longitude, speed, bearing) " +
			  		   						   "VALUES (?, ?, ?, ?, ?)");
    	}
    	
    	try {
    		db.beginTransaction();
    		insertPosStm.bindLong(1, (long) tripId);
    		insertPosStm.bindDouble(2, latitude);
    		insertPosStm.bindDouble(3, longitude);
    		insertPosStm.bindDouble(4, speed);
    		insertPosStm.bindDouble(5, bearing);
    		insertPosStm.executeInsert();
    		db.setTransactionSuccessful();
    	} finally {
    		db.endTransaction();
    	}
    }
   
    public long fetchLastPositionId(SQLiteDatabase dbToUse) {

        SQLiteDatabase db = dbToUse;
        if (null == db) {
            db = getReadableDatabase();
        }       
        
        if (null == fetchLastPositionStm) {
            fetchLastPositionStm = db.compileStatement("SELECT MAX(position_id) from position");
        }
        
        long lastPosId = fetchLastPositionStm.simpleQueryForLong();
        
        return lastPosId;
    }
    
    public void insertEvent(int tripId, int engineStatus, int sailPlan) {
       
        SQLiteDatabase db = getWritableDatabase();

        if (null == insertEventStm) {
            insertEventStm = db.compileStatement("INSERT INTO event (trip_id, position_id, engine, sailplan) " +
            		"VALUES (?, ?, ?, ?)");
        }
        
        try {
            db.beginTransaction();

            long lastPosId = fetchLastPositionId(db);
            
            insertEventStm.bindLong(1, tripId);
            insertEventStm.bindLong(2, lastPosId);
            insertEventStm.bindLong(3, engineStatus);
            insertEventStm.bindLong(4, sailPlan);
            insertEventStm.executeInsert();
            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
        }
    }
    
    /**
     * Create a copy of the database on another file. The idea is to export 
     * this file on the MMC memory so that it can be transferred from
     * the device.
     */
    public void exportDbAsSQLite(ExportFile exportFile) throws IOException {

        File sourceFile = new File(getReadableDatabase().getPath());
 
        FileChannel source = null;
        FileChannel target = null;
        
        try {           
            source = new FileInputStream(sourceFile).getChannel();
            target = new FileOutputStream(exportFile.file()).getChannel();
            target.transferFrom(source, 0, source.size());      
        } finally {
            if (null != source) {
                source.close();
            }
            if (null != target) {
                target.close();
            }
        }
    }
    
    public void exportDbAsKML(ExportFile exportFile) throws IOException {
  
        SQLiteDatabase db = getReadableDatabase();
        String [] selectionArgs = {};
        Cursor c = db.rawQuery("SELECT position_id, longitude, latitude FROM position ORDER BY position_id", selectionArgs);
        
        if (0 >= c.getCount()) {
            return;
        }
        
        try {
            PrintWriter pw = new PrintWriter(exportFile.file(), "utf-8");

            // Start of XML.
            pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            pw.println("<kml xmlns=\"http://www.opengis.net/kml/2.2\">");
            pw.println("<Document>");
            pw.println("<Style id=\"red\">");
            pw.println("<LineStyle><color>ffffff00</color><width>4</width></LineStyle></Style>");
            pw.println("<Placemark>");
            pw.println("<styleUrl>#red</styleUrl>");
            pw.println("<LineString>");
            pw.println("<coordinates>");

            while (true == c.moveToNext()) {
                pw.println(String.format("%f,%f", c.getDouble(1), c.getDouble(2)));
            }

            pw.println("</coordinates>");
            pw.println("</LineString>");
            pw.println("</Placemark>");
            pw.println("</Document>");
            pw.println("</kml>");

            pw.close();
        } finally {
            c.close();
        }
    }

    private static final int dbVersion = 1;

    private ResourceBundle sqlBundle;
    
    private SQLiteStatement insertPosStm;
    private SQLiteStatement insertTripStm;
    private SQLiteStatement fetchLastPositionStm;
    private SQLiteStatement insertEventStm;
}
