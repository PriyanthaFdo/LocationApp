package com.example.locationapp;

import static com.example.locationapp.Constants.*;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class LocationService extends Service {
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;

    private boolean placePipeCharacter;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

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
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground location service",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification() {
        NotificationCompat.Builder builder = new NotificationCompat
                .Builder(this, CHANNEL_ID)
                .setContentTitle(SERVICE_TITLE)
                .setContentText(SERVICE_NOTIFICATION_CONTENT)
                .setSmallIcon(R.drawable.baseline_my_location_24);
        return builder.build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        locationRequest = intent.getParcelableExtra("locationRequest");
        Notification notification = createNotification();
        startForeground(NOTIFICATION_ID, notification);

        placePipeCharacter = false;
        startGetLocationLoop();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        fusedLocationClient.removeLocationUpdates(locationCallback);
        placePipeCharacter = false;
        writeToFile("\n\n");
        stopForeground(true);
    }

    @SuppressLint("MissingPermission")
    private void startGetLocationLoop() {
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void displayLocation(double latitude, double longitude, long time) {
        String result = latitude +","+ longitude +","+ time;
        if(CREATE_TOAST_MSG) {
            Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
        }
        writeToFile(result);
    }

    private void writeToFile(String locationData){
        File internalStorageDir = getFilesDir();
        File file = new File(internalStorageDir, "coordinates.txt");
        try{
            FileWriter writer = new FileWriter(file, true);
            if(placePipeCharacter){
                locationData = "|" + locationData;
            }
            writer.write(locationData);
            placePipeCharacter = true;
            writer.close();
        }catch (IOException e){
            Toast.makeText(this, "Error: "+ e, Toast.LENGTH_LONG).show();
        }
    }
}
