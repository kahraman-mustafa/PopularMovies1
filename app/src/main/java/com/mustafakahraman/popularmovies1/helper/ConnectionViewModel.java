package com.mustafakahraman.popularmovies1.helper;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

public class ConnectionViewModel extends AndroidViewModel {

    private ConnectionLiveData connectionLiveData;


    public ConnectionViewModel(@NonNull Application application) {
        super(application);
        connectionLiveData = new ConnectionLiveData(application.getApplicationContext());

    }

    public ConnectionLiveData getConnectionLiveData() {
        return connectionLiveData;
    }


}
