package kiwi.jordancrawford.kiwiexplorer;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Displays a list of cities.
 */
public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ActivityCompat.OnRequestPermissionsResultCallback {
    private static final int REQUEST_LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int REQUEST_CHECK_SETTINGS_CODE = 2;
    private static final int SNACKBAR_PERMISSION_TIMEOUT = 10000;

    private GoogleApiClient googleApiClient;
    private RecyclerView cityRecyclerView;
    private RecyclerView.LayoutManager cityRecyclerViewLayoutManager;
    private RecyclerView.Adapter cityRecyclerViewAdapter;
    private ArrayList<City> cities = new ArrayList<City>();
    private View containerView;

    private BroadcastReceiver databaseUpdateMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Refresh the database.
            loadCities();
        }
    };

    private BroadcastReceiver cityClickedMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get the city from the intent.
            City city = intent.getParcelableExtra(CityListAdapter.CITY_EXTRA);

            // Start the map view.
            Intent startMapIntent = new Intent(MainActivity.this, CityViewActivity.class);
            startMapIntent.putExtra(CityViewActivity.CITY_EXTRA, city);
            startActivity(startMapIntent);
        }
    };

    private BroadcastReceiver citySeenClickedMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get the city from the intent.
            City city = intent.getParcelableExtra(CityListAdapter.CITY_EXTRA);

            // Toggle whether the city is seen.
            CityData cityData = DatabaseHelper.getInstance(MainActivity.this).getCityDataByCityName(city.getName());
            cityData.setCitySeen(!cityData.isCitySeen());
            DatabaseHelper.getInstance(MainActivity.this).updateCityData(cityData);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get a Google API client.
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(AppIndex.API).build();
        } else {
            checkLocationPermission();
        }

        containerView = findViewById(R.id.main_activity_container);

        // Start the service.
        ComponentName comp = new ComponentName(this.getPackageName(), BackgroundLocationService.class.getName());
        ComponentName service = this.startService(new Intent().setComponent(comp));

        // Setup the city recycler view.
        cityRecyclerView = (RecyclerView) findViewById(R.id.city_recycler_view);
        int deviceOrientation = getResources().getConfiguration().orientation;
        int numberOfColumns = deviceOrientation == Configuration.ORIENTATION_LANDSCAPE ? 2 : 1;
        cityRecyclerViewLayoutManager = new GridLayoutManager(this, numberOfColumns);
        cityRecyclerView.setLayoutManager(cityRecyclerViewLayoutManager);

        // Setup the list adapter.
        cityRecyclerViewAdapter = new CityListAdapter(this, cities);
        cityRecyclerView.setAdapter(cityRecyclerViewAdapter);
    }

    protected void onStart() {
        googleApiClient.connect();
        loadCities();

        LocalBroadcastManager.getInstance(this).registerReceiver(cityClickedMessageReceiver, new IntentFilter(CityListAdapter.CITY_CLICK_KEY));
        LocalBroadcastManager.getInstance(this).registerReceiver(citySeenClickedMessageReceiver, new IntentFilter(CityListAdapter.CITY_SEEN_CLICK_KEY));
        LocalBroadcastManager.getInstance(this).registerReceiver(databaseUpdateMessageReceiver, new IntentFilter(DatabaseHelper.DATABASE_UPDATE_KEY));

        super.onStart();
    }

    protected void onStop() {
        googleApiClient.disconnect();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(cityClickedMessageReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(citySeenClickedMessageReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(databaseUpdateMessageReceiver);

        super.onStop();
    }

    /**
     * Fills in all data abbout cities and sets up the subtitle with the number of cities seen.
     */
    private void loadCities() {
        try {
            cities.clear();
            cities.addAll(Cities.getCities(this));
            Cities.fillInCityData(this); // Fill in the city data from the database.
            cityRecyclerViewAdapter.notifyDataSetChanged();

            // Fill in the number of cities seen as a subtitle.
            ArrayList<City> citiesSeen = new ArrayList<>();
            for (City city : cities) {
                if (city.getCityData() != null && city.getCityData().isCitySeen()) {
                    citiesSeen.add(city);
                }
            }
            String format = getResources().getString(R.string.cities_seen_format);
            String citiesSeenText = String.format(format, citiesSeen.size(), cities.size());
            getSupportActionBar().setSubtitle(citiesSeenText);

        } catch (IOException ioException) {
            Toast.makeText(this, R.string.error_get_cities_io_exception, Toast.LENGTH_LONG).show();
        } catch (JSONException jsonException) {
            Toast.makeText(this, R.string.error_get_cities_json_exception, Toast.LENGTH_LONG).show();
        }
    }

    // == Location permissions

    /**
     * Checks the location permissions and requests permissions where appropriate.
     */
    private void checkLocationPermission() {
        if (PermissionHelper.hasPermission(this)) {
            onHaveLocationPermission();
        } else { // If don't have the required permission.
            // See if we should request it.
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, PermissionHelper.REQUIRED_PERMISSION)) {
                // Show the rationale.
                Snackbar snackbar = Snackbar
                        .make(containerView, R.string.permission_snackbar_explainer, SNACKBAR_PERMISSION_TIMEOUT)
                        .setAction(R.string.permission_snackbar_action, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                requestLocationPermission();
                            }
                        });
                snackbar.show();
            } else {
                // Request it. No need to explain.
                requestLocationPermission();
            }
        }
    }

    /**
     * Sends the request for permissions.
     */
    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{
                PermissionHelper.REQUIRED_PERMISSION
        }, REQUEST_LOCATION_PERMISSION_REQUEST_CODE);
    }

    /**
     * Handle the result for requesting permission. If have the permission, continue onto onHaveLocationPermission.
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // If the user accepted the location permission, request the location.
        if (requestCode == REQUEST_LOCATION_PERMISSION_REQUEST_CODE) {
            if (PermissionHelper.hasPermission(this)) {
                onHaveLocationPermission();
            }
        }
    }

    // == Location settings

    /**
     * Check if the location settings are valid. If not, then do something about it. If it is or there is nothing we can do about it, don't do anything as the service can handle it.
     */
    private void checkLocationSettings() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(LocationSettingHelper.getLocationRequest());
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient,
                        builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Show a dialog to request the user changes the settings.
                        try {
                            status.startResolutionForResult(
                                    MainActivity.this,
                                    REQUEST_CHECK_SETTINGS_CODE);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    // If the settings change is unavailable, don't do anything.
                }
            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        checkLocationPermission();
    }

    public void onHaveLocationPermission() {
        checkLocationSettings();
    }

    @Override
    public void onConnectionSuspended(int i) {
        googleApiClient = null;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        googleApiClient = null;
        Toast.makeText(this, R.string.error_google_play_services_no_resolution, Toast.LENGTH_LONG).show();
    }
}
