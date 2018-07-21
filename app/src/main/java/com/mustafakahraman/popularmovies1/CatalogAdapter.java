package com.mustafakahraman.popularmovies1;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mustafakahraman.popularmovies1.databinding.ItemCatalogBinding;
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

    private LayoutInflater layoutInflater;

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public CatalogAdapter(Context context, ArrayList<Movie> movies) {
        mContext = context;
        this.mMovieList = movies;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        layoutInflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        if (layoutInflater == null) {
            layoutInflater = LayoutInflater.from(parent.getContext());
        }

        ItemCatalogBinding itemCatalogBinding =
                DataBindingUtil.inflate(layoutInflater, R.layout.item_catalog, parent, shouldAttachToParentImmediately);

        return new MovieViewHolder(itemCatalogBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return mMovieList.size();
    }


    public class MovieViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private final ItemCatalogBinding mBinding;

        public MovieViewHolder(final ItemCatalogBinding itemCatalogBinding) {
            super(itemCatalogBinding.getRoot());

            mBinding = itemCatalogBinding;
            mBinding.itemCatalog.setOnClickListener(this);

            displayItemLoading();
        }

        void bind(int gridItemIndex) {
            Movie movie = mMovieList.get(gridItemIndex);

            mBinding.tvTitleItem.setText(movie.getTitle());
            String dateAndVoteAvg = movie.getVoteAvg() + "/10"
                                    + " - "
                                    + movie.getDate().substring(0,4);
            mBinding.tvDateItem.setText(dateAndVoteAvg);
            Picasso.with(mContext)
                    .load(NetworkUtils.buildPosterUrl(
                            mContext,
                            mContext.getString(R.string.POSTER_SIZE_W185),
                            movie.getPosterUrl()))
                    .into(mBinding.imgThumbnail);
            mBinding.imgFavorite.setImageResource(
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
            mBinding.llayItem.setVisibility(View.INVISIBLE);
            mBinding.tvErrorMessageItem.setVisibility(View.INVISIBLE);
            mBinding.pbLoadingBarItem.setVisibility(View.VISIBLE);
        }

        private void displayItem() {
            mBinding.llayItem.setVisibility(View.VISIBLE);
            mBinding.tvErrorMessageItem.setVisibility(View.INVISIBLE);
            mBinding.pbLoadingBarItem.setVisibility(View.INVISIBLE);
        }

        private void displayItemError() {
            mBinding.llayItem.setVisibility(View.INVISIBLE);
            mBinding.tvErrorMessageItem.setVisibility(View.VISIBLE);
            mBinding.pbLoadingBarItem.setVisibility(View.INVISIBLE);
        }
    }
}
