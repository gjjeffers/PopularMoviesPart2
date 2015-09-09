package com.example.mainmachine.popularmoviespart2.data;

import android.content.UriMatcher;
import android.net.Uri;
import android.test.AndroidTestCase;

/**
 * Created by MainMachine on 9/8/2015.
 */
public class TestUriMatcher extends AndroidTestCase {
    public void testUriMatcher() throws Throwable{
        UriMatcher uriMatcher = FavoriteProvider.buildUriMatcher();

        final Uri TEST_FAVORITE_DIR = FavoriteContract.FavoriteEntry.CONTENT_URI;
        final Uri TEST_TRAILER_DIR = FavoriteContract.TrailerEntry.CONTENT_URI;

        assertEquals("Error: The Favorite URI was matched incorrectly.",
                uriMatcher.match(TEST_FAVORITE_DIR), FavoriteProvider.FAVORITE);
        assertEquals("Error: The Trailer URI was matched incorrectly.",
                uriMatcher.match(TEST_TRAILER_DIR), FavoriteProvider.TRAILER);

    }
}
