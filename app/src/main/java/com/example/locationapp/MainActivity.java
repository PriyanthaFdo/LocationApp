package com.example.locationapp;

import static CustomFusedLocation.Constants.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import CustomFusedLocation.CurrentLocation;
import CustomFusedLocation.LocationService;
import CustomFusedLocation.Permissions;


/// Created by Priyantha Fernando. Last Updated 17-04-2023

public class MainActivity extends AppCompatActivity {
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

        if(locationService.isLocationServiceRunning(this)){
            btn_startStop.setText(R.string.stop_location_service);
        }else{
            btn_startStop.setText(R.string.start_location_service);
        }

        btn_startStop.setOnClickListener(startStopBtnOnClick);
        btn_currentLocation.setOnClickListener(currentLocationBtnOnClick);
    }

    private final View.OnClickListener startStopBtnOnClick = v -> {
        Log.d("LocationApp", "MainActivity: Location Service Btn Clicked");

        if(!locationService.isLocationServiceRunning(this)){
            if(locationService.start(this,this)) {
                Log.d("LocationApp", "MainActivity: started location service");
                btn_startStop.setText(R.string.stop_location_service);
            }else{
                Log.d("LocationApp", "MainActivity: could not start service");
            }
        } else {
            if(locationService.stop(this)){
                Log.d("LocationApp", "MainActivity: stopped location service");
                btn_startStop.setText(R.string.start_location_service);
            }else{
                Log.d("LocationApp", "MainActivity: could not stop service");
            }
        }
    };

    private final View.OnClickListener currentLocationBtnOnClick = v -> {
        Log.d("LocationApp", "MainActivity: Single Location Btn Clicked");
        currentLocation.getCurrentLocation(this, this);
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d("LocationApp", "Permissions: permission request result received");
        Permissions.checkPermissions(this, this);
    }
}

