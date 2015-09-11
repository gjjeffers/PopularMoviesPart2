package com.example.mainmachine.popularmoviespart2.data;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.test.AndroidTestCase;

import com.example.mainmachine.popularmoviespart2.data.FavoriteContract.FavoriteEntry;
import com.example.mainmachine.popularmoviespart2.data.FavoriteContract.TrailerEntry;

public class TestProvider extends AndroidTestCase {
    public void deleteAllRecordsProvider() {
        mContext.getContentResolver().delete(FavoriteEntry.CONTENT_URI, null, null);
        mContext.getContentResolver().delete(TrailerEntry.CONTENT_URI, null, null);
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

    public void testQueryAllFavorites() {
        ContentValues testFavValues = Util.createTestSingleFavoriteValues();
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

    public void testQueryOneTrailer() {
        ContentValues testTrailerValues = Util.createTrailerValues();
        Util.insertTrailerValues(mContext);


        // Test the basic content provider query
        Cursor trailerCursor = mContext.getContentResolver().query(
                TrailerEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        Util.validateCursor("testOneTrailerQuery", trailerCursor, testTrailerValues);
    }

    public void testQueryOneFavorites() {
        ContentValues testTrailerValues = Util.createTestFullFavoriteValues();
        Util.insertFavoriteValues(mContext);
        Util.insertTrailerValues(mContext);



        // Test the basic content provider query
        Cursor trailerCursor = mContext.getContentResolver().query(
                FavoriteEntry.buildFavorite(testTrailerValues.getAsString(FavoriteEntry.COLUMN_API_ID)),
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        Util.validateCursor("testOneFavoritesQuery", trailerCursor, testTrailerValues);
    }

    public void testDeleteAllFavorites(){
        Util.insertFavoriteValues(mContext);
        Util.insertTrailerValues(mContext);

        mContext.getContentResolver().delete(FavoriteEntry.CONTENT_URI, null, null);
        Cursor delFavCheck = mContext.getContentResolver().query(FavoriteEntry.CONTENT_URI, null, null, null, null);
        assertTrue("Not all Favorite records were deleted!", delFavCheck.getCount() == 0);
        Cursor delTrailerCheck = mContext.getContentResolver().query(TrailerEntry.CONTENT_URI,null,null,null,null);
        assertTrue("Not all Trailer record were deleted!", delTrailerCheck.getCount() == 0);
        delFavCheck.close();
        delTrailerCheck.close();
    }

    public void testDeleteOneFavorite(){
        Util.insertFavoriteValues(mContext);
        Util.insertTrailerValues(mContext);
        ContentValues testTrailerValues = Util.createTestFullFavoriteValues();

        mContext.getContentResolver().delete(FavoriteEntry.CONTENT_URI, null, new String[]{testTrailerValues.getAsString(FavoriteEntry.COLUMN_API_ID)});
        Cursor delFavCheck = mContext.getContentResolver().query(FavoriteEntry.CONTENT_URI, null, null, null, null);
        assertTrue("Not all Favorite records were deleted!", delFavCheck.getCount() == 0);
        Cursor delTrailerCheck = mContext.getContentResolver().query(TrailerEntry.CONTENT_URI,null,null,null,null);
        assertTrue("Not all Trailer record were deleted!", delTrailerCheck.getCount() == 0);
        delFavCheck.close();
        delTrailerCheck.close();
    }

    public void testInsert(){


        ContentValues testFavValues = Util.createFavoriteValues();
        ContentValues testTrailerValues = Util.createTrailerValues();
        mContext.getContentResolver().insert(FavoriteEntry.CONTENT_URI, testFavValues);
        mContext.getContentResolver().insert(TrailerEntry.CONTENT_URI, testTrailerValues);


        Cursor fullCursor = mContext.getContentResolver().query(
                FavoriteEntry.buildFavorite(testFavValues.getAsString(FavoriteEntry.COLUMN_API_ID)),
                null,
                null,
                null,
                null
        );

        Util.validateCursor("Record not properly inserted", fullCursor, Util.createTestFullFavoriteValues());
    }

    public void testUpdateFavorite(){
        //insert initial Favorite record
        ContentValues testFavValues = Util.createFavoriteValues();
        mContext.getContentResolver().insert(FavoriteEntry.CONTENT_URI, testFavValues);

        //insert initial Trailer record
        ContentValues testTrailerValues = Util.createTrailerValues();
        mContext.getContentResolver().insert(TrailerEntry.CONTENT_URI, testTrailerValues);

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

        //update Trailer record
        ContentValues updateTrailerValues = new ContentValues();
        updateTrailerValues.put(FavoriteContract.TrailerEntry.COLUMN_MOVIE_KEY, "1A");
        updateTrailerValues.put(FavoriteContract.TrailerEntry.COLUMN_TRAILER_TITLE, "THAT MOVIE TRAILER #1");
        updateTrailerValues.put(FavoriteContract.TrailerEntry.COLUMN_TRAILER_URI, "THAT MOVIE TRAILER #1 URI");
        mContext.getContentResolver().update(TrailerEntry.CONTENT_URI,
                updateTrailerValues,
                TrailerEntry.COLUMN_MOVIE_KEY,
                new String[]{updateTrailerValues.getAsString(TrailerEntry.COLUMN_MOVIE_KEY)});

        Cursor fullCursor = mContext.getContentResolver().query(
                FavoriteEntry.buildFavorite(testFavValues.getAsString(FavoriteEntry.COLUMN_API_ID)),
                null,
                null,
                null,
                null
        );
        ContentValues resultValues = new ContentValues();
        resultValues.putAll(updateFavValues);
        resultValues.putAll(updateTrailerValues);


        Util.validateCursor("Record not properly updated", fullCursor, resultValues);
    }




}
