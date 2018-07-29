package com.mustafakahraman.popularmovies1.helper;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class ItemOffsetDecoration extends RecyclerView.ItemDecoration {

    private int mGridSpacingPx;
    private int mGridColumns;
    private boolean mHasOutFrameSpace;

    private boolean mNeedLeftSpacing = false;

    public ItemOffsetDecoration(int gridSpacingPx, int gridColumns, boolean hasOutFrameSpace) {
        mGridSpacingPx = gridSpacingPx;
        mGridColumns = gridColumns;
        mHasOutFrameSpace = hasOutFrameSpace;
    }


    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        int itemPosition = ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewAdapterPosition();

        if(mHasOutFrameSpace) {
            outRect.left = mGridSpacingPx;
            outRect.right = mGridSpacingPx;
            outRect.bottom = mGridSpacingPx;

            // Add top margin only for the first item to avoid double space between items
            if (itemPosition < mGridColumns) {
                outRect.top = mGridSpacingPx;
            } else {
                outRect.top = 0;
            }

            if (itemPosition % mGridColumns == 0) {
                outRect.left = 0;
                outRect.right = mGridSpacingPx / 2;
            } else if ((itemPosition + 1) % mGridColumns == 0) {
                outRect.right = 0;
                outRect.left = mGridSpacingPx / 2;
            } else {
                outRect.right = mGridSpacingPx / 2;
                outRect.left = mGridSpacingPx / 2;
            }

        } else {
            // This block is implemented when the outer frame space is not desired
            int frameWidth = (int) ((parent.getWidth() - (float) mGridSpacingPx * (mGridColumns - 1)) / mGridColumns);
            int padding = parent.getWidth() / mGridColumns - frameWidth;
            if (itemPosition < mGridColumns) {
                outRect.top = 0;
            } else {
                outRect.top = mGridSpacingPx;
            }
            if (itemPosition % mGridColumns == 0) {
                outRect.left = 0;
                outRect.right = padding;
                mNeedLeftSpacing = true;
            } else if ((itemPosition + 1) % mGridColumns == 0) {
                mNeedLeftSpacing = false;
                outRect.right = 0;
                outRect.left = padding;
            } else if (mNeedLeftSpacing) {
                mNeedLeftSpacing = false;
                outRect.left = mGridSpacingPx - padding;
                if ((itemPosition + 2) % mGridColumns == 0) {
                    outRect.right = mGridSpacingPx - padding;
                } else {
                    outRect.right = mGridSpacingPx / 2;
                }
            } else if ((itemPosition + 2) % mGridColumns == 0) {
                mNeedLeftSpacing = false;
                outRect.left = mGridSpacingPx / 2;
                outRect.right = mGridSpacingPx - padding;
            } else {
                mNeedLeftSpacing = false;
                outRect.left = mGridSpacingPx / 2;
                outRect.right = mGridSpacingPx / 2;
            }
            outRect.bottom = 0;
        }

    }

}