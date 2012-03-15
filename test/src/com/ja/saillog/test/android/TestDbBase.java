package com.ja.saillog.test.android;

import java.io.File;

import junit.framework.Assert;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.test.AndroidTestCase;

public abstract class TestDbBase extends AndroidTestCase {

    protected void tearDown() throws Exception {
        SQLiteDatabase db = getWritableDatabase();
        String dbPath = db.getPath();
        db.close();

        new File(dbPath).delete();

        super.tearDown();
    }

    public void testDbSetUp() {
        checkTablesExist(getWritableDatabase(), expectedTables);
     }

    protected void checkTablesExist(SQLiteDatabase db, String[] requiredTables) {
        for (String table: requiredTables) {
            SQLiteStatement stm = db.compileStatement(String.format("select count(*) from sqlite_master " + 
                    "where type = 'table' and name = '%s'", table));
            int tablesFound = (int) stm.simpleQueryForLong();
            Assert.assertEquals(String.format("Table %s not found; ", table), 1, tablesFound);
        }
    }

    protected void checkColumnsNotNull(Cursor c) {
        for (int col = 0; col < c.getColumnCount(); ++col) {
            Assert.assertFalse(c.isNull(col));
        }
    }

    protected abstract SQLiteDatabase getWritableDatabase();
    
    protected String expectedTables[] = null;
}