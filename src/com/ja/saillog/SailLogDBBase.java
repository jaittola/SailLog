package com.ja.saillog;

import java.util.ResourceBundle;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public abstract class SailLogDBBase extends SQLiteOpenHelper {

    public SailLogDBBase(Context context, String databaseName) {
        super(context, databaseName, null, dbVersion);
        
        sqlBundle = ResourceBundle.getBundle("com.ja.saillog.sql");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (String s: createDbStatements) {
        	db.execSQL(sqlBundle.getString(s));
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    protected static final int dbVersion = 1;
    protected ResourceBundle sqlBundle;

    // The strings in this array refer to SQL clauses
    // in the sql.properties file.
    protected String[] createDbStatements = null;
}