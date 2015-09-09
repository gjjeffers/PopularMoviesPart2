package com.example.mainmachine.popularmoviespart2.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.HashSet;

/**
 * Created by MainMachine on 9/8/2015.
 * Adapted from Sunshine app. All the appropriate test cases were already there, and a good software
 * engineer reuses as much code as possible! :)
 */
public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    // Start clean
    void deleteTheDatabase() {
        mContext.deleteDatabase(FavoriteDbHelper.DATABASE_NAME);
    }

    public void setUp() {
        deleteTheDatabase();
    }

    public void testCreateDb() throws Throwable {
        // build a HashSet of all of the table names we wish to look for
        // Note that there will be another table in the DB that stores the
        // Android metadata (db version information)
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(FavoriteContract.FavoriteEntry.TABLE_NAME);
        tableNameHashSet.add(FavoriteContract.TrailerEntry.TABLE_NAME);

        mContext.deleteDatabase(FavoriteDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new FavoriteDbHelper(this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly",c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while( c.moveToNext() );

        // if this fails, it means that your database doesn't contain both the location entry
        // and weather entry tables
        assertTrue("Error: Your database was created without both the favorite entry and trailer entry tables",
                tableNameHashSet.isEmpty());

        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + FavoriteContract.FavoriteEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> favoriteColumnHashSet = new HashSet<String>();
        favoriteColumnHashSet.add(FavoriteContract.FavoriteEntry._ID);
        favoriteColumnHashSet.add(FavoriteContract.FavoriteEntry.COLUMN_API_ID);
        favoriteColumnHashSet.add(FavoriteContract.FavoriteEntry.COLUMN_TITLE);
        favoriteColumnHashSet.add(FavoriteContract.FavoriteEntry.COLUMN_SYN);
        favoriteColumnHashSet.add(FavoriteContract.FavoriteEntry.COLUMN_RATING);
        favoriteColumnHashSet.add(FavoriteContract.FavoriteEntry.COLUMN_RELEASE_DATE);
        favoriteColumnHashSet.add(FavoriteContract.FavoriteEntry.COLUMN_POSTER);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            favoriteColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required location
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required favorite entry columns",
                favoriteColumnHashSet.isEmpty());
        db.close();
    }

    public void testFavoriteTable() throws Throwable {
        // First step: Get reference to writable database
        FavoriteDbHelper helper = new FavoriteDbHelper(this.getContext());
        SQLiteDatabase db = helper.getWritableDatabase();

        // Create ContentValues of what you want to insert
        // (you can use the createNorthPoleLocationValues if you wish)
        ContentValues testValues = new ContentValues();
        testValues.put(FavoriteContract.FavoriteEntry.COLUMN_API_ID, "1A");
        testValues.put(FavoriteContract.FavoriteEntry.COLUMN_TITLE, "THIS MOVIE");
        testValues.put(FavoriteContract.FavoriteEntry.COLUMN_SYN, "IT'S THIS MOVIE");
        testValues.put(FavoriteContract.FavoriteEntry.COLUMN_RATING, "2");
        testValues.put(FavoriteContract.FavoriteEntry.COLUMN_RELEASE_DATE, "2/2/2012");
        testValues.put(FavoriteContract.FavoriteEntry.COLUMN_POSTER, "SOME URI");


        // Insert ContentValues into database and get a row ID back
        long favoriteRowId;
        favoriteRowId = db.insert(FavoriteContract.FavoriteEntry.TABLE_NAME, null, testValues);

        Cursor result = db.query(FavoriteContract.FavoriteEntry.TABLE_NAME,null,null,null,null,null,null);

        result.moveToFirst();

        int index = result.getColumnIndex(FavoriteContract.FavoriteEntry._ID);
        int id = result.getInt(index);

        // Verify we got a row back.
        assertTrue("Returned value from insert", id != -1);
        assertTrue("IDs agree", id == favoriteRowId);

        result.close();
        db.close();
    }

    public void testTrailerTable() throws Throwable {
        // First step: Get reference to writable database
        FavoriteDbHelper helper = new FavoriteDbHelper(this.getContext());
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues setupValues = new ContentValues();
        setupValues.put(FavoriteContract.FavoriteEntry.COLUMN_API_ID, "1A");
        setupValues.put(FavoriteContract.FavoriteEntry.COLUMN_TITLE, "THIS MOVIE");
        setupValues.put(FavoriteContract.FavoriteEntry.COLUMN_SYN, "IT'S THIS MOVIE");
        setupValues.put(FavoriteContract.FavoriteEntry.COLUMN_RATING, "2");
        setupValues.put(FavoriteContract.FavoriteEntry.COLUMN_RELEASE_DATE, "2/2/2012");
        setupValues.put(FavoriteContract.FavoriteEntry.COLUMN_POSTER, "SOME URI");


        // Insert ContentValues into database and get a row ID back
        long favoriteRowId;
        favoriteRowId = db.insert(FavoriteContract.FavoriteEntry.TABLE_NAME, null, setupValues);

        // Create ContentValues of what you want to insert
        // (you can use the createNorthPoleLocationValues if you wish)
        ContentValues testValues = new ContentValues();
        testValues.put(FavoriteContract.TrailerEntry.COLUMN_MOVIE_KEY, setupValues.get(FavoriteContract.FavoriteEntry.COLUMN_API_ID).toString());
        testValues.put(FavoriteContract.TrailerEntry.COLUMN_TRAILER_TITLE, "THIS TRAILER #1");
        testValues.put(FavoriteContract.TrailerEntry.COLUMN_TRAILER_URI, "TRAILER #1 URI");

        long trailerRowId;
        trailerRowId = db.insert(FavoriteContract.TrailerEntry.TABLE_NAME, null, testValues);

        Cursor tResult = db.query(FavoriteContract.TrailerEntry.TABLE_NAME,null,null,null,null,null,null);

        tResult.moveToFirst();

        int index = tResult.getColumnIndex(FavoriteContract.TrailerEntry._ID);
        int id = tResult.getInt(index);

        // Verify we got a row back.
        assertTrue("Returned value from insert", id != -1);
        assertTrue("IDs agree", id == trailerRowId);

        tResult.close();
        db.close();
    }
}
