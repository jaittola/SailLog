package com.ja.saillog;

import java.util.ResourceBundle;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbIf extends SQLiteOpenHelper {

    public DbIf(Context context) {
        super(context, dbName, null, dbVersion);

        sqlBundle = ResourceBundle.getBundle("com.ja.saillog.sql");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    	String[] statements = {
    			"set_vacuum",
    			"drop_trip",
    			"create_trip"
    	};
 
       for (String s: statements) {
        	db.execSQL(sqlBundle.getString(s));
        }
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    private static final String dbName = "SLDB.db";
    private static final int dbVersion = 1;

    private ResourceBundle sqlBundle;
}
