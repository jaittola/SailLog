package com.ja.saillog;

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
    
    /**
     * Create a copy of the database on another file. The idea is to export 
     * this file on the MMC memory so tha it can be transferred from
     * the device.
     */
    public void exportDb(String targeFileName) {
    	
    }

    private static final int dbVersion = 1;

    private ResourceBundle sqlBundle;
    
    private SQLiteStatement insertPosStm;
    private SQLiteStatement insertTripStm;
}
