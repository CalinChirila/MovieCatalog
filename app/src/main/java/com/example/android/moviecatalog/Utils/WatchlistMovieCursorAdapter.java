package com.example.android.moviecatalog.Utils;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.moviecatalog.R;
import com.example.android.moviecatalog.data.MovieContract;
import com.squareup.picasso.Picasso;

/**
 * Created by Astraeus on 12/10/2017.
 */

public class WatchlistMovieCursorAdapter extends RecyclerView.Adapter<WatchlistMovieCursorAdapter.MovieCursorAdapterViewHolder> {

    private Cursor mCursor;

    public WatchlistMovieCursorAdapter() {}

    class MovieCursorAdapterViewHolder extends RecyclerView.ViewHolder {

        ImageView mMoviePoster;
        TextView mMovieTitle;

        MovieCursorAdapterViewHolder(View view) {
            super(view);

            mMoviePoster = view.findViewById(R.id.iv_watchlist_movie_poster);
            mMovieTitle = view.findViewById(R.id.tv_watchlist_movie_title);

        }
    }

    @Override
    public WatchlistMovieCursorAdapter.MovieCursorAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.watchlist_item_layout, parent, false);
        return new MovieCursorAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(WatchlistMovieCursorAdapter.MovieCursorAdapterViewHolder holder, int position) {
        mCursor.moveToPosition(position);

        String movieTitle = mCursor.getString(mCursor.getColumnIndex(MovieContract.WatchlistEntry.COLUMN_MOVIE_TITLE));
        String moviePoster = mCursor.getString(mCursor.getColumnIndex(MovieContract.WatchlistEntry.COLUMN_MOVIE_POSTER));
        int id = mCursor.getInt(mCursor.getColumnIndex(MovieContract.WatchlistEntry._ID));

        Picasso.with(holder.mMoviePoster.getContext())
                .load(moviePoster)
                .error(R.drawable.posternotfound)
                .placeholder(R.drawable.posternotfound)
                .into(holder.mMoviePoster);

        holder.mMovieTitle.setText(movieTitle);

        holder.itemView.setTag(id);
    }

    @Override
    public int getItemCount() {
        if(mCursor == null) return 0;
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }
}
