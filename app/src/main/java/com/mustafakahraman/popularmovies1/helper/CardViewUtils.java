package com.mustafakahraman.popularmovies1.helper;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.TextView;

public class CardViewUtils {

    private static CardView mCardView;
    private static TextView mTvCollapser;
    private static int minHeight;

    public static void makeCardViewsCollapsable(Context context, CardView cardView, TextView tvCollapser) {
        // Now we will step into RecylerView adapter which is holding your cards, and you can start coding inside ViewHolder of a card,
        mCardView = cardView;
        mTvCollapser = tvCollapser;

        // 1. Lets first measure a height of the screen, You can give custom height if you don't wanna expand it to full screen.
        WindowManager windowmanager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dimension = context.getResources().getDisplayMetrics();
        windowmanager.getDefaultDisplay().getMetrics(dimension);
        final int height = dimension.heightPixels;

        // 2. Lets assign a minimum height to cardView.

        mCardView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

            @Override
            public boolean onPreDraw() {
                mCardView.getViewTreeObserver().removeOnPreDrawListener(this);
                minHeight = mCardView.getHeight();
                ViewGroup.LayoutParams layoutParams = mCardView.getLayoutParams();
                layoutParams.height = minHeight;
                mCardView.setLayoutParams(layoutParams);

                return true;
            }
        });

        // 3. Now lets add an expand & collapse functionality on a button.

        mTvCollapser.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                toggleCardViewnHeight(height);

            }
        });
    }

    private static void toggleCardViewnHeight(int height) {

        if (mCardView.getHeight() == minHeight) {
            // expand

            expandView(height); //'height' is the height of screen which we have measured already.

        } else {
            // collapse
            collapseView();

        }
    }

    // 4. The magic code which will make our cardview expand and collapse.

    public static void collapseView() {

        ValueAnimator anim = ValueAnimator.ofInt(mCardView.getMeasuredHeightAndState(),
                minHeight);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = mCardView.getLayoutParams();
                layoutParams.height = val;
                mCardView.setLayoutParams(layoutParams);

            }
        });
        anim.start();
    }

    public static void expandView(int height) {

        ValueAnimator anim = ValueAnimator.ofInt(mCardView.getMeasuredHeightAndState(),
                height);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = mCardView.getLayoutParams();
                layoutParams.height = val;
                mCardView.setLayoutParams(layoutParams);
            }
        });
        anim.start();
    }

}
