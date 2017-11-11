package com.example.android.moviecatalog;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by Astraeus on 11/8/2017.
 */

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieAdapterViewHolder> {

    private Movie[] mMovieData;

    private final MovieAdapterOnClickHandler mClickHandler;

    public interface MovieAdapterOnClickHandler {
        void onClick(Movie movie);
    }

    public MovieAdapter(MovieAdapterOnClickHandler clickHandler) {
        mClickHandler = clickHandler;
    }


    public class MovieAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final ImageView mMoviePoster;
        private final TextView mMovieTitle;

        public MovieAdapterViewHolder(View view) {
            super(view);

            mMoviePoster = view.findViewById(R.id.iv_movie_poster);
            mMovieTitle = view.findViewById(R.id.tv_movie_title);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Movie movie = mMovieData[getAdapterPosition()];
            mClickHandler.onClick(movie);
        }
    }

    @Override
    public MovieAdapter.MovieAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_layout, parent, false);
        return new MovieAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieAdapter.MovieAdapterViewHolder holder, int position) {

        // Set the movie title and poster for the Catalog Activity
        Movie currentMovie = mMovieData[position];
        String posterURLString = currentMovie.getMoviePoster();

        Picasso.with(holder.mMoviePoster.getContext()).load(posterURLString).into(holder.mMoviePoster);
        holder.mMovieTitle.setText(currentMovie.getMovieTitle());

    }

    @Override
    public int getItemCount() {
        if (null == mMovieData) return 0;
        return mMovieData.length;
    }

    public void setMovieData(Movie[] movieData) {
        mMovieData = movieData;
        notifyDataSetChanged();
    }
}
