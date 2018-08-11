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

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by kahraman on 14.04.2018.
 */

public class CatalogAdapter extends RecyclerView.Adapter<CatalogAdapter.MovieViewHolder> {

    private ArrayList<Movie> mMovieList = new ArrayList<Movie>();
    private ItemClickListener mClickListener;
    private Context mContext;

    private int selectedPos = RecyclerView.NO_POSITION;
    MoviesViewModel moviesViewModel;
    private String mOrderType;

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(int position);
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public CatalogAdapter(Context context, MoviesViewModel moviesViewModel, ArrayList<Movie> movies, String moviesOrderType) {
        mContext = context;
        this.mMovieList = movies;
        this.moviesViewModel = moviesViewModel;
        mOrderType = moviesOrderType;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;
        // Inflate the task_layout to a view
        View view = layoutInflater.inflate(R.layout.movie_catalog_item, parent, shouldAttachToParentImmediately);

        return new MovieViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        holder.itemView.setSelected(selectedPos == position);
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return mMovieList.size();
    }

    public Movie getItemAtPosition(int position) {
        Movie movie = mMovieList.get(position);
        if(moviesViewModel.getFavoriteMovies() != null) {
            try {
                // check if current item id is in the favoriteId list or not and
                // according to it, show its star as enabled or disabled in catalog view
                if (moviesViewModel.getFavoriteMovieIds().getValue().contains(movie.get_id())) {
                    movie.setFavorite(true);
                } else {
                    movie.setFavorite(false);
                }
            } catch (NullPointerException e) {
                movie.setFavorite(false);
                e.printStackTrace();
            }
        } else {
            movie.setFavorite(false);
        }
        return movie;
    }

    /**
     * When data changes, this method updates the list of taskEntries
     * and notifies the adapter to use the new values on it
     */
    public void setMovieList(List<Movie> movieList) {
        mMovieList = (ArrayList<Movie>) movieList;
        notifyDataSetChanged();
    }

    public class MovieViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.tv_title_item) TextView tvTitleItem;
        @BindView(R.id.tv_date_item) TextView tvDateItem;
        @BindView(R.id.tv_error_message_item) TextView tvErrorMessageItem;
        @BindView(R.id.img_poster_item) ImageView imgThumbnail;
        @BindView(R.id.img_favorite_item) ImageView imgFavorite;
        @BindView(R.id.llay_item) LinearLayout llayItem;
        @BindView(R.id.pb_loading_bar_item) ProgressBar pbLoadingBarItem;
        
        public MovieViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);

            displayItemLoading();
        }

        void bind(int gridItemIndex) {
            final Movie movie = mMovieList.get(gridItemIndex);



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


            // get movie id to check whether it is the favoriteId arraylist in movieViewModel or not
            // if so show its star image as enabled, if not as disabled
            long itemMovieId = movie.get_id();

            if(moviesViewModel.getFavoriteMovies() != null) {
                try {
                    // check if current item id is in the favoriteId list or not and
                    // according to it, show its star as enabled or disabled in catalog view
                    if (moviesViewModel.getFavoriteMovieIds().getValue().contains(itemMovieId)) {
                        imgFavorite.setImageResource(R.drawable.star_enabled);
                    } else {
                        imgFavorite.setImageResource(R.drawable.star_disabled);
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    imgFavorite.setImageResource(R.drawable.star_disabled);
                }
            } else {
                imgFavorite.setImageResource(R.drawable.star_disabled);
            }

            /*if (movie.isFavorite()) {
                imgFavorite.setImageResource(R.drawable.star_enabled);
            } else {
                imgFavorite.setImageResource(R.drawable.star_disabled);
            }*/

            if(movie.get_id() < 0) {
                displayItemError();
            } else {
                displayItem();
            }
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();

            notifyItemChanged(selectedPos);
            selectedPos = getLayoutPosition();
            notifyItemChanged(selectedPos);

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
