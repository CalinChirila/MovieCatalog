package com.example.android.moviecatalog.Utils;

import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.moviecatalog.R;
import com.example.android.moviecatalog.data.MovieContract;
import com.squareup.picasso.Picasso;

/**
 * Created by Astraeus on 12/9/2017.
 */

public class FavoriteMovieCursorAdapter extends RecyclerView.Adapter<FavoriteMovieCursorAdapter.MovieCursorAdapterViewHolder> {

    private Cursor mCursor;

    private final MovieCursorAdapterOnClickHandler mCursorClickHandler;

    public interface MovieCursorAdapterOnClickHandler {
        void onCursorItemClick(Cursor cursor);
    }

    public FavoriteMovieCursorAdapter(MovieCursorAdapterOnClickHandler cursorClickHandler){
        mCursorClickHandler = cursorClickHandler;
    }


    public class MovieCursorAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView mMoviePoster;
        TextView mMovieTitle;


        public MovieCursorAdapterViewHolder(View view) {
            super(view);

            mMoviePoster = view.findViewById(R.id.iv_movie_poster);
            mMovieTitle = view.findViewById(R.id.tv_movie_title);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            // Get the clicked cursor and pass it to the CursorClickHandler
            mCursor.moveToPosition(getAdapterPosition());
            mCursorClickHandler.onCursorItemClick(mCursor);
        }
    }


    @Override
    public FavoriteMovieCursorAdapter.MovieCursorAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_layout, parent, false);
        return new MovieCursorAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FavoriteMovieCursorAdapter.MovieCursorAdapterViewHolder holder, int position) {
        // Move the cursor to the required position
        mCursor.moveToPosition(position);

        String movieTitle = mCursor.getString(mCursor.getColumnIndex(MovieContract.FavoritesEntry.COLUMN_MOVIE_TITLE));
        String posterPath = mCursor.getString(mCursor.getColumnIndex(MovieContract.FavoritesEntry.COLUMN_MOVIE_POSTER));
        Uri posterUri = Uri.parse(posterPath);

        Log.v("IMPORTANT", "Adapter poster path:" + posterPath);

        Picasso.with(holder.mMoviePoster.getContext())
                .load("file://" + posterPath)
                .placeholder(R.drawable.posternotfound)
                .error(R.drawable.posternotfound)
                .into(holder.mMoviePoster);


        holder.mMovieTitle.setText(movieTitle);
    }

    @Override
    public int getItemCount() {
        if(mCursor == null) return 0;
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor){
        mCursor = newCursor;
        notifyDataSetChanged();
    }
}
