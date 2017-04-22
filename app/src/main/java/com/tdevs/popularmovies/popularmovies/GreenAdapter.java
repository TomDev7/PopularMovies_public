package com.tdevs.popularmovies.popularmovies;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.List;

public class GreenAdapter extends RecyclerView.Adapter<GreenAdapter.NumberViewHolder> {

    private static final String TAG = GreenAdapter.class.getSimpleName();
    final private ListItemClickListener mOnClickListener;
    private int mNumberItems;
    private Context context;
    int screenWidth;
    private DatabaseWrapper databaseWrapper;
    private List<OneMovie> sortedMovies;

    interface ListItemClickListener
    {
        void onListItemClick(int itemNum);
    }


    public GreenAdapter(int numberOfItems, Context appContext, ListItemClickListener listItemClickListener) {
        mNumberItems = numberOfItems;
        context = appContext;
        mOnClickListener = listItemClickListener;
        databaseWrapper = new DatabaseWrapper(appContext);

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        if(appContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            screenWidth = size.y;
        }
        else
        {
            screenWidth = size.x;
        }
    }

    public void changeItemCount(int newItemCount)
    {
        mNumberItems = newItemCount;
    }

    public void notifyDataSetChangedOverride()
    {
        databaseWrapper.open();
        sortedMovies = databaseWrapper.getAllMovies();
        databaseWrapper.close();

        Collections.sort(sortedMovies);

        notifyDataSetChanged();
    }


    @Override
    public NumberViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        NumberViewHolder viewHolder = new NumberViewHolder(view);

        return viewHolder;
    }




    @Override
    public void onBindViewHolder(NumberViewHolder holder, int position) {
        holder.bind(position);
    }


    @Override
    public int getItemCount() {
        return mNumberItems;
    }


    class NumberViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        ImageView itemImageView;

        public NumberViewHolder(View itemView) {

            super(itemView);
            itemImageView = (ImageView) itemView.findViewById(R.id.item_image_view);
            itemImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            itemImageView.setAdjustViewBounds(true);
            itemImageView.setOnClickListener(this);
        }


        void bind(int listIndex) {

            if (sortedMovies != null && sortedMovies.size() > 0) {

                String posterWidth = "";

                if (screenWidth < 400)
                {
                    posterWidth = "w185";
                }
                else if (screenWidth < 700)
                {
                    posterWidth = "w342";
                }
                else if (screenWidth < 1200)
                {
                    posterWidth = "w500";
                }
                else
                {
                    posterWidth = "w780";
                }

                Picasso.with(context).load("http://image.tmdb.org/t/p/" + posterWidth + sortedMovies.get(listIndex).getPosterPath()).into(itemImageView);
            }
            else
            {
                itemImageView.setImageResource(R.drawable.no_img_placeholder);
            }
        }

        @Override
        public void onClick(View view) {
            int selectedMovieId = sortedMovies.get(getAdapterPosition()).getId();

            mOnClickListener.onListItemClick(selectedMovieId);
        }
    }
}
