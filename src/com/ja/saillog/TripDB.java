package com.ja.saillog;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import java.util.Random;

public class TripDB extends SailLogDBBase {

    public TripDB(Context context, String databaseName) {
        super(context, databaseName);
        
        createDbStatements = new String[] {
                "set_vacuum",
                "drop_trip",
                "create_trip",
       };
        
        // This is crap.
        rand = new Random();
    }

    @Override
    protected void finalize() throws Throwable {
        if (null != insertTripStm) {
            insertTripStm.close();
        }
    
        super.finalize();
    }
    
    public void insertTrip(String tripName) {
         SQLiteDatabase db = getWritableDatabase();
         
         // This needs fixing. Should use the row sequential row number.
         // But it's now easier this way. TODO.
         String tripDbFile = "sldb_trip_" + rand.nextInt();
         
         if (null == insertTripStm) {
             insertTripStm = db.compileStatement("INSERT INTO trip (trip_name, trip_db_filename) " + 
                     "VALUES (?, ?)");
         }
         
         try {
             db.beginTransaction();
             insertTripStm.bindString(1, tripName);
             insertTripStm.bindString(2, tripDbFile);
             insertTripStm.executeInsert();
             db.setTransactionSuccessful();
         } finally {
             db.endTransaction();
         }
     }
     
    public class TripDbInfo extends Object {
    	public int tripId;
    	public String tripName;	
    	public String dbFileName;
    }
    
     public TripDbInfo fetchTripId(String tripName) {
    	 TripDbInfo tdi = null;
    	 
         SQLiteDatabase db = getReadableDatabase();
         String [] selectionArgs = { tripName };
         Cursor c = db.rawQuery("SELECT trip_id, trip_db_filename FROM trip WHERE " +
         		"trip_id = (SELECT MAX(trip_id) FROM trip WHERE trip_name = ?)", selectionArgs);
         if (true == c.moveToNext()) {
             tdi = new TripDbInfo();
             
             if (!c.isNull(0)) {
                 tdi.tripId = c.getInt(0);
             }
             if (!c.isNull(1)) {
                 tdi.dbFileName = c.getString(1);
             }
             tdi.tripName = tripName;
         }
         
         c.close();
             
         return tdi;
     }
     
     private SQLiteStatement insertTripStm;
     Random rand;
}
