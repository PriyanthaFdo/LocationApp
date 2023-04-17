package CustomFusedLocation;

import static CustomFusedLocation.Constants.*;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.locationapp.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class LocationService extends Service {
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
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

    public boolean start(Activity activity, Context context){
        if(Permissions.checkPermissions(activity, context)) {
            Log.d("LocationApp", "LocationService: start called");
            Intent intent = new Intent(context, LocationService.class);
            context.startService(intent);
            return  true;
        }else{
            return false;
        }
    }

    public boolean stop(Context context){
        Log.d("LocationApp", "LocationService: stop called");
        Intent intent = new Intent(context, LocationService.class);
        return context.stopService(intent);
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
        Log.d("LocationApp", "LocationService: starting location service");
        Notification notification = createNotification();
        startForeground(NOTIFICATION_ID, notification);

        placePipeCharacter = false;
        startGetLocationLoop();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d("LocationApp", "LocationService: stopping location service");
        super.onDestroy();
        fusedLocationClient.removeLocationUpdates(locationCallback);
        placePipeCharacter = false;
        writeToFile("\n\n");
        stopForeground(true);
    }

    @SuppressLint("MissingPermission")
    private void startGetLocationLoop() {
        Log.d("LocationApp", "LocationService: Location loop started");
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    public boolean isLocationServiceRunning(Context context) {
        Log.d("LocationApp", "LocationService: checking is service active");
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
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
