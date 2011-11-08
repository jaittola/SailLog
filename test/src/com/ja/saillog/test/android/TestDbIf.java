package com.ja.saillog.test.android;

import java.io.File;

import junit.framework.Assert;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.test.AndroidTestCase;

import com.ja.saillog.DbIf;

public class TestDbIf extends AndroidTestCase {
	
	protected void setUp() throws Exception {
		super.setUp();
		
		dbif = new DbIf(mContext);
	}
	
	protected void tearDown() throws Exception {
		SQLiteDatabase db = dbif.getWritableDatabase();
		String dbPath = db.getPath();
		db.close();
		
		new File(dbPath).delete();
		
		super.tearDown();
	}
	
	public void testDbSetUp() {
		SQLiteDatabase db = dbif.getWritableDatabase();
		
		String[] expectedTables = { "trip" };
 		checkTablesExist(db, expectedTables);
	}
	
	public void testTempTripInsert() {
		// TODO continue here.
	}
	
	private void checkTablesExist(SQLiteDatabase db, String[] requiredTables) {
		for (String table: requiredTables) {
			SQLiteStatement stm = db.compileStatement(String.format("select count (*) from sqlite_master where type = 'table' and name = '%s'", table));
			long count = stm.simpleQueryForLong();
			Assert.assertEquals(1, count);
		}
	}
	
	private DbIf dbif;
}
