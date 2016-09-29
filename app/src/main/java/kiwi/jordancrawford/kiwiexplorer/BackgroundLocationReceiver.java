package kiwi.jordancrawford.kiwiexplorer;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

import com.google.android.gms.common.data.DataBuffer;
import com.google.android.gms.location.LocationResult;

import javax.crypto.spec.DHGenParameterSpec;

/**
 * Created by Jordan on 28/09/16.
 */
public class BackgroundLocationReceiver extends BroadcastReceiver {
    private static Location lastLocation = null; // Cache the last location to avoid excessive requests to Google.
    private static boolean lastLocationRetreiveSuccess; // Whether the last location was retreived successfully.
    private static final int MINIMUM_DISTANCE_FOR_REVERSE_LOOKUP = 1000; // The minimum
    private Context context;

    private CityResultReceiver cityResultReceiver;

    public BackgroundLocationReceiver() {
        super();

        // Setup the city resolver.
        cityResultReceiver = new CityResultReceiver(new Handler());
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context.getApplicationContext();
        if (LocationResult.hasResult(intent)) {
            LocationResult locationResult = LocationResult.extractResult(intent);
            Location location = locationResult.getLastLocation();

            if (location == null) {
                return;
            }

            // If the last location is not defined, couldn't retreive the reverse address successfully, or the distance between the points is greater than the minimum, then do a reverse lookup.
            if (lastLocation == null || !lastLocationRetreiveSuccess || lastLocation.distanceTo(location) > MINIMUM_DISTANCE_FOR_REVERSE_LOOKUP) {
                if (Geocoder.isPresent()) {
                    // Get the city.
                    Intent getCityIntent = new Intent(context, FetchCityIntentService.class);
                    getCityIntent.putExtra(FetchCityIntentService.Constants.RECEIVER, cityResultReceiver);
                    getCityIntent.putExtra(FetchCityIntentService.Constants.LOCATION_DATA_EXTRA, location);
                    context.startService(getCityIntent);
                } else {
                    lastLocationRetreiveSuccess = false;
                }
            }
            lastLocation = location;
        }
    }

    @SuppressLint("ParcelCreator")
    class CityResultReceiver extends ResultReceiver {
        public CityResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (resultCode == FetchCityIntentService.Constants.SUCCESS_RESULT) {
                String city = resultData.getString(FetchCityIntentService.Constants.RESULT_DATA_KEY);

                System.out.println("In background service, city: " + city);

                lastLocationRetreiveSuccess = true;

                if (context != null) {
                    CityData cityData = null;
                    if (city != null) {
                        cityData = DatabaseHelper.getInstance(context).getCityDataByCityName(city);
                        cityData.setCitySeen(true);
                        cityData.setCurrentLocation(true);
                    }

                    // Get the last current location.
                    CityData currentCityData = DatabaseHelper.getInstance(context).getCurrentCity();
                    // If the current city is defined and is different to the new current city.
                    if (currentCityData != null && (cityData != null && !cityData.getCityName().equals(currentCityData.getCityName()))) {
                        currentCityData.setCurrentLocation(false);
                        DatabaseHelper.getInstance(context).updateCityData(currentCityData);
                    }

                    if (cityData != null) {
                        // Update the new current city.
                        DatabaseHelper.getInstance(context).updateCityData(cityData);
                    }

                    // TODO: Need to send an intent to let the activity know?
                }
            } else {
                lastLocationRetreiveSuccess = false;
            }
        }
    }
}
