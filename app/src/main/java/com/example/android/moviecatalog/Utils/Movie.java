package com.example.android.moviecatalog.Utils;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Astraeus on 11/6/2017.
 */

public class Movie implements Parcelable{

    private String mMovieTitle;
    private String mMoviePosterPath;
    private String mMovieReleaseDate;
    private String mMoviePlot;
    private double mMovieRating;

    /**
     * The constructor for the Movie object
     */
    public Movie (){ }

    private Movie(Parcel in){
        mMovieTitle = in.readString();
        mMoviePosterPath = in.readString();
        mMovieReleaseDate = in.readString();
        mMoviePlot = in.readString();
        mMovieRating = in.readDouble();

    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    /**
     * Implement getter methods so that getting the different components for the movie will be easy
     */
    public String getMovieTitle(){ return mMovieTitle; }
    public String getMovieReleaseDate(){ return mMovieReleaseDate; }
    public String getMoviePlot(){ return mMoviePlot; }
    public double getMovieRating(){ return mMovieRating; }
    public String getMoviePoster(){ return mMoviePosterPath; }

    /**
     * Setter methods
     */
    public void setMovieTitle(String movieTitle){
        mMovieTitle = movieTitle;
    }
    public void setMoviePoster(String moviePosterPath){
        mMoviePosterPath = moviePosterPath;
    }
    public void setMovieReleaseDate(String movieReleaseDate){
        mMovieReleaseDate = movieReleaseDate;
    }
    public void setMoviePlot(String moviePlot){
        mMoviePlot = moviePlot;
    }
    public void setMovieRating(double movieRating){
        mMovieRating = movieRating;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

        parcel.writeString(mMovieTitle);
        parcel.writeString(mMoviePosterPath);
        parcel.writeString(mMovieReleaseDate);
        parcel.writeString(mMoviePlot);
        parcel.writeDouble(mMovieRating);
    }
}
