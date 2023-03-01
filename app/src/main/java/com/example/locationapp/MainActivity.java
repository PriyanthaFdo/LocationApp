package com.example.locationapp;

import androidx.annotation.NonNull;
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
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.CancellationTokenSource;

public class MainActivity extends AppCompatActivity {
    FusedLocationProviderClient fusedLocationProviderClient;
    CancellationTokenSource cancellationTokenSource;
    CancellationToken cancellationToken;

    boolean isRunning;

    Button btn_getLocation;
    TextView txt_locationResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_getLocation = findViewById(R.id.btn_getLocation);
        txt_locationResult = findViewById(R.id.txt_locationResult);

        isRunning = false;

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        btn_getLocation.setOnClickListener(v -> {
            if(!isRunning) {
                cancellationTokenSource = new CancellationTokenSource();
                cancellationToken = cancellationTokenSource.getToken();
                isRunning = true;
                getLocation();
            }
        });
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        getLocation();
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Log.d("getCurrentLocation", "new Sdk");
            fusedLocationProviderClient.getCurrentLocation(LocationRequest.QUALITY_BALANCED_POWER_ACCURACY, cancellationToken)
                    .addOnSuccessListener(this::displayLocation);
        } else {
            Log.d("getCurrentLocation", "old Sdk");
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(this::displayLocation);
        }
    }

    private void displayLocation(Location location) {
        cancellationTokenSource = new CancellationTokenSource();
        if(location != null){
            double longitude = location.getLongitude();
            double latitude = location.getLatitude();

            String currentText = txt_locationResult.getText().toString();
            String newText = getResources().getString(R.string.location, latitude, longitude);
            txt_locationResult.setText(getResources().getString(R.string.multiLine, newText, currentText));
            cancellationTokenSource.cancel();

        } else {
            txt_locationResult.setText(R.string.errorRetrievingLocation);
            new Handler().postDelayed(() -> txt_locationResult.setText(""), 3000);
        }
        isRunning = false;
    }
}

