package com.example.locationapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.Task;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
/*
* Created by Priyantha by viewing Youtube video
* https://www.youtube.com/watch?v=mbQd6frpC3g
* */

public class MainActivity extends AppCompatActivity {
    private static final int LOCATION_UPDATE_TIME_INTERVAL = 5000; //milliseconds
    private static final int LOCATION_UPDATE_DISTANCE_INTERVAL = 50; //meters
    private static final boolean KEEP_SCREEN_ON = true;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationManager locationManager;
    private LocationRequest locationRequest;

    private Button btn_startStop;

    private boolean isRunning;
    private boolean isNewTrip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(KEEP_SCREEN_ON) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        isRunning = false;
        btn_startStop = findViewById(R.id.btn_startStop);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if(locationResult.getLocations().size() > 0){
                    int index = locationResult.getLocations().size() - 1;
                    double latitude = locationResult.getLocations().get(index).getLatitude();
                    double longitude = locationResult.getLocations().get(index).getLongitude();
                    long time =locationResult.getLocations().get(index).getTime();
                    displayLocation(latitude, longitude, time);
                }
            }
        };

        btn_startStop.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, LocationService.class);
            if(!isRunning){
                startService(i);
                startLocationService();
            } else {
                stopService(i);
                stopLocationService();
            }
            isRunning = !isRunning;
        });
    }


    public void startLocationService(){
        isNewTrip = true;
        getLocation();
        btn_startStop.setText(R.string.stop_location_service);
    }

    public void stopLocationService(){
        fusedLocationClient.removeLocationUpdates(locationCallback);
        writeToFile("\n\n");
        btn_startStop.setText(R.string.start_location_service);
    }

    private void getLocation() {
        if (isLocationPermissionAllowed()) {
            if (isGpsEnabled()) {
                getCurrentLocation();
            } else {
                turnOnGps();
            }
        } else {
            requestLocationPermission();
        }
    }

    private void turnOnGps() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(getApplicationContext())
                .checkLocationSettings(builder.build());

        result.addOnCompleteListener(task -> {
            try {
                LocationSettingsResponse response = task.getResult(ApiException.class);
                Toast.makeText(MainActivity.this, "GPS is already turned on", Toast.LENGTH_SHORT).show();
            } catch (ApiException e) {
                switch (e.getStatusCode()) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                            resolvableApiException.startResolutionForResult(MainActivity.this, 2);
                        } catch (IntentSender.SendIntentException ex) {
                            ex.printStackTrace();
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        //Device does not have location
                        break;
                }
            }
        });
    }

    private boolean isGpsEnabled() {
        if (locationManager == null) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private boolean isLocationPermissionAllowed() {
        return ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        getLocation();
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                LOCATION_UPDATE_TIME_INTERVAL)
                .setMinUpdateIntervalMillis(LOCATION_UPDATE_TIME_INTERVAL)
                .setMinUpdateDistanceMeters(LOCATION_UPDATE_DISTANCE_INTERVAL).build();

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void displayLocation(double latitude, double longitude, long time) {
        String result = latitude +","+ longitude +","+ time;
        Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
        writeToFile(result);
    }

    private void writeToFile(String locationData){
        File internalStorageDir = getFilesDir();
        File file = new File(internalStorageDir, "coordinates.txt");
        try{
            FileWriter writer = new FileWriter(file, true);
            if(!isNewTrip){
                locationData = "|" + locationData;
            }
            writer.write(locationData);
            isNewTrip = false;
            writer.close();
        }catch (IOException e){
            Toast.makeText(MainActivity.this, "Error: "+ e, Toast.LENGTH_LONG).show();
        }
    }
}

