package kiwi.jordancrawford.kiwiexplorer;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.ResultReceiver;
import android.os.Bundle;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Fetches the name of the city.
 *
 * Created by Jordan on 22/09/16.
 */
public class FetchCityIntentService extends IntentService {
    protected ResultReceiver resultReceiver;

    public FetchCityIntentService() {
        super("FetchCity");
    }

    public final class Constants {
        public static final int SUCCESS_RESULT = 0;
        public static final int FAILURE_RESULT = 1;
        public static final String PACKAGE_NAME = "kiwi.jordancrawford.kiwiexplorer";
        public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
        public static final String RESULT_DATA_KEY = PACKAGE_NAME + ".RESULT_DATA_KEY";
        public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_DATA_EXTRA";

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        // Get the location passed to this service through an extra.
        Location location = intent.getParcelableExtra(
                Constants.LOCATION_DATA_EXTRA);
        resultReceiver = (ResultReceiver) intent.getParcelableExtra(Constants.RECEIVER);

        List<Address> addresses = null;

        try {
            // Get one address from the provided location.
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        } catch (IOException ioException) {
        } catch (IllegalArgumentException illegalArgumentException) {
        }

        if (addresses != null && addresses.size() > 0) {
            // An address was found.
            String city = addresses.get(0).getLocality();
            deliverResultToReceiver(Constants.SUCCESS_RESULT, city);
        } else {
            deliverResultToReceiver(Constants.FAILURE_RESULT, null);
        }
    }

    private void deliverResultToReceiver(int resultCode, String city) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.RESULT_DATA_KEY, city);
        resultReceiver.send(resultCode, bundle);
    }
}
