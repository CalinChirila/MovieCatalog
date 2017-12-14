package com.example.android.moviecatalog.Activities;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.android.moviecatalog.R;
import com.example.android.moviecatalog.Utils.WatchlistMovieCursorAdapter;
import com.example.android.moviecatalog.data.MovieContract;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WatchlistActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = WatchlistActivity.class.getSimpleName();

    @BindView(R.id.rv_watchlist)
    RecyclerView mWatchlistRecyclerView;
    @BindView(R.id.tv_watchlist_empty_state)
    TextView mWatchlistEmptyState;

    public static WatchlistMovieCursorAdapter mWatchlistAdapter;

    private static final int WATCHLIST_LOADER_ID = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watchlist);
        ButterKnife.bind(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mWatchlistRecyclerView.setLayoutManager(layoutManager);

        mWatchlistRecyclerView.setHasFixedSize(true);

        mWatchlistAdapter = new WatchlistMovieCursorAdapter();

        mWatchlistRecyclerView.setAdapter(mWatchlistAdapter);

        getLoaderManager().initLoader(WATCHLIST_LOADER_ID, null, this);

        /**
         * Users can swipe left or right to delete items from the watchlist
         */
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

                int id = (int) viewHolder.itemView.getTag();

                String stringId = Integer.toString(id);
                Uri uri = MovieContract.WatchlistEntry.CONTENT_URI;
                uri = uri.buildUpon().appendPath(stringId).build();

                getContentResolver().delete(uri, null, null);
                getLoaderManager().restartLoader(WATCHLIST_LOADER_ID, null, WatchlistActivity.this);

            }
        }).attachToRecyclerView(mWatchlistRecyclerView);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        return new AsyncTaskLoader<Cursor>(this) {

            Cursor mWatchlistData = null;

            @Override
            protected void onStartLoading() {
                if (mWatchlistData != null) {
                    deliverResult(mWatchlistData);
                } else {
                    forceLoad();
                }
            }

            @Override
            public Cursor loadInBackground() {

                try {
                    return getContentResolver().query(
                            MovieContract.WatchlistEntry.CONTENT_URI,
                            null,
                            null,
                            null,
                            null
                    );
                } catch (Exception e) {
                    Log.e(TAG, "Failed to load watchlist");
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public void deliverResult(Cursor data) {
                mWatchlistData = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mWatchlistAdapter.swapCursor(cursor);

        if(cursor.getCount() == 0){
            showEmptyState();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mWatchlistAdapter.swapCursor(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.watchlist_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int id = menuItem.getItemId();
        switch (id) {
            case R.id.menu_delete_watchlist:
                createDeleteWatchlistConfirmation();
                break;
            case R.id.menu_share_watchlist:
                Intent shareIntent = createWatchlistShareIntent();
                if (shareIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(shareIntent);
                }
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    /**
     * Helper method to create the share watchlist intent
     */
    private Intent createWatchlistShareIntent() {
        // Create an intent to share the items on the watchlist (just the movie titles)
        // First query the watchlist db
        String[] projection = {MovieContract.WatchlistEntry.COLUMN_MOVIE_TITLE};
        Cursor watchlistCursor = getContentResolver().query(MovieContract.WatchlistEntry.CONTENT_URI, projection, null, null, null);

        // Create a string with every movie title in the watchlist
        String textSegment;
        StringBuilder builder = new StringBuilder();
        int index = 1;
        while (watchlistCursor.moveToNext()) {
            textSegment = String.valueOf(index) + ". " + watchlistCursor.getString(watchlistCursor.getColumnIndex(MovieContract.WatchlistEntry.COLUMN_MOVIE_TITLE)) + "\n\n";
            index++;
            builder.append(textSegment);
        }
        String shareText = builder.toString();
        watchlistCursor.close();

        // Create the share intent
        Intent shareIntent = ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setText(shareText)
                .getIntent();
        shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);

        return shareIntent;
    }

    /**
     * Helper method to create the confirmation for deleting the watchlist
     */
    private void createDeleteWatchlistConfirmation() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle(R.string.confirmation_delete_watchlist_title)
                .setMessage(R.string.confirmation_delete_watchlist_message)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new AlertDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Delete the watchlist
                        getContentResolver().delete(MovieContract.WatchlistEntry.CONTENT_URI, null, null);
                        recreateActivity();
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
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
     * Helper method to reveal the empty state
     */
    private void showEmptyState(){
        mWatchlistRecyclerView.setVisibility(View.GONE);
        mWatchlistEmptyState.setVisibility(View.VISIBLE);
    }
}
