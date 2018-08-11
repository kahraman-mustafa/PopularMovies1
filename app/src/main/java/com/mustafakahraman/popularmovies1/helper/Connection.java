package com.mustafakahraman.popularmovies1.helper;

import android.util.Log;

public class Connection {

    private int type;
    public static final int NoData = 0;
    public static final int WifiData = 1;
    public static final int MobileData = 2;

    private boolean isConnected;

    public Connection(int type, boolean isConnected) {
        this.type = type;
        this.isConnected = isConnected;
    }

    public boolean getIsConnected() {
        return isConnected;
    }

    public int getType() {
        return type;
    }
}
