package com.ja.saillog.database;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Pair;

public abstract class SailLogDBBase extends SQLiteOpenHelper {
    
    protected class DBUpgradeStatement {
        public DBUpgradeStatement(String sql, int toVersion) {
            this.sql = sql;
            this.toVersion = toVersion;
        }
        
        public String sql;
        public int toVersion;
    }

    public SailLogDBBase(Context context, String databaseName) {
        super(context, databaseName, null, dbVersion);

        sqlBundle = ResourceBundle.getBundle("com.ja.saillog.database.sql");
        cachedStatements = new HashMap<Pair<String, SQLiteDatabase>,
            SQLiteStatement>();
    }

    @Override
    protected void finalize() throws Throwable {
        for (Map.Entry<Pair<String, SQLiteDatabase>, SQLiteStatement> entry:
                 cachedStatements.entrySet()) {
            entry.getValue().close();
        }
        cachedStatements.clear();

        super.finalize();
    }

    /**
     * Create the database.
     *
     * Run through all statements requested on creation.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        for (String s: createDbStatements) {
            db.execSQL(sqlBundle.getString(s));
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db,
                          int oldVersion,
                          int newVersion) {
        if (null == upgradeDbStatements) {
            return;
        }
        
        for (int v = oldVersion + 1; v <= newVersion; ++v) {
            for (DBUpgradeStatement stm: upgradeDbStatements) {
                if (stm.toVersion == v) {
                    db.execSQL(sqlBundle.getString(stm.sql));
                }
            }
        }
    }

    protected SQLiteStatement getStatement(SQLiteDatabase db,
                                           String statement) {
        Pair<String, SQLiteDatabase> key =
            new Pair<String, SQLiteDatabase>(statement, db);
        SQLiteStatement stm = cachedStatements.get(key);
        if (null == stm) {
            stm = db.compileStatement(statement);
            cachedStatements.put(key, stm);
        }

        return stm;
    }

    protected static int dbVersion = 1;
    protected ResourceBundle sqlBundle;

    // The strings in this array refer to SQL clauses
    // in the sql.properties file.
    protected String[] createDbStatements = null;
 
    // Similar to createDbStatements, but contains
    // db version numbers.
    protected DBUpgradeStatement[] upgradeDbStatements = null;

    /**
     * Cached statements for this database.
     */
    protected HashMap<Pair<String, SQLiteDatabase>,
        SQLiteStatement> cachedStatements;
}
