package com.tdevs.popularmovies.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tomasz on 01.05.2017.
 */

public class TrailersListAdapter extends BaseAdapter {

    ArrayList<String> trailersNames;
    ArrayList<String> trailersKeys;
    Context appContext;

    public TrailersListAdapter(List<Result> trailers, Context context)
    {
        appContext = context;
        trailersKeys = new ArrayList<>();
        trailersNames = new ArrayList<>();

        for (Result one : trailers)
        {
            System.out.println("One name: " + one.getName());
            System.out.println("One key: " + one.getKey());
            trailersKeys.add(one.getKey());
            trailersNames.add(one.getName());
        }

        trailersNames.add("empty one");
        trailersKeys.add("fake one");
    }


    @Override
    public int getCount() {
        return trailersKeys.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) appContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.trailer_list_item, viewGroup, false);
            }

        TextView trailerNameTextView = (TextView)view.findViewById(R.id.trailer_name_text_view);
        trailerNameTextView.setText(trailersNames.get(i));

        return view;
    }

    public String getTrailerKey(int num)
    {
        return trailersKeys.get(num);
    }
}
