package com.example.locationapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.Task;

import java.util.List;

/*
* Created by Priyantha by viewing Youtube video
* https://www.youtube.com/watch?v=mbQd6frpC3g
* */

public class MainActivity extends AppCompatActivity {
    private static final int LOCATION_UPDATE_TIME_INTERVAL = 5000; //milliseconds
    private static final int LOCATION_UPDATE_DISTANCE_INTERVAL = 50; //meters
    private static final boolean KEEP_SCREEN_ON = true;

    private boolean isRunning;

    private Intent intent;
    private LocationManager locationManager;
    private LocationRequest locationRequest;

    private Button btn_startStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(KEEP_SCREEN_ON) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        isRunning = false;
        btn_startStop = findViewById(R.id.btn_startStop);
        intent = new Intent(MainActivity.this, LocationService.class);

        locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                LOCATION_UPDATE_TIME_INTERVAL)
                .setMinUpdateIntervalMillis(LOCATION_UPDATE_TIME_INTERVAL)
                .setMinUpdateDistanceMeters(LOCATION_UPDATE_DISTANCE_INTERVAL).build();

        if(isLocationServiceRunning()){
            btn_startStop.setText(R.string.stop_location_service);
        }else{
            btn_startStop.setText(R.string.start_location_service);
        }

        btn_startStop.setOnClickListener(v -> {
            if(!isLocationServiceRunning()){
                checkPermissions();
//                startLocationService();
            } else {
                stopLocationService();
            }
        });
    }


    public void startLocationService(){
        btn_startStop.setText(R.string.stop_location_service);
        isRunning = true;
        startService(intent);
    }

    public void stopLocationService(){
        stopService(intent);
        isRunning = false;
        btn_startStop.setText(R.string.start_location_service);
    }

    public boolean isLocationServiceRunning() {
        ActivityManager manager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            List<ActivityManager.RunningServiceInfo> runningServices = manager.getRunningServices(Integer.MAX_VALUE);
            if (runningServices != null) {
                for (ActivityManager.RunningServiceInfo service : runningServices) {
                    if (LocationService.class.getName().equals(service.service.getClassName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    private void checkPermissions() {
        if (isLocationAllowed()) {
            if (isGpsEnabled()) {
                intent.putExtra("locationRequest", locationRequest);
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    startLocationService();
//                    startService(intent);
                } else {
                    if(isBackgroundLocationAllowed()) {
                        startLocationService();
//                        startService(intent);
                    } else {
                        requestBackgroundLocationPermission();
                    }
                }
            } else {
                turnOnGps();
            }
        } else {
            requestLocationPermission();
        }
    }

    private boolean isGpsEnabled() {
        if (locationManager == null) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
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

    private boolean isLocationAllowed() {
        return ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private boolean isBackgroundLocationAllowed(){
        return ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void requestBackgroundLocationPermission() {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 55);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        checkPermissions();
    }
}

