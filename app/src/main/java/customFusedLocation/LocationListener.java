package customFusedLocation;

import android.location.Location;

import androidx.annotation.Nullable;

import java.io.Serializable;

public interface LocationListener extends Serializable {
  void onLocationReceived(@Nullable Location location);
}

