package com.example.android.moviecatalog.Activities;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.android.moviecatalog.R;
import com.example.android.moviecatalog.Utils.FavoriteMovieCursorAdapter;
import com.example.android.moviecatalog.Utils.Movie;
import com.example.android.moviecatalog.data.MovieContract;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FavoritesActivity extends AppCompatActivity implements FavoriteMovieCursorAdapter.MovieCursorAdapterOnClickHandler{
    @BindView(R.id.rv_favorites)
    RecyclerView mRecyclerView;
    @BindView(R.id.tv_favorites_empty_state)
    TextView mEmptyState;

    private LoaderManager.LoaderCallbacks<Cursor> favoritesCallback;
    private GridLayoutManager mLayoutManager;
    public static FavoriteMovieCursorAdapter mFavoriteMovieCursorAdapter;

    private static final int FAVORITES_LOADER_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);
        ButterKnife.bind(this);

        // Get a grid layout manager and set it to the RecyclerView
        mLayoutManager = new GridLayoutManager(this, 2);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Set the RecyclerView's fixed size so that the size of the items doesn't change
        mRecyclerView.setHasFixedSize(true);
        mFavoriteMovieCursorAdapter = new FavoriteMovieCursorAdapter(this);
        mRecyclerView.setAdapter(mFavoriteMovieCursorAdapter);

        /**
         * Instantiate the LoaderCallBacks for the Cursor return type
         */
        favoritesCallback = new LoaderManager.LoaderCallbacks<Cursor>() {

            @Override
            public Loader<Cursor> onCreateLoader(int id, final Bundle bundle) {
                return new AsyncTaskLoader<Cursor>(getApplicationContext()) {
                    Cursor mFavoritesData = null;

                    @Override
                    protected void onStartLoading() {

                        if (mFavoritesData != null) {
                            deliverResult(mFavoritesData);
                        } else {
                            forceLoad();
                        }
                    }

                    @Override
                    public Cursor loadInBackground() {

                        try {
                            return getContentResolver().query(
                                    MovieContract.FavoritesEntry.CONTENT_URI,
                                    null,
                                    null,
                                    null,
                                    null
                            );
                        } catch (Exception e) {

                            e.printStackTrace();
                            return null;
                        }
                    }

                    @Override
                    public void deliverResult(Cursor data) {
                        mFavoritesData = data;
                        super.deliverResult(data);
                    }
                };
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
                if(cursor != null) {
                    mFavoriteMovieCursorAdapter.swapCursor(cursor);
                }
                if (cursor.getCount() == 0) {
                    showEmptyState();
                    mEmptyState.setText(R.string.add_to_favorites_empty_state);
                }
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
                mFavoriteMovieCursorAdapter.swapCursor(null);
            }
        };

        getLoaderManager().initLoader(FAVORITES_LOADER_ID, null, favoritesCallback);

    }

    /**
     * Helper method to show the empty state
     */
    public void showEmptyState() {
        mRecyclerView.setVisibility(View.GONE);
        mEmptyState.setVisibility(View.VISIBLE);

    }

    @Override
    public void onCursorItemClick(Cursor cursor) {
        // Create a new movie object
        Movie movie = new Movie();

        // Store the required data
        String movieTitle = cursor.getString(cursor.getColumnIndex(MovieContract.FavoritesEntry.COLUMN_MOVIE_TITLE));
        String movieReleaseDate = cursor.getString(cursor.getColumnIndex(MovieContract.FavoritesEntry.COLUMN_MOVIE_RELEASE_DATE));
        String moviePlot = cursor.getString(cursor.getColumnIndex(MovieContract.FavoritesEntry.COLUMN_MOVIE_PLOT));
        String moviePoster = cursor.getString(cursor.getColumnIndex(MovieContract.FavoritesEntry.COLUMN_MOVIE_POSTER));
        double movieRating = cursor.getDouble(cursor.getColumnIndex(MovieContract.FavoritesEntry.COLUMN_MOVIE_VOTE_AVERAGE));
        int movieId = cursor.getInt(cursor.getColumnIndex(MovieContract.FavoritesEntry.COLUMN_MOVIE_ID));

        // Add the data from above to the new movie object
        movie.setMovieTitle(movieTitle);
        movie.setMovieReleaseDate(movieReleaseDate);
        movie.setMoviePlot(moviePlot);
        movie.setMoviePoster("file://" + moviePoster);
        movie.setMovieRating(movieRating);
        movie.setMovieId(movieId);

        // Create the intent to start the DetailsActivity
        Intent intent = new Intent(FavoritesActivity.this, DetailsActivity.class);
        intent.putExtra(CatalogActivity.EXTRA_MOVIE_PARCEL, movie);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        getLoaderManager().restartLoader(FAVORITES_LOADER_ID, null, favoritesCallback);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.favorites_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem){
        switch(menuItem.getItemId()){
            case R.id.settings_delete_all_favorites:
                showDeleteFavoritesConfirmation();
                break;
            case R.id.menu_share_favorites:
                Intent shareIntent = createShareFavoritesIntent();
                if (shareIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(shareIntent);
                }
                break;
        }

        return super.onOptionsItemSelected(menuItem);
    }

    /**
     * Helper method for the delete favorites confirmation dialog
     */
    private void showDeleteFavoritesConfirmation() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle(R.string.delete_favorites_confirmation_title)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setMessage(R.string.delete_favorites_confirmation_message)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Delete favorites
                        getContentResolver().delete(MovieContract.FavoritesEntry.CONTENT_URI, null, null);
                        recreateActivity();
                    }
                })
                .setNegativeButton(android.R.string.no, null).show();
    }

    /**
     * Helper method to recreate an activity
     */
    private void recreateActivity() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    /**
     * Helper method to create the share favorites intent
     */
    private Intent createShareFavoritesIntent() {
        // Create an intent to share the items on the favorites list (just the movie titles)
        // First query the favorites db
        String[] projection = {MovieContract.FavoritesEntry.COLUMN_MOVIE_TITLE};
        Cursor favoritesCursor = getContentResolver().query(MovieContract.FavoritesEntry.CONTENT_URI, projection, null, null, null);

        // Create a string with every movie title in the favorites list
        String textSegment;
        StringBuilder builder = new StringBuilder();
        int index = 1;
        while (favoritesCursor.moveToNext()) {
            textSegment = String.valueOf(index) + ". " + favoritesCursor.getString(favoritesCursor.getColumnIndex(MovieContract.WatchlistEntry.COLUMN_MOVIE_TITLE)) + "\n\n";
            index++;
            builder.append(textSegment);
        }
        String shareText = builder.toString();
        favoritesCursor.close();

        // Create the share intent
        Intent shareIntent = ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setText(shareText)
                .getIntent();
        shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);

        return shareIntent;
    }


}
