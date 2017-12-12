package com.example.android.moviecatalog.Utils;

/**
 * Created by Astraeus on 12/12/2017.
 */

public class Review {
    private String mReviewAuthor;
    private String mReviewText;

    public Review(String author, String text){
        mReviewAuthor = author;
        mReviewText = text;
    }

    public String getReviewAuthor(){ return mReviewAuthor; }
    public String getReviewText(){ return mReviewText; }
}
