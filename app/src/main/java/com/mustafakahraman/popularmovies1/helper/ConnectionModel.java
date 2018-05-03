package com.mustafakahraman.popularmovies1.helper;

public class ConnectionModel {

    private int type;
    public static final int WifiData = 1;
    public static final int MobileData = 2;

    private boolean isConnected;

    public ConnectionModel(int type, boolean isConnected) {
        this.type = type;
        this.isConnected = isConnected;
    }

    public int getType() {
        return type;
    }

    public boolean getIsConnected() {
        return isConnected;
    }

}
