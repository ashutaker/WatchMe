package com.example.android.watchme.app;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by U0162467 on 5/26/2016.
 */
public class TrailerViewAdapter extends RecyclerView.Adapter<TrailerViewAdapter.TrailersViewHolder> {
    LayoutInflater inflater;
    private Context mContext;
    private ClickListener clickListener;

    List<String> mData;

    public TrailerViewAdapter(Context context, ArrayList<String> data) {

        this.mData = data;
        this.mContext = context;
        inflater = LayoutInflater.from(mContext);
    }

    public void swap(ArrayList<String> data) {

        if (mData == null && data != null) {
            mData.addAll(data);
        }
        notifyDataSetChanged();
    }


    public void setOnClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public TrailersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.trailers_list_item_layout, parent, false);

        TrailersViewHolder holder = new TrailersViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(TrailersViewHolder holder, int position) {

        String current = mData.get(position);
        holder.trailerTitle.setText(current);
        holder.icon.setImageResource(android.R.drawable.ic_media_play);
    }


    @Override
    public int getItemCount() {
        if (mData != null) {

            return mData.size();
        } else {
            return 0;
        }
    }

    public class TrailersViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView trailerTitle;
        ImageView icon;

        public TrailersViewHolder(View itemView) {
            super(itemView);

            trailerTitle = (TextView) itemView.findViewById(R.id.trailer_link);
            icon = (ImageView) itemView.findViewById(R.id.trailer_icon);

            trailerTitle.setOnClickListener(this);
            icon.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {
            if (clickListener != null) {
                clickListener.itemClicked(v, getLayoutPosition());
            } else {
                Log.v("Click Listener ", " NULL");
            }
        }
    }


    public interface ClickListener {
        public void itemClicked(View view, int position);
    }
}
