package com.mustafakahraman.popularmovies1.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mustafakahraman.popularmovies1.R;
import com.mustafakahraman.popularmovies1.model.Review;
import com.mustafakahraman.popularmovies1.model.Video;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by kahraman on 14.04.2018.
 */

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    private ArrayList<Video> mVideoList = new ArrayList<Video>();
    private ItemClickListener mClickListener;

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(int position);
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public VideoAdapter(ArrayList<Video> videos) {
        this.mVideoList = videos;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;
        // Inflate the task_layout to a view
        View view = layoutInflater.inflate(R.layout.video_item, parent, shouldAttachToParentImmediately);

        return new VideoViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return mVideoList.size();
    }

    public Video getItemAtPosition(int position) {
        Video video = mVideoList.get(position);
        return video;
    }

    /**
     * When data changes, this method updates the list of taskEntries
     * and notifies the adapter to use the new values on it
     */
    public void setVideoList(List<Video> videoList) {
        mVideoList = (ArrayList<Video>) videoList;
        notifyDataSetChanged();
    }

    public class VideoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.tv_video_name) TextView tvVideoName;
        @BindView(R.id.tv_video_type) TextView tvVideoType;
        
        public VideoViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);
        }

        void bind(int gridItemIndex) {
            Video video = mVideoList.get(gridItemIndex);

            tvVideoName.setText(video.getName());
            tvVideoType.setText(video.getType());
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if (mClickListener != null) mClickListener.onItemClick(position);
        }
    }
}
