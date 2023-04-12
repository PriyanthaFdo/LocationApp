package com.example.locationapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class CurrentLocation {
    FusedLocationProviderClient fusedLocationClient;
    LocationCallback locationCallback;
    private LocationRequest locationRequest;

    public void getCurrentLocation(Context context, LocationRequest locationRequest) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult.getLocations().size() > 0) {
                    int index = locationResult.getLocations().size() - 1;
                    double latitude = locationResult.getLocations().get(index).getLatitude();
                    double longitude = locationResult.getLocations().get(index).getLongitude();
                    long time = locationResult.getLocations().get(index).getTime();
                    displayCurrentLocation(context, latitude, longitude, time);
                    fusedLocationClient.removeLocationUpdates(locationCallback);
                }
            }
        };
        this.locationRequest = locationRequest;
        startGetLocationLoop();
    }

    private void displayCurrentLocation(Context context, double latitude, double longitude, long time) {
        Toast.makeText(context, latitude +","+ longitude +","+ time, Toast.LENGTH_LONG).show();
    }

    @SuppressLint("MissingPermission")
    private void startGetLocationLoop() {
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }
}
