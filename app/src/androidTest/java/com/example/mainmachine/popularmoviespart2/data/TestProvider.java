package com.example.mainmachine.popularmoviespart2.data;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import com.example.mainmachine.popularmoviespart2.data.FavoriteContract.FavoriteEntry;
import com.example.mainmachine.popularmoviespart2.data.FavoriteContract.TrailerEntry;

import java.util.Map;
import java.util.Set;

/**
 * Created by MainMachine on 9/8/2015.
 */
public class TestProvider extends AndroidTestCase {
    public static final String LOG_TAG = TestProvider.class.getSimpleName();

    public void deleteAllRecordsProvider() {
        mContext.getContentResolver().delete(FavoriteEntry.CONTENT_URI, null, null);
        mContext.getContentResolver().delete(TrailerEntry.CONTENT_URI, null, null);
        Cursor favCursor = mContext.getContentResolver().query(FavoriteEntry.CONTENT_URI,null,null,null,null);
        assertEquals("Error: Not all records deleted from Favorite table", 0, favCursor.getCount());
        favCursor.close();

        Cursor trailCursor = mContext.getContentResolver().query(TrailerEntry.CONTENT_URI,null,null,null,null);
        assertEquals("Error: Not all records delted from Trailer table",0,trailCursor.getCount());
        trailCursor.close();
    }

    public void deleteAllRecordsDB(){
        FavoriteDbHelper dbHelper = new FavoriteDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.delete(FavoriteEntry.TABLE_NAME, null, null);
        db.delete(TrailerEntry.TABLE_NAME, null, null);
    }

    public void deleteAllRecords(){deleteAllRecordsDB();}

    protected void setUp() throws Exception{
        super.setUp();
        deleteAllRecords();
    }

    public void testProviderRegistry() {
        PackageManager pm = mContext.getPackageManager();

        // We define the component name based on the package name from the context and the
        // WeatherProvider class.
        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                FavoriteProvider.class.getName());
        try {
            // Fetch the provider info using the component name from the PackageManager
            // This throws an exception if the provider isn't registered.
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            // Make sure that the registered authority matches the authority from the Contract.
            assertEquals("Error: FavoriteProvider registered with authority: " + providerInfo.authority +
                            " instead of authority: " + FavoriteContract.CONTENT_AUTHORITY,
                    providerInfo.authority, FavoriteContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            // I guess the provider isn't registered correctly.
            assertTrue("Error: FavoriteProvider not registered at " + mContext.getPackageName(),
                    false);
        }
    }

    public void testGetType() {
        final String testApiKey = "1a";
        // content://com.example.mainmachine.popularmoviespart2/favorite
        String type = mContext.getContentResolver().getType(FavoriteEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.android.sunshine.app/weather
        assertEquals("Error: the WeatherEntry CONTENT_URI should return WeatherEntry.CONTENT_TYPE",
                FavoriteEntry.CONTENT_TYPE, type);


        // content://com.example.mainmachine.popularmoviespart2/favorite/1a
        type = mContext.getContentResolver().getType(
                FavoriteEntry.buildFavorite(testApiKey));
        // vnd.android.cursor.dir/com.example.android.sunshine.app/weather
        assertEquals("Error: the FavoriteEntry CONTENT_URI with location should return FavoriteEntry.CONTENT_ITEM_TYPE",
                FavoriteEntry.CONTENT_ITEM_TYPE, type);

        // content://com.example.mainmachine.popularmoviespart2/trailer
        type = mContext.getContentResolver().getType(TrailerEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.android.sunshine.app/weather
        assertEquals("Error: the TrailerEntry CONTENT_URI should return TrailerEntry.CONTENT_TYPE",
                TrailerEntry.CONTENT_TYPE, type);

        // content://com.example.mainmachine.popularmoviespart2/trailer/1a
        type = mContext.getContentResolver().getType(TrailerEntry.buildTrailer(testApiKey));
        // vnd.android.cursor.dir/com.example.android.sunshine.app/weather
        assertEquals("Error: the FavoriteEntry CONTENT_URI with location should return FavoriteEntry.CONTENT_TYPE",
                TrailerEntry.CONTENT_TYPE, type);    }

    public void testAllFavoritesQuery() {
        // insert our test records into the database
        FavoriteDbHelper dbHelper = new FavoriteDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testFavValues = createTestSingleFavoriteValues();
        long favoriteRowId = insertFavoriteValues(mContext);

        db.close();

        // Test the basic content provider query
        Cursor favCursor = mContext.getContentResolver().query(
                FavoriteEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        validateCursor("testBasicFavoriteQuery", favCursor, testFavValues);
    }

    private ContentValues createTestSingleFavoriteValues(){
        ContentValues testValues = new ContentValues();
        testValues.put(FavoriteEntry.COLUMN_API_ID,"1A");
        testValues.put(FavoriteEntry.COLUMN_TITLE, "THIS MOVIE");
        testValues.put(FavoriteEntry.COLUMN_SYN, "THIS IS THAT MOVIE");
        testValues.put(FavoriteEntry.COLUMN_RATING,"3");
        testValues.put(FavoriteEntry.COLUMN_RELEASE_DATE, "2/2/2012");
        testValues.put(FavoriteEntry.COLUMN_POSTER, "POSTER URI GOES HERE");
        return testValues;
    }

    private ContentValues createFavoriteValues(){
        ContentValues favValues = new ContentValues();
        favValues.put(FavoriteEntry.COLUMN_API_ID,"1A");
        favValues.put(FavoriteEntry.COLUMN_TITLE, "THIS MOVIE");
        favValues.put(FavoriteEntry.COLUMN_SYN, "THIS IS THAT MOVIE");
        favValues.put(FavoriteEntry.COLUMN_RATING,"3");
        favValues.put(FavoriteEntry.COLUMN_RELEASE_DATE, "2/2/2012");
        favValues.put(FavoriteEntry.COLUMN_POSTER, "POSTER URI GOES HERE");
        return favValues;
    }

    private ContentValues createTrailerValues(){
        ContentValues trailerValues = new ContentValues();
        trailerValues.put(TrailerEntry.COLUMN_MOVIE_KEY, "1A");
        trailerValues.put(TrailerEntry.COLUMN_TRAILER_TITLE, "THIS MOVIE TRAILER #1");
        trailerValues.put(TrailerEntry.COLUMN_TRAILER_URI, "THIS MOVIE TRAILER #1 URI");
        return trailerValues;
    }

    private long insertFavoriteValues(Context context) {
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

    private long insertTrailerValues(Context context) {
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

    private void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    private void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
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
