package com.mustafakahraman.popularmovies1.ui;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

public class SortingOrderModel extends ViewModel {

    private static final String ORDER_BY_POPULARITY = "popular";
    private static final String ORDER_BY_TOPRATED = "top_rated";
    private static final String ORDER_BY_FAVORITE = "favorite";

    private MutableLiveData<String> mSortOder;

    public SortingOrderModel() {

    }

    public MutableLiveData<String> getSortOder() {
        if (mSortOder == null) {
            mSortOder = new MutableLiveData<String>();
        }
        return mSortOder;
    }

}
