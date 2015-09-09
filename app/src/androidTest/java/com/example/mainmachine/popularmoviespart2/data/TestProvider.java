package com.example.mainmachine.popularmoviespart2.data;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import com.example.mainmachine.popularmoviespart2.data.FavoriteContract.FavoriteEntry;
import com.example.mainmachine.popularmoviespart2.data.FavoriteContract.TrailerEntry;

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

}
