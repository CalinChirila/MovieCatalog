package com.example.android.moviecatalog.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Astraeus on 12/7/2017.
 */

public class MovieContract {

    public static final String AUTHORITY = "com.example.android.moviecatalog";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    // The two paths for the two entries. One for the favorites table and one for the watchlist.
    public static final String PATH_FAVORITES = "favorites";
    public static final String PATH_WATCHLIST = "watchlist";

    // The favorites table
    public static final class FavoritesEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI
                .buildUpon()
                .appendPath(PATH_FAVORITES)
                .build();

        public static final String TABLE_NAME = "favorites";

        // We need this table to contain all the information that is displayed in the DetailsActivity
        // when the user has internet connection. This way, we will be able to display the list of
        // favorite movies even if the user doesn't have internet connectivity.
        public static final String COLUMN_MOVIE_ID = "movieID";
        public static final String COLUMN_MOVIE_POSTER = "poster";
        public static final String COLUMN_MOVIE_TITLE = "title";
        public static final String COLUMN_MOVIE_VOTE_AVERAGE = "voteAverage";
        public static final String COLUMN_MOVIE_RELEASE_DATE = "releaseDate";
        public static final String COLUMN_MOVIE_TRAILER = "trailer";
        public static final String COLUMN_MOVIE_REVIEWS = "reviews";
        public static final String COLUMN_MOVIE_PLOT = "plot";
    }

    public static final class WatchlistEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_WATCHLIST)
                .build();

        public static final String TABLE_NAME = "watchlist";

        // We only need the movie poster and the title for the watchlist.
        public static final String COLUMN_MOVIE_POSTER = "poster";
        public static final String COLUMN_MOVIE_TITLE = "title";
    }
}
