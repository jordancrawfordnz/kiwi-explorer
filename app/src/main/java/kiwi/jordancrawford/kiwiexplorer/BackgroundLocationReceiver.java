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

import com.google.android.gms.location.LocationResult;

/**
 * Created by Jordan on 28/09/16.
 */
public class BackgroundLocationReceiver extends BroadcastReceiver {
    private CityResultReceiver cityResultReceiver;

    public BackgroundLocationReceiver() {
        super();

        // Setup the city resolver.
        cityResultReceiver = new CityResultReceiver(new Handler());
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        if (LocationResult.hasResult(intent)) {
            LocationResult locationResult = LocationResult.extractResult(intent);
            Location location = locationResult.getLastLocation();

            if (location == null) {
                return;
            }
            if (Geocoder.isPresent()) {
                // Get the city.
                Intent getCityIntent = new Intent(context, FetchCityIntentService.class);
                getCityIntent.putExtra(FetchCityIntentService.Constants.RECEIVER, cityResultReceiver);
                getCityIntent.putExtra(FetchCityIntentService.Constants.LOCATION_DATA_EXTRA, location);
                context.startService(getCityIntent);
            }
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
            }
        }
    }
}
