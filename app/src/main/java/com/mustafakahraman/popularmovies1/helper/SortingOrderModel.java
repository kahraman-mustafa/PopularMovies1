package com.mustafakahraman.popularmovies1.helper;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.mustafakahraman.popularmovies1.helper.NetworkUtils;

public class SortingOrderModel extends AndroidViewModel {

    private MutableLiveData<String> mSortOder;

    public SortingOrderModel(Application application) {
        super(application);
        mSortOder = new MutableLiveData<String>();
    }

    public MutableLiveData<String> getSortOder() {
        return mSortOder;
    }

}
