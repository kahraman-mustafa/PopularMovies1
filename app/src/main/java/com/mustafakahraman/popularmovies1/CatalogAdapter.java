package com.mustafakahraman.popularmovies1;

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

import com.mustafakahraman.popularmovies1.helper.NetworkUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by kahraman on 14.04.2018.
 */

public class CatalogAdapter extends RecyclerView.Adapter<CatalogAdapter.MovieViewHolder> {

    private ArrayList<Movie> mMovieList = new ArrayList<Movie>();
    private ItemClickListener mClickListener;
    private Context mContext;

    public CatalogAdapter(Context context, ArrayList<Movie> movies) {
        mContext = context;
        this.mMovieList = movies;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdGridItem = R.layout.item_catalog;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View gridItemView = inflater.inflate(layoutIdGridItem, parent, shouldAttachToParentImmediately);
        MovieViewHolder viewHolder = new MovieViewHolder(gridItemView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return mMovieList.size();
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public class MovieViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        /*
        @BindView (R.id.tv_title_item) TextView tvTitle;
        @BindView (R.id.tv_date_item) TextView tvDate;
        @BindView (R.id.img_thumbnail) ImageView imgThumbnail;
        @BindView (R.id.img_favorite) ImageView imgFavorite;
        @BindView (R.id.llay_item) LinearLayout llayItem;
        @BindView (R.id.pb_loading_bar_item) ProgressBar pbLoadingBar;
        @BindView (R.id.tv_error_message_item) TextView tvError;
        */

        TextView tvTitle, tvDate, tvError;
        ImageView imgThumbnail, imgFavorite;
        LinearLayout llayItem;
        ProgressBar pbLoadingBar;

        public MovieViewHolder(View gridItemView) {
            super(gridItemView);

            tvTitle = gridItemView.findViewById(R.id.tv_title_item);
            tvDate = gridItemView.findViewById(R.id.tv_date_item);
            imgThumbnail = gridItemView.findViewById(R.id.img_thumbnail);
            imgFavorite = gridItemView.findViewById(R.id.img_favorite);
            llayItem = gridItemView.findViewById(R.id.llay_item);
            pbLoadingBar = gridItemView.findViewById(R.id.pb_loading_bar_item);
            tvError = gridItemView.findViewById(R.id.tv_error_message_item);
            //ButterKnife.bind(mContext, gridItemView);
            displayItemLoading();

            gridItemView.setOnClickListener(this);
        }

        void bind(int gridItemIndex) {
            Movie movie = mMovieList.get(gridItemIndex);

            tvTitle.setText(movie.getTitle());
            String dateAndVoteAvg = movie.getVoteAvg() + "/10"
                                    + " - "
                                    + movie.getDate().substring(0,4);
            tvDate.setText(dateAndVoteAvg);
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

            if (mClickListener != null) mClickListener.onItemClick(view, position);
        }

        private void displayItemLoading() {
            llayItem.setVisibility(View.INVISIBLE);
            tvError.setVisibility(View.INVISIBLE);
            pbLoadingBar.setVisibility(View.VISIBLE);
        }

        private void displayItem() {
            llayItem.setVisibility(View.VISIBLE);
            tvError.setVisibility(View.INVISIBLE);
            pbLoadingBar.setVisibility(View.INVISIBLE);
        }

        private void displayItemError() {
            llayItem.setVisibility(View.INVISIBLE);
            tvError.setVisibility(View.VISIBLE);
            pbLoadingBar.setVisibility(View.INVISIBLE);
        }
    }

    // convenience method for getting data at click position
    /*String getItem(int position) {
        Movie movie = mMovieList.get(position);
        return movie.getTitle() + "\n" + movie.getDate();
    }*/
}
