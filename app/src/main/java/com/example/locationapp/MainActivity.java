package com.example.locationapp;

import static customFusedLocation.Constants.CREATE_TOAST_MSG;
import static customFusedLocation.Constants.KEEP_SCREEN_ON;
import static customFusedLocation.Constants.LOGGER_TAG;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import customFusedLocation.CurrentLocation;
import customFusedLocation.LocationService;
import customFusedLocation.Permissions;


/// Created by Priyantha Fernando. Last Updated 18-04-2023

public class MainActivity extends AppCompatActivity {
  private CurrentLocation currentLocation;
  private LocationService locationService;
  private FusedLocationProviderClient fusedLocationClient;
  @SuppressLint("MissingPermission") // Permissions.CheckPermissions() provides all needed permissions
  private final View.OnClickListener currentLocationBtnOnClick = v -> {
    Log.d(LOGGER_TAG, "MainActivity: Single Location Btn Clicked");
    if (Permissions.checkPermissions(this, this)) {
      currentLocation.getCurrentLocation(fusedLocationClient, location -> {
        if (location != null) {
          double latitude = location.getLatitude();
          double longitude = location.getLongitude();
          long time = location.getTime();

          Toast.makeText(this, latitude + "," + longitude + "," + time, Toast.LENGTH_LONG).show();
        } else {
          Toast.makeText(this, "Could not get Location", Toast.LENGTH_SHORT).show();
        }
      });
    }
  };
  private Button btn_startStop;
  private final View.OnClickListener startStopBtnOnClick = v -> {
    Log.d(LOGGER_TAG, "MainActivity: Location Service Btn Clicked");

    if (!locationService.isLocationServiceRunning(this)) {
      if (startLocationService()) {
        Log.d(LOGGER_TAG, "MainActivity: started location service");
        btn_startStop.setText(R.string.stop_location_service);
      } else {
        Log.d(LOGGER_TAG, "MainActivity: could not start service");
      }
    } else {
      if (locationService.stop(this)) {
        Log.d(LOGGER_TAG, "MainActivity: stopped location service");
        btn_startStop.setText(R.string.start_location_service);
      } else {
        Log.d(LOGGER_TAG, "MainActivity: could not stop service");
      }
    }
  };

  @SuppressLint("MissingPermission")
  private boolean startLocationService(){
    boolean startSuccess = false;
    if(Permissions.checkPermissions(this, this)) {
      startSuccess = locationService.startLocationServiceLoop(this, location -> {
        if(location != null) {
          double latitude = location.getLatitude();
          double longitude = location.getLongitude();
          long time = location.getTime();

          String result = latitude +","+ longitude +","+ time;
          if(CREATE_TOAST_MSG) {
            Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
          }
          locationService.writeToFile(this, result);
        }else{
          Toast.makeText(this, "Service returned null location", Toast.LENGTH_SHORT).show();
        }
      });
    }
    return startSuccess;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Log.d(LOGGER_TAG, "MainActivity: onCreate started");

    if (KEEP_SCREEN_ON) {
      getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    currentLocation = new CurrentLocation();
    locationService = new LocationService();
    fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

    Button btn_currentLocation = findViewById(R.id.btn_currentLocation);
    btn_startStop = findViewById(R.id.btn_startStop);

    if (locationService.isLocationServiceRunning(this)) {
      btn_startStop.setText(R.string.stop_location_service);
    } else {
      btn_startStop.setText(R.string.start_location_service);
    }

    btn_startStop.setOnClickListener(startStopBtnOnClick);
    btn_currentLocation.setOnClickListener(currentLocationBtnOnClick);
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    Log.d(LOGGER_TAG, "Permissions: permission request result received");
    Permissions.checkPermissions(this, this);
  }
}

