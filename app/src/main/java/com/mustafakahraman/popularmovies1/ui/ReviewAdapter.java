package com.mustafakahraman.popularmovies1.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mustafakahraman.popularmovies1.R;
import com.mustafakahraman.popularmovies1.data.DateConverter;
import com.mustafakahraman.popularmovies1.helper.NetworkUtils;
import com.mustafakahraman.popularmovies1.model.Movie;
import com.mustafakahraman.popularmovies1.model.Review;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by kahraman on 14.04.2018.
 */

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private ArrayList<Review> mReviewList = new ArrayList<Review>();
    private ItemClickListener mClickListener;

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(int position);
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public ReviewAdapter(ArrayList<Review> reviews) {
        this.mReviewList = reviews;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;
        // Inflate the task_layout to a view
        View view = layoutInflater.inflate(R.layout.review_item, parent, shouldAttachToParentImmediately);

        return new ReviewViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return mReviewList.size();
    }

    public Review getItemAtPosition(int position) {
        Review review = mReviewList.get(position);
        return review;
    }

    /**
     * When data changes, this method updates the list of taskEntries
     * and notifies the adapter to use the new values on it
     */
    public void setReviewList(List<Review> reviewList) {
        mReviewList = (ArrayList<Review>) reviewList;
        notifyDataSetChanged();
    }

    public class ReviewViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.tv_review_author) TextView tvReviewAuthor;
        @BindView(R.id.tv_review_content) TextView tvReviewContent;
        @BindView(R.id.tv_review_url) TextView tvReviewUrl;
        
        public ReviewViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);
        }

        void bind(int gridItemIndex) {
            Review review = mReviewList.get(gridItemIndex);

            tvReviewAuthor.setText(review.getAuthor());
            tvReviewContent.setText(review.getContent());
            tvReviewUrl.setText(review.getUrl());
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();

            if (mClickListener != null) mClickListener.onItemClick(position);
        }
    }
}
