package com.example.mainmachine.popularmoviespart2.data;


import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class FavoriteContract {
    public static final String CONTENT_AUTHORITY = "com.example.mainmachine.popularmoviespart2";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_FAVORITE = "favorite";
    public static final String PATH_TRAILER = "trailer";

    public static final class FavoriteEntry implements BaseColumns{
        public static final String TABLE_NAME = "favorite";

        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_SYN = "synopsis";
        public static final String COLUMN_RATING = "rating";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_POSTER = "poster";
        public static final String COLUMN_API_ID = "api_id";
        public static final String COLUMN_REVIEW = "review";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAVORITE).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAVORITE;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAVORITE;

        public static Uri buildFavoriteUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildFavorite(String api_key){
            return CONTENT_URI.buildUpon().appendPath(api_key).build();
        }

        public static String getColumnApiId(Uri uri){
            return uri.getLastPathSegment();
        }
    }

}
