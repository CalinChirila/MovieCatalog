package com.example.android.moviecatalog.Utils;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.moviecatalog.R;

/**
 * Created by Astraeus on 12/14/2017.
 */

public class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.TrailerAdapterViewHolder> {

    private String[] mTrailerData;

    private final TrailerAdapterOnClickHandler mTrailerClickHandler;

    public interface TrailerAdapterOnClickHandler{
        void onTrailerClick(String youtubeString);
    }

    public TrailerAdapter(TrailerAdapterOnClickHandler clickHandler){ mTrailerClickHandler = clickHandler; }

    public class TrailerAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView trailerView;

        public TrailerAdapterViewHolder(View itemView) {
            super(itemView);

            trailerView = itemView.findViewById(R.id.tv_trailer);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            String youtubeString = mTrailerData[getAdapterPosition()];
            mTrailerClickHandler.onTrailerClick(youtubeString);
        }
    }

    @Override
    public TrailerAdapter.TrailerAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.trailer_item_layout, parent, false);
        return new TrailerAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TrailerAdapter.TrailerAdapterViewHolder holder, int position) {

        String textViewText = "Trailer " + (position + 1);

        holder.trailerView.setText(textViewText);

    }

    @Override
    public int getItemCount() {
        if(mTrailerData == null) return 0;
        return mTrailerData.length;
    }

    public void setTrailerData(String[] data){
        mTrailerData = data;
        notifyDataSetChanged();
    }
}
