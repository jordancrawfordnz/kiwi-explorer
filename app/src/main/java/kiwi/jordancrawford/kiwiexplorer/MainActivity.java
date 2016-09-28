package kiwi.jordancrawford.kiwiexplorer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ActivityCompat.OnRequestPermissionsResultCallback {
    private static final int REQUEST_LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int REQUEST_CHECK_SETTINGS_CODE = 2;
    private static final long LOCATION_INTERVAL = 1000;
    private static final long FASTEST_LOCATION_INTERVAL = 1000;

    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    private LocationRequest locationRequest;
    private boolean isRequestingLocationUpdates = false;
    private CityResultReceiver cityResultReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup a location request.
        locationRequest = new LocationRequest();
        locationRequest.setInterval(LOCATION_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_LOCATION_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);

        // Get a Google API client.
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        } else {
            checkLocationPermission();
        }

        cityResultReceiver = new CityResultReceiver(new Handler());

        // Start the service.
        System.out.println("Starting the service.");
        ComponentName comp = new ComponentName(this.getPackageName(), BackgroundLocationService.class.getName());
        ComponentName service = this.startService(new Intent().setComponent(comp));
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
                System.out.println("In main activity, city: " + city);
            }
        }
    }

    protected void onStart() {
        googleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        googleApiClient.disconnect();
        // TODO: Stop requesting updates
        super.onStop();
    }

    private void getLocation() {
        System.out.println("About to get location");
        //noinspection MissingPermission
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (lastLocation != null) {
            System.out.println("Got location");
            System.out.println(lastLocation);
        } else {
            System.out.println("Last location is null");
        }
    }

    // == Location permissions
    private void checkLocationPermission() {
        if (PermissionHelper.hasPermission(this)) {
            onHaveLocationPermission();
        } else { // If don't have the reequired permission, request it.
            ActivityCompat.requestPermissions(this, new String[]{
                    PermissionHelper.REQUIRED_PERMISSION
            }, REQUEST_LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // If the user accepted the location permission, request the location.
        if (requestCode == REQUEST_LOCATION_PERMISSION_REQUEST_CODE) {
            System.out.println("On request permission result");
            if (permissions.length == 1 && permissions[0].equals(Manifest.permission.ACCESS_COARSE_LOCATION) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onHaveLocationPermission();
            }
        }
    }

    // == Location settings
    private void checkLocationSettings() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient,
                        builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // No problems with the settings.
                        System.out.println("Success");
                        onAllowedToGetLocation();
                        break;
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

    public void onActivityResult (int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CHECK_SETTINGS_CODE) {
            System.out.println("On check settings result");
            if (resultCode == RESULT_OK) {
                System.out.println("Settings are all ok");
                onAllowedToGetLocation();
            } else {
                System.out.println("Settings are not ok");
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        System.out.println("Connected to Google Play Services.");

        checkLocationPermission();
    }

    public void onHaveLocationPermission() {
        System.out.println("Have location permission");
        checkLocationSettings();
    }

    public void onAllowedToGetLocation() {
        // TODO: Call the BackgroundLocationService.
    }

    @Override
    public void onConnectionSuspended(int i) {
        googleApiClient = null;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        googleApiClient = null;
        if (connectionResult.hasResolution()) {
            // TODO: Display an appropriate result to the user.
        }
        System.out.println("Could not connect to Google Play Services.");
    }
}
