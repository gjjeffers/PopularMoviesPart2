package com.example.mainmachine.popularmoviespart2.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.Map;
import java.util.Set;

/**
 * Created by MainMachine on 9/10/2015.
 */
public class Util extends AndroidTestCase {

    static ContentValues createTestSingleFavoriteValues(){
        ContentValues testValues = new ContentValues();
        testValues.put(FavoriteContract.FavoriteEntry.COLUMN_API_ID,"1A");
        testValues.put(FavoriteContract.FavoriteEntry.COLUMN_TITLE, "THIS MOVIE");
        testValues.put(FavoriteContract.FavoriteEntry.COLUMN_SYN, "THIS IS THAT MOVIE");
        testValues.put(FavoriteContract.FavoriteEntry.COLUMN_RATING,"3");
        testValues.put(FavoriteContract.FavoriteEntry.COLUMN_RELEASE_DATE, "2/2/2012");
        testValues.put(FavoriteContract.FavoriteEntry.COLUMN_POSTER, "POSTER URI GOES HERE");
        return testValues;
    }

    static ContentValues createTestFullFavoriteValues(){
        ContentValues testValues = new ContentValues();
        testValues.put(FavoriteContract.FavoriteEntry.COLUMN_API_ID,"1A");
        testValues.put(FavoriteContract.FavoriteEntry.COLUMN_TITLE, "THIS MOVIE");
        testValues.put(FavoriteContract.FavoriteEntry.COLUMN_SYN, "THIS IS THAT MOVIE");
        testValues.put(FavoriteContract.FavoriteEntry.COLUMN_RATING,"3");
        testValues.put(FavoriteContract.FavoriteEntry.COLUMN_RELEASE_DATE, "2/2/2012");
        testValues.put(FavoriteContract.FavoriteEntry.COLUMN_POSTER, "POSTER URI GOES HERE");

        testValues.put(FavoriteContract.TrailerEntry.COLUMN_MOVIE_KEY, "1A");
        testValues.put(FavoriteContract.TrailerEntry.COLUMN_TRAILER_TITLE, "THIS MOVIE TRAILER #1");
        testValues.put(FavoriteContract.TrailerEntry.COLUMN_TRAILER_URI, "THIS MOVIE TRAILER #1 URI");
        return testValues;
    }

    static ContentValues createFavoriteValues(){
        ContentValues favValues = new ContentValues();
        favValues.put(FavoriteContract.FavoriteEntry.COLUMN_API_ID,"1A");
        favValues.put(FavoriteContract.FavoriteEntry.COLUMN_TITLE, "THIS MOVIE");
        favValues.put(FavoriteContract.FavoriteEntry.COLUMN_SYN, "THIS IS THAT MOVIE");
        favValues.put(FavoriteContract.FavoriteEntry.COLUMN_RATING,"3");
        favValues.put(FavoriteContract.FavoriteEntry.COLUMN_RELEASE_DATE, "2/2/2012");
        favValues.put(FavoriteContract.FavoriteEntry.COLUMN_POSTER, "POSTER URI GOES HERE");
        return favValues;
    }

    static ContentValues createTrailerValues(){
        ContentValues trailerValues = new ContentValues();
        trailerValues.put(FavoriteContract.TrailerEntry.COLUMN_MOVIE_KEY, "1A");
        trailerValues.put(FavoriteContract.TrailerEntry.COLUMN_TRAILER_TITLE, "THIS MOVIE TRAILER #1");
        trailerValues.put(FavoriteContract.TrailerEntry.COLUMN_TRAILER_URI, "THIS MOVIE TRAILER #1 URI");
        return trailerValues;
    }

    static long insertFavoriteValues(Context context) {
        // insert our test records into the database
        FavoriteDbHelper dbHelper = new FavoriteDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues testValues = createFavoriteValues();

        long favoriteRowId;
        favoriteRowId = db.insert("favorite", null, testValues);

        // Verify we got a row back.
        assertTrue("Error: Failure to insert Favorite Values", favoriteRowId != -1);

        return favoriteRowId;
    }

    static long insertTrailerValues(Context context) {
        // insert our test records into the database
        FavoriteDbHelper dbHelper = new FavoriteDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues testValues = createTrailerValues();

        long trailerRowId;
        trailerRowId = db.insert("trailer", null, testValues);

        // Verify we got a row back.
        assertTrue("Error: Failure to insert Trailer Values", trailerRowId != -1);

        return trailerRowId;
    }

    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }
}
