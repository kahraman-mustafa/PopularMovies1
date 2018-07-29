package com.mustafakahraman.popularmovies1.helper;

import android.util.Log;

public class Connection {

    private int type;
    public static final int NoData = 0;
    public static final int WifiData = 1;
    public static final int MobileData = 2;

    private boolean isConnected;
    private boolean isInternetAvailable;

    public Connection(int type, boolean isConnected, boolean isInternetAvailable) {
        this.type = type;
        this.isConnected = isConnected;
        this.isInternetAvailable = isInternetAvailable;
        Log.d("Connection", "Connection object created - isConnected: " + isConnected + ", " +
                "\nisInternetAvailable: " + isInternetAvailable + ", type: " + type);
    }

    public boolean getIsConnected() {
        return isConnected;
    }

    public int getType() {
        return type;
    }

    public boolean getIsInternetAvailable() {
        return isInternetAvailable;
    }
}
