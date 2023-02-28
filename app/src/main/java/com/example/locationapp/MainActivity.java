package com.example.locationapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationRequest;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.CancellationTokenSource;

public class MainActivity extends AppCompatActivity {
    FusedLocationProviderClient fusedLocationProviderClient;
    CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();
    CancellationToken cancellationToken = cancellationTokenSource.getToken();

    Button btn_getLocation;
    TextView txt_locationResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_getLocation = findViewById(R.id.btn_getLocation);
        txt_locationResult = findViewById(R.id.txt_locationResult);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        btn_getLocation.setOnClickListener(v -> getLocation());
    }

    private void getLocation(){
        if(isLocationPermissionAllowed()){
            getCurrentLocation();
        } else {
            requestLocationPermission();
        }
    }

    private boolean isLocationPermissionAllowed() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            fusedLocationProviderClient.getCurrentLocation(LocationRequest.QUALITY_BALANCED_POWER_ACCURACY, cancellationToken)
                    .addOnSuccessListener(this::displayLocation);
        } else {
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(this::displayLocation);
        }
    }

    private void displayLocation(Location location) {
        if(location != null){
            double longitude = location.getLongitude();
            double latitude = location.getLatitude();

            txt_locationResult.setText(getResources().getString(R.string.location, latitude, longitude));
            cancellationTokenSource.cancel();

            new Handler().postDelayed(() -> txt_locationResult.setText(""), 3000);
        } else {
            txt_locationResult.setText(R.string.errorRetrievingLocation);
        }
    }
}

