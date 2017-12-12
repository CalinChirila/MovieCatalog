package com.example.android.moviecatalog.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by Astraeus on 12/7/2017.
 */

public class MovieContentProvider extends ContentProvider {

    // The codes for the Uri matcher
    public static final int FAVORITES = 100;
    public static final int FAVORITES_WITH_ID = 101;
    public static final int WATCHLIST = 200;
    public static final int WATCHLIST_WITH_ID = 201;

    private static UriMatcher sUriMatcher = buildUriMatcher();

    /**
     * Helper method to build the UriMatcher
     * @return the uri matcher
     */
    public static UriMatcher buildUriMatcher(){
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(MovieContract.AUTHORITY, MovieContract.PATH_FAVORITES, FAVORITES);
        uriMatcher.addURI(MovieContract.AUTHORITY, MovieContract.PATH_FAVORITES + "/#", FAVORITES_WITH_ID);

        uriMatcher.addURI(MovieContract.AUTHORITY, MovieContract.PATH_WATCHLIST, WATCHLIST);
        uriMatcher.addURI(MovieContract.AUTHORITY, MovieContract.PATH_WATCHLIST + "/#", WATCHLIST_WITH_ID);

        return uriMatcher;
    }

    private MovieDbHelper mDbHelper;


    @Override
    public boolean onCreate() {

        // Initialize a MovieDbHelper on startup.
        Context context = getContext();
        mDbHelper = new MovieDbHelper(context);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        final SQLiteDatabase db = mDbHelper.getReadableDatabase();
        int match = sUriMatcher.match(uri);
        Cursor cursor;

        switch(match){
            case FAVORITES:
                cursor = db.query(
                        MovieContract.FavoritesEntry.TABLE_NAME,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );
                break;
            case WATCHLIST:
                cursor = db.query(
                        MovieContract.WatchlistEntry.TABLE_NAME,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);


        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }


    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match){
            case FAVORITES:
                long id = db.insert(MovieContract.FavoritesEntry.TABLE_NAME, null, contentValues);
                if(id > 0){
                    returnUri = ContentUris.withAppendedId(MovieContract.FavoritesEntry.CONTENT_URI, id);
                } else {
                    throw new SQLException("Failed to insert row into uri " + uri);
                }
                break;
            case WATCHLIST:
                long id1 = db.insert(MovieContract.WatchlistEntry.TABLE_NAME, null, contentValues);
                if(id1 > 0){
                    returnUri = ContentUris.withAppendedId(MovieContract.WatchlistEntry.CONTENT_URI, id1);
                } else {
                    throw new SQLException("Failed to insert row into uri " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("An error occurred when inserting item with uri " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        int modifiedRows;

        switch(match){
            case FAVORITES:
                modifiedRows = db.delete(MovieContract.FavoritesEntry.TABLE_NAME, null, null);
                break;
            case FAVORITES_WITH_ID:
                String id = uri.getPathSegments().get(1);   // Get the id of the item in question.
                String where = "_id=?";
                String[] args = new String[]{id};
                modifiedRows = db.delete(MovieContract.FavoritesEntry.TABLE_NAME, where, args);
                break;
            case WATCHLIST:
                modifiedRows = db.delete(MovieContract.WatchlistEntry.TABLE_NAME, null, null);
                break;
            case WATCHLIST_WITH_ID:
                String id1 = uri.getPathSegments().get(1);
                String where1 = "_id=?";
                String[] args1 = new String[]{id1};
                modifiedRows = db.delete(MovieContract.WatchlistEntry.TABLE_NAME, where1, args1);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri " + uri);
        }

        if(modifiedRows != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return modifiedRows;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {
        // The user will not change any values in the table entries, so this method remains as is.
        return 0;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }
}
