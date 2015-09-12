package com.example.mainmachine.popularmoviespart2.data;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.test.AndroidTestCase;

import com.example.mainmachine.popularmoviespart2.data.FavoriteContract.FavoriteEntry;

public class TestProvider extends AndroidTestCase {
    public void deleteAllRecordsProvider() {
        mContext.getContentResolver().delete(FavoriteEntry.CONTENT_URI, null, null);
        Cursor favCursor = mContext.getContentResolver().query(FavoriteEntry.CONTENT_URI, null, null, null, null);
        assertEquals("Error: Not all records deleted from database", 0, favCursor.getCount());
        favCursor.close();
    }

    public void deleteAllRecords(){deleteAllRecordsProvider();}

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
    }

    public void testQueryAllFavorites() {
        ContentValues testFavValues = Util.createFavoriteValues();
        Util.insertFavoriteValues(mContext);


        // Test the basic content provider query
        Cursor favCursor = mContext.getContentResolver().query(
                FavoriteEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        Util.validateCursor("testBasicFavoriteQuery", favCursor, testFavValues);
    }

    public void testQueryOneFavorites() {
        ContentValues testFavoriteValues = Util.createFavoriteValues();
        Util.insertFavoriteValues(mContext);

        // Test the basic content provider query
        Cursor trailerCursor = mContext.getContentResolver().query(
                FavoriteEntry.buildFavorite(testFavoriteValues.getAsString(FavoriteEntry.COLUMN_API_ID)),
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        Util.validateCursor("testOneFavoritesQuery", trailerCursor, testFavoriteValues);
    }

    public void testDeleteAllFavorites(){
        Util.insertFavoriteValues(mContext);

        mContext.getContentResolver().delete(FavoriteEntry.CONTENT_URI, null, null);
        Cursor delFavCheck = mContext.getContentResolver().query(FavoriteEntry.CONTENT_URI, null, null, null, null);
        assertTrue("Not all Favorite records were deleted!", delFavCheck.getCount() == 0);
        delFavCheck.close();
    }

    public void testDeleteOneFavorite(){
        Util.insertFavoriteValues(mContext);
        ContentValues testTrailerValues = Util.createFavoriteValues();

        mContext.getContentResolver().delete(FavoriteEntry.CONTENT_URI, null, new String[]{testTrailerValues.getAsString(FavoriteEntry.COLUMN_API_ID)});
        Cursor delFavCheck = mContext.getContentResolver().query(FavoriteEntry.CONTENT_URI, null, null, null, null);
        assertTrue("Not all Favorite records were deleted!", delFavCheck.getCount() == 0);
        delFavCheck.close();
    }

    public void testInsert(){
        ContentValues testFavValues = Util.createFavoriteValues();
        mContext.getContentResolver().insert(FavoriteEntry.CONTENT_URI, testFavValues);


        Cursor fullCursor = mContext.getContentResolver().query(
                FavoriteEntry.buildFavorite(testFavValues.getAsString(FavoriteEntry.COLUMN_API_ID)),
                null,
                null,
                null,
                null
        );

        Util.validateCursor("Record not properly inserted", fullCursor, testFavValues);
    }

    public void testUpdateFavorite(){
        //insert initial Favorite record
        ContentValues testFavValues = Util.createFavoriteValues();
        mContext.getContentResolver().insert(FavoriteEntry.CONTENT_URI, testFavValues);

        //update Favorite record
        ContentValues updateFavValues = new ContentValues();
        updateFavValues.put(FavoriteContract.FavoriteEntry.COLUMN_API_ID, "1A");
        updateFavValues.put(FavoriteContract.FavoriteEntry.COLUMN_TITLE, "THAT MOVIE");
        updateFavValues.put(FavoriteContract.FavoriteEntry.COLUMN_SYN, "THAT IS THIS MOVIE");
        updateFavValues.put(FavoriteContract.FavoriteEntry.COLUMN_RATING, "5");
        updateFavValues.put(FavoriteContract.FavoriteEntry.COLUMN_RELEASE_DATE, "12/12/2012");
        updateFavValues.put(FavoriteContract.FavoriteEntry.COLUMN_POSTER, "UPDATED POSTER URI GOES HERE");

        mContext.getContentResolver().update(FavoriteEntry.CONTENT_URI,
                updateFavValues,
                FavoriteEntry.COLUMN_API_ID,
                new String[]{updateFavValues.getAsString(FavoriteEntry.COLUMN_API_ID)});

        Cursor fullCursor = mContext.getContentResolver().query(
                FavoriteEntry.buildFavorite(testFavValues.getAsString(FavoriteEntry.COLUMN_API_ID)),
                null,
                null,
                null,
                null
        );
        ContentValues resultValues = new ContentValues();
        resultValues.putAll(updateFavValues);

        Util.validateCursor("Record not properly updated", fullCursor, resultValues);
    }




}
