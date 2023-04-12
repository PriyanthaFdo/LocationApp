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
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
    private LocationManager locationManager;
    private LocationRequest locationRequest;

    private CurrentLocation currentLocation;
    private LocationService locationService;

    private Button btn_startStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("LocationApp", "MainActivity: onCreate started");

        if(KEEP_SCREEN_ON) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        currentLocation = new CurrentLocation();
        locationService = new LocationService();

        Button btn_currentLocation = findViewById(R.id.btn_currentLocation);
        btn_startStop = findViewById(R.id.btn_startStop);

        // TODO: move to services files & do something to checkGPS
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

        btn_startStop.setOnClickListener(startStopBtnOnClick);
        btn_currentLocation.setOnClickListener(currentLocationBtnOnClick);
    }

    private final View.OnClickListener startStopBtnOnClick = v -> {
        Log.d("LocationApp", "MainActivity: Location Service Btn Clicked");

        if(!isLocationServiceRunning()){
            if(checkPermissions()){
                Log.d("LocationApp", "MainActivity: start location service");
                btn_startStop.setText(R.string.stop_location_service);
                locationService.start(this, locationRequest);
            }
        } else {
            Log.d("LocationApp", "MainActivity: stop location service");
            locationService.stop(this);
            btn_startStop.setText(R.string.start_location_service);
        }
    };

    private final View.OnClickListener currentLocationBtnOnClick = v -> {
        Log.d("LocationApp", "MainActivity: Single Location Btn Clicked");
        if(checkPermissions())
            currentLocation.getCurrentLocation(this, locationRequest);
    };

    private boolean isLocationServiceRunning() {
        Log.d("LocationApp", "MainActivity: checking is service active");
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

    private boolean checkPermissions() {
        boolean isAllPermissionsGranted = false;

        Log.d("LocationApp", "MainActivity: checking permissions");

        if (isLocationAllowed()) {
            if (isGpsEnabled()) {
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

        return  isAllPermissionsGranted;
    }

    private boolean isGpsEnabled() {
        Log.d("LocationApp", "MainActivity: is GPS enabled");
        if (locationManager == null) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void turnOnGps() {
        Log.d("LocationApp", "MainActivity: Request GPS turn on");
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
        Log.d("LocationApp", "MainActivity: is location permission allowed");
        return ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        Log.d("LocationApp", "MainActivity: request location permission");
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private boolean isBackgroundLocationAllowed(){
        Log.d("LocationApp", "MainActivity: is background location permission allowed");
        return ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void requestBackgroundLocationPermission() {
        Log.d("LocationApp", "MainActivity: requesting background location permission");
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 55);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d("LocationApp", "MainActivity: permission request result received");
        checkPermissions();
    }
}

