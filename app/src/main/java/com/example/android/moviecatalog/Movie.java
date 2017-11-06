package com.example.android.moviecatalog;

import android.graphics.Bitmap;

/**
 * Created by Astraeus on 11/6/2017.
 */

public class Movie {

    private String mMovieTitle;
    private Bitmap mMoviePoster;
    private String mMovieGenre;         // This might change to an array of strings or ints
    private String mMovieReleaseDate;
    private String mMoviePlot;
    private double mMovieRating;

    /**
     * The constructor for the Movie object will take all parameters from above
     */
    public Movie (String movieTitle,
                  String movieGenre,
                  String movieReleaseDate,
                  String moviePlot,
                  double movieRating,
                  Bitmap moviePoster){
        mMovieTitle = movieTitle;
        mMovieGenre = movieGenre;
        mMovieReleaseDate = movieReleaseDate;
        mMoviePlot = moviePlot;
        mMovieRating = movieRating;
        mMoviePoster = moviePoster;
    }

    /**
     * Implement getter methods so that getting the different components for the movie will be easy
     */
    public String getMovieTitle(){ return mMovieTitle; }
    public String getMovieGenre(){ return mMovieGenre; }
    public String getMovieReleaseDate(){ return mMovieReleaseDate; }
    public String getMoviePlot(){ return mMoviePlot; }
    public double getMovieRating(){ return mMovieRating; }
    public Bitmap getMoviePoster(){ return mMoviePoster; }

}
