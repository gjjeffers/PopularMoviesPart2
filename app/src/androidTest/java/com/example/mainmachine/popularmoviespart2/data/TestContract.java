package com.example.mainmachine.popularmoviespart2.data;

import android.net.Uri;
import android.test.AndroidTestCase;

/**
 * Created by MainMachine on 9/8/2015.
 */
public class TestContract extends AndroidTestCase {
    private static final String TEST_URI_EXT = "1a";

    public void testFavoriteUri() throws Throwable{
        Uri favUri = FavoriteContract.FavoriteEntry.buildFavorite(TEST_URI_EXT);
        assertNotNull("Error: Null Uri returned.  You must fill-in buildFavorite in FavoriteContract.",
                favUri);
        assertEquals("Error: Favorite not properly appended to the end of the Uri",
                TEST_URI_EXT, favUri.getLastPathSegment());
        assertEquals("Error: Favorite Uri doesn't match our expected result",
                favUri.toString(),
                "content://com.example.mainmachine.popularmoviespart2/favorite/1a");
    }

}
