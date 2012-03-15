package com.ja.saillog;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

public class TripDB extends SailLogDBBase {

    public TripDB(Context context, String databaseName) {
        super(context, databaseName);
        
        createDbStatements = new String[] {
                "set_vacuum",
                "drop_trip",
                "create_trip",
                "drop_leg",
                "create_leg",
        };
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
     
     private SQLiteStatement insertTripStm;
     
    
}
