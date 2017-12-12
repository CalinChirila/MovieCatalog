package com.example.android.moviecatalog.Utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.android.moviecatalog.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Astraeus on 12/12/2017.
 */

public class ReviewAdapter extends ArrayAdapter<Review> {

    public ReviewAdapter(@NonNull Context context, @NonNull List<Review> reviews) {
        super(context, 0, reviews);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent){
        View listItemView = convertView;
        ViewHolder holder;
        Review currentReview = getItem(position);

        if(listItemView == null){
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.review_item_layout, parent, false);
            holder = new ViewHolder(listItemView);
            listItemView.setTag(holder);
        } else {
            holder = (ViewHolder) listItemView.getTag();
        }

        holder.mReviewAuthor.setText(currentReview.getReviewAuthor());
        holder.mReviewText.setText(currentReview.getReviewText());


        return listItemView;
    }


    public class ViewHolder{
        @BindView (R.id.tv_review_author)
        TextView mReviewAuthor;
        @BindView(R.id.tv_review_text)
        TextView mReviewText;

        public ViewHolder(View view){
            ButterKnife.bind(this, view);
        }
    }
}
