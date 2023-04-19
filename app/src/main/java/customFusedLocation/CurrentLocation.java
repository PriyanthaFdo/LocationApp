package customFusedLocation;

import static customFusedLocation.Constants.locationRequest;

import android.Manifest;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;

public class CurrentLocation {
  private LocationCallback locationCallback;


  @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
  public void getCurrentLocation(FusedLocationProviderClient fusedLocationClient, LocationListener listener) {
    locationCallback = new LocationCallback() {
      @Override
      public void onLocationResult(@NonNull LocationResult locationResult) {
        super.onLocationResult(locationResult);
        if (locationResult.getLocations().size() > 0) {
          fusedLocationClient.removeLocationUpdates(locationCallback);
          listener.onLocationReceived(locationResult.getLastLocation());
        } else {
          listener.onLocationReceived(null);
        }
      }
    };
    startGetLocationLoop(fusedLocationClient);

  }

  @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
  private void startGetLocationLoop(FusedLocationProviderClient fusedLocationClient) {
    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
  }
}
