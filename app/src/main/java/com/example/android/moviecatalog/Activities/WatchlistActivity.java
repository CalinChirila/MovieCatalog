package com.example.android.moviecatalog.Activities;

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.android.moviecatalog.R;
import com.example.android.moviecatalog.Utils.WatchlistMovieCursorAdapter;
import com.example.android.moviecatalog.data.MovieContract;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WatchlistActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

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
            protected void onStartLoading(){
                if(mWatchlistData != null){
                    deliverResult(mWatchlistData);
                } else {
                    forceLoad();
                }
            }

            @Override
            public Cursor loadInBackground() {

                try{
                   return getContentResolver().query(
                            MovieContract.WatchlistEntry.CONTENT_URI,
                           null,
                           null,
                           null,
                           null
                   );
                } catch(Exception e){
                    Log.e(TAG, "Failed to load watchlist");
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public void deliverResult(Cursor data){
                mWatchlistData = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mWatchlistAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mWatchlistAdapter.swapCursor(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.watchlist_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem){
        int id = menuItem.getItemId();
        switch(id){
            case R.id.menu_delete_watchlist:
                //TODO: add confirmation dialog here
                getContentResolver().delete(MovieContract.WatchlistEntry.CONTENT_URI, null, null);
                // TODO: make the emptyState visible
        }
        return super.onOptionsItemSelected(menuItem);
    }
}
