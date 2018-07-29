package com.mustafakahraman.popularmovies1.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mustafakahraman.popularmovies1.R;
import com.mustafakahraman.popularmovies1.data.DateConverter;
import com.mustafakahraman.popularmovies1.helper.NetworkUtils;
import com.mustafakahraman.popularmovies1.model.Movie;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kahraman on 14.04.2018.
 */

public class CatalogAdapter extends RecyclerView.Adapter<CatalogAdapter.MovieViewHolder> {

    private ArrayList<Movie> mMovieList = new ArrayList<Movie>();
    private ItemClickListener mClickListener;
    private Context mContext;


    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(int position);
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public CatalogAdapter(Context context, ArrayList<Movie> movies) {
        mContext = context;
        this.mMovieList = movies;
    }

    /*@NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        if (layoutInflater == null) {
            layoutInflater = LayoutInflater.from(parent.getContext());
        }

        ItemCatalogBinding itemCatalogBinding =
                DataBindingUtil.inflate(layoutInflater, R.layout.item_catalog, parent, shouldAttachToParentImmediately);

        return new MovieViewHolder(itemCatalogBinding);
    }*/

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;
        // Inflate the task_layout to a view
        View view = layoutInflater.inflate(R.layout.item_catalog, parent, shouldAttachToParentImmediately);

        return new MovieViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return mMovieList.size();
    }

    public Movie getItemAtPosition(int position) {
        return mMovieList.get(position);
    }

    /**
     * When data changes, this method updates the list of taskEntries
     * and notifies the adapter to use the new values on it
     */
    public void setMovieList(List<Movie> movieList) {
        mMovieList = (ArrayList<Movie>) movieList;
        notifyDataSetChanged();
    }

    public class MovieViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        //private final ItemCatalogBinding mBinding;

        TextView tvTitleItem, tvDateItem, tvErrorMessageItem;
        ImageView imgThumbnail, imgFavorite;
        LinearLayout llayItem;
        ProgressBar pbLoadingBarItem;

        /*public MovieViewHolder(final ItemCatalogBinding itemCatalogBinding) {
            super(itemCatalogBinding.getRoot());

            mBinding = itemCatalogBinding;
            itemCatalog.setOnClickListener(this);

            displayItemLoading();
        }*/
        
        public MovieViewHolder(View itemView) {
            super(itemView);
            
            initializeItemViews(itemView);
            itemView.setOnClickListener(this);

            displayItemLoading();
        }
        
        private void initializeItemViews(View itemView) {
            tvTitleItem = (TextView) itemView.findViewById(R.id.tv_title_item);
            tvDateItem = (TextView) itemView.findViewById(R.id.tv_date_item);
            tvErrorMessageItem = (TextView) itemView.findViewById(R.id.tv_error_message_item);
            imgThumbnail = (ImageView) itemView.findViewById(R.id.img_thumbnail);
            imgFavorite = (ImageView) itemView.findViewById(R.id.img_favorite);
            llayItem = (LinearLayout) itemView.findViewById(R.id.llay_item);
            pbLoadingBarItem = (ProgressBar) itemView.findViewById(R.id.pb_loading_bar_item);
        }

        void bind(int gridItemIndex) {
            Movie movie = mMovieList.get(gridItemIndex);

            tvTitleItem.setText(movie.getTitle());
            String dateAndVoteAvg = movie.getVoteAvg() + "/10"
                                    + " - "
                                    + DateConverter.toStringDate(movie.getDate()).substring(0,4);
            tvDateItem.setText(dateAndVoteAvg);
            Picasso.with(mContext)
                    .load(NetworkUtils.buildPosterUrl(
                            mContext,
                            mContext.getString(R.string.POSTER_SIZE_W185),
                            movie.getPosterUrl()))
                    .into(imgThumbnail);
            imgFavorite.setImageResource(
                    movie.isFavorite() ? R.drawable.star_enabled : R.drawable.star_disabled);

            if(movie.get_id() < 0) {
                displayItemError();
            } else {
                displayItem();
            }
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();

            if (mClickListener != null) mClickListener.onItemClick(position);
        }

        private void displayItemLoading() {
            llayItem.setVisibility(View.INVISIBLE);
            tvErrorMessageItem.setVisibility(View.INVISIBLE);
            pbLoadingBarItem.setVisibility(View.VISIBLE);
        }

        private void displayItem() {
            llayItem.setVisibility(View.VISIBLE);
            tvErrorMessageItem.setVisibility(View.INVISIBLE);
            pbLoadingBarItem.setVisibility(View.INVISIBLE);
        }

        private void displayItemError() {
            llayItem.setVisibility(View.INVISIBLE);
            tvErrorMessageItem.setVisibility(View.VISIBLE);
            pbLoadingBarItem.setVisibility(View.INVISIBLE);
        }
    }
}
