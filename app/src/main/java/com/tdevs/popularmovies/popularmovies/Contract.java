package com.tdevs.popularmovies.popularmovies;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Tomasz on 04.05.2017.
 */

public class Contract {

    public static final String AUTHORITY = "com.tdevs.popularmovies.popularmovies";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String PATH_FAVORITES = "favorites";

    public static final class FavoriteEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAVORITES).build();

        public static final String TABLE_NAME = "favorites_table";
        public static final String COLUMN_MOVID = "movie_id";   //some movies seem duplicated in the movie database. Some appear sometimes with two different posters (displayed in the list)
                                                                //that is why it is better to store separately movie id and the id of the record in the local database (that column is automatically created because of BaseColumns implementation)
        public static final String COLUMN_TITLE = "title";
    }
}
