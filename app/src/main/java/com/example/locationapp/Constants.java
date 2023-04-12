package com.example.locationapp;

public final class Constants{
    public static final int LOCATION_UPDATE_TIME_INTERVAL = 5000; //milliseconds
    public static final int LOCATION_UPDATE_DISTANCE_INTERVAL = 50; //meters
    public static final boolean KEEP_SCREEN_ON = true;
    public static final boolean CREATE_TOAST_MSG = true;

    public static final int NOTIFICATION_ID = 1;
    public static final String CHANNEL_ID = "Foreground location service";
    public static final String SERVICE_TITLE = "LocationApp Location Service";
    public static final String SERVICE_NOTIFICATION_CONTENT = "Location Service is active";
}