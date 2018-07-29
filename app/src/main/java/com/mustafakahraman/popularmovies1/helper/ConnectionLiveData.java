package com.mustafakahraman.popularmovies1.helper;

import android.arch.lifecycle.LiveData;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import static com.mustafakahraman.popularmovies1.helper.Connection.MobileData;
import static com.mustafakahraman.popularmovies1.helper.Connection.NoData;
import static com.mustafakahraman.popularmovies1.helper.Connection.WifiData;

public class ConnectionLiveData extends LiveData<Connection> {

    private Context context;

    public ConnectionLiveData(Context context) {
        this.context = context;
    }

    @Override
    protected void onActive() {
        super.onActive();
        IntentFilter filter = new    IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(networkReceiver, filter);
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        context.unregisterReceiver(networkReceiver);
    }

    private BroadcastReceiver networkReceiver = new BroadcastReceiver() {
        @SuppressWarnings("deprecation")
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getExtras()!=null) {
                NetworkInfo activeNetwork = (NetworkInfo) intent.getExtras().get(ConnectivityManager.EXTRA_NETWORK_INFO);
                boolean isConnected = activeNetwork != null &&
                        activeNetwork.isConnectedOrConnecting();
                if(isConnected) {

                    boolean isInternetAvailable = NetworkUtils.getIsInternetAvailable();

                    switch (activeNetwork.getType()){
                        case ConnectivityManager.TYPE_WIFI:
                            if(isInternetAvailable) {
                                postValue(new Connection(WifiData, true, true));
                            } else {
                                postValue(new Connection(WifiData, true, false));
                            }
                            break;
                        case ConnectivityManager.TYPE_MOBILE:
                            if(isInternetAvailable) {
                                postValue(new Connection(MobileData, true, true));
                            } else {
                                postValue(new Connection(MobileData, true, false));
                            }
                            break;
                    }
                } else {
                    postValue(new Connection(NoData,false, false));
                }
            }
        }
    };

}
