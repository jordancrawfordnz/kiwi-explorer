package kiwi.jordancrawford.kiwiexplorer;

import com.google.android.gms.location.LocationRequest;

/**
 * Created by Jordan on 28/09/16.
 */
public class LocationSettingHelper {
    private static final long LOCATION_INTERVAL = 1000;
    private static final long FASTEST_LOCATION_INTERVAL = 1000;

    public static LocationRequest getLocationRequest() {
        // Setup the location request.
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(LOCATION_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_LOCATION_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        return locationRequest;
    }
}
