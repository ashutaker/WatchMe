package com.example.android.watchme.app;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by U0162467 on 4/15/2016.
 */
public class GridViewAdapter extends BaseAdapter {


    private Context mContext;
    private int mLayoutResourceId;
    private ArrayList<String> mUrls;

    public GridViewAdapter(Context c,int layoutResourceId,ArrayList urls) {

        this.mContext = c;
        this.mLayoutResourceId = layoutResourceId;
        this.mUrls = urls;
    }

    @Override
    public int getCount() {
        return mUrls.size();
    }

    @Override
    public Uri getItem(int position) {
        return  null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        LayoutInflater mInflater = (LayoutInflater.from(mContext));
        if (convertView == null) {
            //imageView = new ImageView(mContext);
            convertView = mInflater.inflate(mLayoutResourceId,null,true);
            imageView = (ImageView) convertView.findViewById(R.id.grid_imageview);

            //imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }

        //Log.v("URI in adapter" , mUrls.get(position).toString());
        //imageView.setImageResource(mUrls.get(position));
              Picasso.with(mContext)
                .load(mUrls.get(position))
                .into(imageView);

        return imageView;
    }

    public void setData(ArrayList urls){
        this.mUrls = urls;
        notifyDataSetChanged();
    }

    // references to our images

}