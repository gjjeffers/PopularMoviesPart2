package com.example.mainmachine.popularmoviespart2.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import com.example.mainmachine.popularmoviespart2.data.FavoriteContract.FavoriteEntry;
import com.example.mainmachine.popularmoviespart2.data.FavoriteContract.TrailerEntry;

/**
 * Created by MainMachine on 9/7/2015.
 */
public class FavoriteProvider extends ContentProvider {

    private static final UriMatcher uriMatcher = buildUriMatcher();
    private FavoriteDbHelper dbHelper;
    private static final SQLiteQueryBuilder queryBuilder;

    static {
        queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(
                FavoriteEntry.TABLE_NAME + " INNER JOIN " + TrailerEntry.TABLE_NAME + " ON " +
                        FavoriteEntry.TABLE_NAME + "." + FavoriteEntry.COLUMN_API_ID + " = " +
                        TrailerEntry.TABLE_NAME + "." + TrailerEntry.COLUMN_MOVIE_KEY
        );
    }

    static final String favoriteDetailClause = FavoriteEntry.TABLE_NAME + "." + FavoriteEntry.COLUMN_API_ID + " = ? ";


    static final int FAVORITE = 100;
    static final int FAVORITE_WITH_KEY = 101;
    static final int TRAILER = 200;
    static final int TRAILER_WITH_KEY = 201;

    static UriMatcher buildUriMatcher(){
        UriMatcher returnMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = FavoriteContract.CONTENT_AUTHORITY;
        returnMatcher.addURI(authority, FavoriteContract.PATH_FAVORITE, FAVORITE);
        returnMatcher.addURI(authority, FavoriteContract.PATH_FAVORITE+"/*", FAVORITE_WITH_KEY);
        returnMatcher.addURI(authority, FavoriteContract.PATH_TRAILER, TRAILER);
        returnMatcher.addURI(authority, FavoriteContract.PATH_TRAILER+"/*", TRAILER_WITH_KEY);

        return returnMatcher;
    }

    private Cursor getFavorites(String sortOrder){
        //returns title and post of all favorites
        SQLiteDatabase _db = dbHelper.getReadableDatabase();
        String[] columns  = {FavoriteEntry.COLUMN_API_ID, FavoriteEntry.COLUMN_TITLE, FavoriteEntry.COLUMN_POSTER};
        return _db.query(FavoriteEntry.TABLE_NAME,columns,null, null,null,null,sortOrder);
    }

    private Cursor getFavoriteDetail(Uri uri, String sortOrder){
        //returns all details for a movie given apiKey
        String movieApiKey = FavoriteContract.FavoriteEntry.getColumnApiId(uri);
        return queryBuilder.query(dbHelper.getReadableDatabase(), null, favoriteDetailClause, new String[]{movieApiKey}, null, null, sortOrder);
    }

    @Override
    public boolean onCreate() {
        dbHelper = new FavoriteDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri){
        final int match = uriMatcher.match(uri);

        switch (match){
            case FAVORITE:
                return FavoriteEntry.CONTENT_TYPE;
            case FAVORITE_WITH_KEY:
                return FavoriteEntry.CONTENT_ITEM_TYPE;
            case TRAILER:
                return TrailerEntry.CONTENT_TYPE;
            case TRAILER_WITH_KEY:
                return TrailerEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder){
        Cursor returnCursor;
        switch(uriMatcher.match(uri)){
            case FAVORITE: {
                returnCursor = getFavorites(sortOrder);
                break;
            }
            case TRAILER:{
                returnCursor = getFavoriteDetail(uri,sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }

        returnCursor.setNotificationUri(getContext().getContentResolver(),uri);
        return returnCursor;
    }

    @Override
    public Uri insert(Uri uri,ContentValues values){
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        Uri returnUri;

        switch (match){
            case FAVORITE:{
                long _id = db.insert(FavoriteEntry.TABLE_NAME,null, values);
                if(_id > 0)
                    returnUri = FavoriteEntry.buildFavoriteUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into" + uri);
                break;
            }
            case TRAILER:{
                long _id = db.insert(TrailerEntry.TABLE_NAME,null, values);
                if(_id > 0)
                    returnUri = TrailerEntry.buildTrailerUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into" + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);

        }

        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    public int delete(Uri uri, String selection, String[] selectionArgs){
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        int returnCount = -1;
        String whereClause;

        if(match > 0){
            if(selection != null){
                whereClause = FavoriteEntry._ID + " = " + selection;
                Cursor _apiCursor = db.query(FavoriteEntry.TABLE_NAME,new String[]{FavoriteEntry.COLUMN_API_ID},whereClause,null,null,null,null);
                int _apiKey = _apiCursor.getInt(0);
                returnCount = db.delete(FavoriteEntry.TABLE_NAME,whereClause,null);
                whereClause = TrailerEntry.COLUMN_MOVIE_KEY + " = " + _apiKey;
                db.delete(TrailerEntry.TABLE_NAME,whereClause,null);
            }
        }
        else{
            throw new UnsupportedOperationException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri,null);
        return returnCount;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        int returnCount;
        String whereClause = null;

        switch (match) {
            case FAVORITE: {
                if(selection != null){
                    whereClause = selection;
                }
                returnCount = db.update(FavoriteEntry.TABLE_NAME, values,whereClause, selectionArgs);
                break;
            }
            case TRAILER:{
                if(selection != null){
                    whereClause = selection;
                }
                returnCount = db.update(TrailerEntry.TABLE_NAME, values, whereClause, selectionArgs);
                break;

            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnCount;
    }
}
