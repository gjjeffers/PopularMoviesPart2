package com.example.mainmachine.popularmoviespart2.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.mainmachine.popularmoviespart2.data.FavoriteContract.FavoriteEntry;
import com.example.mainmachine.popularmoviespart2.data.FavoriteContract.TrailerEntry;

/**
 * Created by MainMachine on 9/7/2015.
 */
public class FavoriteDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    static final String DATABASE_NAME = "favorite.db";

    public FavoriteDbHelper(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase){
        final String SQL_CREATE_FAVORITE_TABLE = "CREATE TABLE " + FavoriteEntry.TABLE_NAME + " (" +
               FavoriteEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
               FavoriteEntry.COLUMN_API_ID + " TEXT NOT NULL, " +
               FavoriteEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
               FavoriteEntry.COLUMN_SYN + " TEXT NOT NULL, " +
               FavoriteEntry.COLUMN_RATING + " TEXT NOT NULL, " +
               FavoriteEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL, " +
               FavoriteEntry.COLUMN_POSTER + " TEXT NOT NULL) ";
        sqLiteDatabase.execSQL(SQL_CREATE_FAVORITE_TABLE);

        final String SQL_CREATE_TRAILER_TABLE = "CREATE TABLE " + TrailerEntry.TABLE_NAME + " (" +
                TrailerEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TrailerEntry.COLUMN_MOVIE_KEY + " TEXT NOT NULL, " +
                TrailerEntry.COLUMN_TRAILER_TITLE + " TEXT NOT NULL, " +
                TrailerEntry.COLUMN_TRAILER_URI + " TEXT NOT NULL, " +
                " FOREIGN KEY (" + TrailerEntry.COLUMN_MOVIE_KEY + ") REFERENCES " +
                FavoriteEntry.TABLE_NAME + " (" + FavoriteEntry.COLUMN_API_ID + "), " +
                " UNIQUE (" + TrailerEntry.COLUMN_TRAILER_URI + ") ON CONFLICT REPLACE);";
        sqLiteDatabase.execSQL(SQL_CREATE_TRAILER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + FavoriteEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TrailerEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
