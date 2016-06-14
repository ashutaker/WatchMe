package com.example.android.watchme.app;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by u0162467 on 6/2/2016.
 */
public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    LayoutInflater inflater;
    private Context mContext;

    List<String> mData;

    ReviewAdapter(Context context, ArrayList<String> data) {
        this.mContext = context;
        this.mData = data;
        inflater = LayoutInflater.from(mContext);
    }

    @Override
    public ReviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.reviews_list_item_layout, parent, false);

        ReviewViewHolder holder = new ReviewViewHolder(view);
        return holder;
    }

    public void swap(ArrayList<String> data) {
        if (data != null) {
            if (mData == null) {
                mData.addAll(data);
            }
        } else {
            data.add("No reviews yet.");
            mData.addAll(data);
        }
        notifyDataSetChanged();
        Log.v("Review from adapter : ", String.valueOf(data));
    }


    @Override
    public void onBindViewHolder(ReviewViewHolder holder, int position) {
        holder.textView.setText(mData.get(position));
    }

    @Override
    public int getItemCount() {
        if (mData != null) {
            return mData.size();
        } else {
            return 0;
        }
    }


    public class ReviewViewHolder extends RecyclerView.ViewHolder {

        TextView textView;

        public ReviewViewHolder(View itemView) {
            super(itemView);

            textView = (TextView) itemView.findViewById(R.id.review_content);
        }
    }
}