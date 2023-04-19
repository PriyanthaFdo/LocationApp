package customFusedLocation;

import static customFusedLocation.Constants.*;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
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
    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if(locationResult.getLocations().size() > 0){
                    LocationListenerHolder.getLocationListener().onLocationReceived(locationResult.getLastLocation());
                }
            }
        };
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    public boolean startLocationServiceLoop(Context context, LocationListener listener){
        Log.d(LOGGER_TAG, "LocationService: start called");
        Intent intent = new Intent(context, LocationService.class);
        LocationListenerHolder.setLocationListener(listener);
        placePipeCharacter = false;
        ComponentName isStarted = context.startService(intent);
        return isStarted != null;
    }

    public boolean stop(Context context){
        Log.d(LOGGER_TAG, "LocationService: stop called");

        placePipeCharacter = false;
        writeToFile(context,"\n\n");

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
    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    // startGetLocationLoop() (Custom method) needs location permission
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOGGER_TAG, "LocationService: starting location service");
        Notification notification = createNotification();
        startForeground(NOTIFICATION_ID, notification);

        startGetLocationLoop();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(LOGGER_TAG, "LocationService: stopping location service");
        super.onDestroy();
        fusedLocationClient.removeLocationUpdates(locationCallback);
        stopForeground(true);
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    //FusedLocationProviderClient.requestLocationUpdates need location permission
    private void startGetLocationLoop() {
        Log.d(LOGGER_TAG, "LocationService: Location loop started");
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    public boolean isLocationServiceRunning(Context context) {
        Log.d(LOGGER_TAG, "LocationService: checking is service active");
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

    public void writeToFile(Context context, String string){
        File internalStorageDir = context.getFilesDir();
        File file = new File(internalStorageDir, "coordinates.txt");
        try{
            FileWriter writer = new FileWriter(file, true);
            if(placePipeCharacter){
                string = "|" + string;
            }else {
                placePipeCharacter = true;
            }
            writer.write(string);
            writer.close();
        }catch (IOException e){
            Toast.makeText(this, "Error: "+ e, Toast.LENGTH_LONG).show();
        }
    }

    static class LocationListenerHolder {
        private static LocationListener locationListener;

        public static synchronized void setLocationListener(LocationListener listener) {
            locationListener = listener;
        }

        public static synchronized LocationListener getLocationListener() {
            return locationListener;
        }
    }
}
