package com.example.locationapp;

import static com.example.locationapp.Constants.locationRequest;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.Task;

public class Permissions {
  private static LocationManager locationManager;

  public static boolean checkPermissions(Activity activity, Context context) {
    boolean isAllPermissionsGranted = false;

    Log.d("LocationApp", "Permissions: checking permissions");

    if (isLocationAllowed(context)) {
      if (isGpsEnabled(context)) {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
          isAllPermissionsGranted = true;
        } else {
          if(isBackgroundLocationAllowed(context)) {
            isAllPermissionsGranted = true;
          } else {
            requestBackgroundLocationPermission(activity);
          }
        }
      } else {
        turnOnGps(activity, context);
      }
    } else {
      requestLocationPermission(activity);
    }

    return  isAllPermissionsGranted;
  }

  private static boolean isGpsEnabled(Context context) {
    if(locationManager == null)
      locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    Log.d("LocationApp", "Permissions: is GPS enabled");
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
  }

  private static void turnOnGps(Activity activity, Context context) {
    Log.d("LocationApp", "Permissions: Request GPS turn on");
    LocationSettingsRequest.Builder builder =
          new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
    builder.setAlwaysShow(true);

    Task<LocationSettingsResponse> result =
          LocationServices.getSettingsClient(context.getApplicationContext())
                .checkLocationSettings(builder.build());

    result.addOnCompleteListener(task -> {
      try {
        task.getResult(ApiException.class);
        Toast.makeText(context, "GPS is already turned on", Toast.LENGTH_SHORT).show();
      } catch (ApiException e) {
        switch (e.getStatusCode()) {
          case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
            try {
              ResolvableApiException resolvableApiException = (ResolvableApiException) e;
              resolvableApiException.startResolutionForResult(activity, 2);
            } catch (IntentSender.SendIntentException ex) {
              ex.printStackTrace();
            }
            break;
          case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
            //Device does not have location
            Toast.makeText(context, "This device does not support Location", Toast.LENGTH_LONG).show();
            break;
        }
      }
    });
  }

  private static boolean isLocationAllowed(Context context) {
    Log.d("LocationApp", "Permissions: is location permission allowed");
    return ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
  }

  private static void requestLocationPermission(Activity activity) {
    Log.d("LocationApp", "Permissions: request location permission");
    ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 44);
  }

  @RequiresApi(api = Build.VERSION_CODES.Q)
  private static boolean isBackgroundLocationAllowed(Context context){
    Log.d("LocationApp", "Permissions: is background location permission allowed");
    return ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
  }

  @RequiresApi(api = Build.VERSION_CODES.Q)
  private static void requestBackgroundLocationPermission(Activity activity) {
    Log.d("LocationApp", "Permissions: requesting background location permission");
    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 55);
  }

}
