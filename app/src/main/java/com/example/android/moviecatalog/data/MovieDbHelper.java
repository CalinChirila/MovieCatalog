package com.example.android.moviecatalog.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.moviecatalog.data.MovieContract.FavoritesEntry;
import com.example.android.moviecatalog.data.MovieContract.WatchlistEntry;

/**
 * Created by Astraeus on 12/7/2017.
 */

public class MovieDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "movies.db";
    private static final int DATABASE_VERSION = 2;

    // The SQL statement for creating the favorites table
    private static final String SQL_CREATE_FAVORITES_TABLE =
            "CREATE TABLE " + FavoritesEntry.TABLE_NAME + " ("
                    + FavoritesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + FavoritesEntry.COLUMN_MOVIE_ID + " INTEGER, "
                    + FavoritesEntry.COLUMN_MOVIE_TITLE + " TEXT NOT NULL, "
                    + FavoritesEntry.COLUMN_MOVIE_VOTE_AVERAGE + " REAL NOT NULL, "
                    + FavoritesEntry.COLUMN_MOVIE_RELEASE_DATE + " TEXT NOT NULL, "
                    + FavoritesEntry.COLUMN_MOVIE_PLOT + " TEXT, "
                    + FavoritesEntry.COLUMN_MOVIE_POSTER + " TEXT, "
                    + FavoritesEntry.COLUMN_MOVIE_REVIEWS + " TEXT, "
                    + FavoritesEntry.COLUMN_MOVIE_TRAILER + " TEXT);";

    // The SQL statement for creating the watchlist table
    private static final String SQL_CREATE_WATCHLIST_TABLE =
            "CREATE TABLE " + WatchlistEntry.TABLE_NAME + " ("
                    + WatchlistEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + WatchlistEntry.COLUMN_MOVIE_TITLE + " TEXT NOT NULL, "
                    + WatchlistEntry.COLUMN_MOVIE_POSTER + " TEXT NOT NULL);";

    // The SQL statements for deleting both tables.
    private static final String SQL_DELETE_FAVORITES_TABLE = "DROP TABLE IF EXISTS " + FavoritesEntry.TABLE_NAME;
    private static final String SQL_DELETE_WATCHLIST_TABLE = "DROP TABLE IF EXISTS " + WatchlistEntry.TABLE_NAME;

    // The constructor
    MovieDbHelper (Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_FAVORITES_TABLE);
        db.execSQL(SQL_CREATE_WATCHLIST_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_FAVORITES_TABLE);
        db.execSQL(SQL_DELETE_WATCHLIST_TABLE);
        onCreate(db);
    }
}
