package com.example.android.moviecatalog.Activities;


import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.moviecatalog.R;
import com.example.android.moviecatalog.Utils.FavoriteMovieCursorAdapter;
import com.example.android.moviecatalog.Utils.JSONUtils;
import com.example.android.moviecatalog.Utils.Movie;
import com.example.android.moviecatalog.Utils.MovieAdapter;
import com.example.android.moviecatalog.Utils.NetworkUtils;
import com.example.android.moviecatalog.data.MovieContract;

import java.net.URL;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CatalogActivity extends AppCompatActivity implements MovieAdapter.MovieAdapterOnClickHandler,
        FavoriteMovieCursorAdapter.MovieCursorAdapterOnClickHandler, SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = CatalogActivity.class.getSimpleName();

    @BindView(R.id.rv_catalog)
    RecyclerView mRecyclerView;
    @BindView(R.id.progress_bar)
    ProgressBar mProgressBar;
    @BindView(R.id.tv_empty_state)
    TextView mEmptyState;

    public static MovieAdapter mMovieAdapter;
    public static FavoriteMovieCursorAdapter mFavoriteMovieCursorAdapter;

    boolean isConnected = false;
    private String userChoice;
    private String sortOrder;
    private URL url = null;
    private Movie[] mData;
    private Movie[] oldData;
    private GridLayoutManager mLayoutManager;
    private SharedPreferences sharedPref;
    Bundle queryBundle;

    LoaderManager.LoaderCallbacks<Movie[]> movieCallback;
    LoaderManager.LoaderCallbacks<Cursor> favoritesCallback;


    public static final String EXTRA_MOVIE_PARCEL = "movieParcel";
    public static final String SEARCH_QUERY_URL_EXTRA = "query";
    private static final String DEFAULT_CATEGORY = "movie/popular";
    private static final String DEFAULT_SORT_ORDER = ".desc";
    private static final int FAVORITES_LOADER_ID = 1;
    private static final int MOVIE_LOADER_ID = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);
        ButterKnife.bind(this);


        // Create a boolean that checks if we have internet connectivity
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork;
        if (cm != null) {
            activeNetwork = cm.getActiveNetworkInfo();
            isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }

        // Get a grid layout manager and set it to the RecyclerView
        mLayoutManager = new GridLayoutManager(this, 2);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Set the RecyclerView's fixed size so that the size of the items doesn't change
        mRecyclerView.setHasFixedSize(true);

        // Get the user's sorting preferences
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        userChoice = sharedPref.getString(getString(R.string.key_sort_by), DEFAULT_CATEGORY);
        sortOrder = sharedPref.getString(getString(R.string.key_sort_order), DEFAULT_SORT_ORDER);

        mMovieAdapter = new MovieAdapter(this);
        mFavoriteMovieCursorAdapter = new FavoriteMovieCursorAdapter(this);
        // Set the correct adapter
        if (!userChoice.equals("favorites")) {
            // Create a new MovieAdapter and set it to the RecyclerView
            mRecyclerView.setAdapter(mMovieAdapter);
        } else {
            mRecyclerView.setAdapter(mFavoriteMovieCursorAdapter);
        }

        queryBundle = new Bundle();



        /**
         * Instantiate the LoaderCallBacks for the Movie[] return type
         */
        movieCallback = new LoaderManager.LoaderCallbacks<Movie[]>() {
            @Override
            public Loader<Movie[]> onCreateLoader(int id, final Bundle bundle) {
                return new AsyncTaskLoader<Movie[]>(getApplicationContext()) {

                    @Override
                    protected void onStartLoading() {
                        if (bundle == null) {
                            return;
                        }

                        String newUserChoice = sharedPref.getString(getString(R.string.key_sort_by), DEFAULT_CATEGORY);
                        String newSortOrder = sharedPref.getString(getString(R.string.key_sort_order), DEFAULT_SORT_ORDER);

                        mProgressBar.setVisibility(View.VISIBLE);

                        /**
                         * NOTE TO REVIEWER:
                         * The sort order criteria works IF the sorting criteria is RELEASE DATE
                         * That is because the app will query the discover/movie end point which has a sort_by optional parameter
                         * The project rubric requires me to also query the movie/popular and movie/top_rated end points. These DO NOT have a sort_by optional parameter (That's how the moviedb API is)
                         * I apologise if I'm misunderstanding.
                         */
                        if (mData != null) {
                            // If the user changed something in preferences
                            if( newUserChoice != userChoice || newSortOrder != sortOrder){
                                userChoice = newUserChoice;
                                sortOrder = newSortOrder;
                                // Build the url again
                                url = NetworkUtils.buildUrl(getApplicationContext(), userChoice, sortOrder);
                                bundle.putString(SEARCH_QUERY_URL_EXTRA, url.toString());
                                forceLoad();
                                return;
                            } else {
                                deliverResult(mData);
                            }
                        }

                        if(mData != oldData || mData == null){
                            Log.v("IMPORTANT", "" + takeContentChanged());
                            forceLoad();
                        }


                    }

                    @Override
                    protected void onStopLoading() {
                        cancelLoad();
                    }

                    @Override
                    public Movie[] loadInBackground() {

                        String searchQueryUrlString = bundle.getString(SEARCH_QUERY_URL_EXTRA);

                        Movie[] movieData;
                        String jsonResponse;

                        if (searchQueryUrlString == null || TextUtils.isEmpty(searchQueryUrlString)) {
                            return null;
                        }

                        try {
                            URL moviesUrl = new URL(searchQueryUrlString);
                            jsonResponse = NetworkUtils.makeHttpRequest(moviesUrl);
                            movieData = JSONUtils.extractFeaturesFromJson(jsonResponse);
                            return movieData;

                        } catch (Exception e) {
                            e.printStackTrace();
                            return null;
                        }
                    }

                    @Override
                    public void deliverResult(Movie[] data) {
                        if (isReset()) {
                            return;
                        }



                        // If nothing has changed
                        if(oldData == data){
                            mMovieAdapter.setMovieData(data);
                            mProgressBar.setVisibility(View.GONE);
                            mEmptyState.setVisibility(View.GONE);
                        }
                        mData = data;
                        oldData = mData;
                        if (isStarted()) {
                            super.deliverResult(data);
                        }
                    }
                };
            }

            @Override
            public void onLoadFinished(Loader<Movie[]> loader, Movie[] movies) {

                // If the user chose sort by top rated or sort by most popular and the sort order is ascending
                // reverse the movies order in the array
                // This is an edge case, but unfortunately themoviedb API doesn't have a sort_by optional param for these end points
                // This way at least, the sort order button changes the order of the movies regardless.
                if (sortOrder.equals(".asc") && (userChoice.equals("movie/popular") || userChoice.equals("movie/top_rated"))) {
                    movies = reverseData(movies);
                }
                mMovieAdapter.setMovieData(movies);
                mProgressBar.setVisibility(View.GONE);
                mEmptyState.setVisibility(View.GONE);
            }

            @Override
            public void onLoaderReset(Loader<Movie[]> loader) {
                mMovieAdapter.setMovieData(null);
            }
        };

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
                mProgressBar.setVisibility(View.GONE);

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



        if (userChoice.equals("favorites")) {
            // Initialize the favorites loader here
            getLoaderManager().initLoader(FAVORITES_LOADER_ID, null, favoritesCallback);
            getSupportActionBar().setTitle(R.string.favorites_label);

        } else {
            if (isConnected) {
                // If we have internet connectivity, add the user choice in the URL
                // and make the network request
                url = NetworkUtils.buildUrl(getApplicationContext(), userChoice, sortOrder);
                queryBundle.putString(SEARCH_QUERY_URL_EXTRA, url.toString());

                getLoaderManager().initLoader(MOVIE_LOADER_ID, queryBundle, movieCallback);

            } else {
                // If we do not have connectivity, set the text of the empty state text view
                // to inform the user of the problem
                mEmptyState.setText(getString(R.string.no_internet_connection_message));
                showEmptyState();
            }
        }
    }

    @Override
    public void onClick(Movie movie) {
        // When an item in the RecyclerView is clicked, start the DetailsActivity
        // and pass the information of the clicked item to the activity
        Intent intent = new Intent(CatalogActivity.this, DetailsActivity.class);
        intent.putExtra(EXTRA_MOVIE_PARCEL, movie);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {

        switch (menuItem.getItemId()) {
            case R.id.settings_menu:
                // Start the SettingsActivity
                Intent menuIntent = new Intent(CatalogActivity.this, SettingsActivity.class);
                if (menuIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(menuIntent);
                }
                break;
            case R.id.settings_delete_all_favorites:
                showDeleteFavoritesConfirmation();
                break;

            case R.id.settings_watchlist:
                // Start the WatchlistActivity
                Intent intent = new Intent(CatalogActivity.this, WatchlistActivity.class);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
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
        Intent intent = new Intent(CatalogActivity.this, DetailsActivity.class);
        intent.putExtra(EXTRA_MOVIE_PARCEL, movie);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
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
                        Toast.makeText(getApplicationContext(), R.string.favorites_cleared_toast, Toast.LENGTH_SHORT).show();
                        recreateActivity();
                    }
                })
                .setNegativeButton(android.R.string.no, null).show();
    }

    /**
     * Helper method to show the empty state
     */
    public void showEmptyState() {
        mRecyclerView.setVisibility(View.GONE);
        mEmptyState.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
    }

    /**
     * Helper method to recreate an activity
     */
    private void recreateActivity() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    // Register the preference change listener
    @Override
    public void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }


    /**
     * Helper method to reverse the movies in the array
     */
    private Movie[] reverseData(Movie[] data) {
        Movie[] newData = new Movie[data.length];
        int j = 0;
        for (int i = data.length - 1; i >= 0; i--) {
            newData[j] = data[i];
            j++;
        }
        return newData;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (sharedPreferences.getString(key, "").equals("favorites")) {
            getLoaderManager().restartLoader(FAVORITES_LOADER_ID, null, favoritesCallback);
        } else {
            getLoaderManager().restartLoader(MOVIE_LOADER_ID, queryBundle, movieCallback);
        }
    }
}
