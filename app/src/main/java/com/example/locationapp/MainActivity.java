package com.example.locationapp;

import static com.example.locationapp.Constants.*;

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

/// Created by Priyantha Fernando. Last Updated 07-03-2023

public class MainActivity extends AppCompatActivity {
    private Intent intent;
    private LocationManager locationManager;
    private LocationRequest locationRequest;

    private RequestType requestType;
    private CurrentLocation currentLocation;

    private Button btn_startStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(KEEP_SCREEN_ON) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        currentLocation = new CurrentLocation();

        Button btn_currentLocation = findViewById(R.id.btn_currentLocation);
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
            requestType = RequestType.CONTINUOUS_LOCATION;

            if(!isLocationServiceRunning()){
                checkPermissions();
            } else {
                stopLocationService();
            }
        });

        btn_currentLocation.setOnClickListener(v -> {
            requestType = RequestType.CURRENT_LOCATION;
            checkPermissions();
        });
    }


    private void startLocationService(){
        btn_startStop.setText(R.string.stop_location_service);
        startService(intent);
    }

    private void stopLocationService(){
        stopService(intent);
        btn_startStop.setText(R.string.start_location_service);
    }

    private boolean isLocationServiceRunning() {
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
        boolean isAllPermissionsGranted = false;

        if (isLocationAllowed()) {
            if (isGpsEnabled()) {
                intent.putExtra("locationRequest", locationRequest);
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    isAllPermissionsGranted = true;
                } else {
                    if(isBackgroundLocationAllowed()) {
                        isAllPermissionsGranted = true;
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

        if(isAllPermissionsGranted){
            if(requestType == RequestType.CONTINUOUS_LOCATION)
                startLocationService();
            else if(requestType == RequestType.CURRENT_LOCATION)
                currentLocation.getCurrentLocation(this, locationRequest);
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

