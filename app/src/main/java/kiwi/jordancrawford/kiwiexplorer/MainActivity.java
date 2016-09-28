package kiwi.jordancrawford.kiwiexplorer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
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
    private GoogleApiClient googleApiClient;

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

        // Start the service.
        ComponentName comp = new ComponentName(this.getPackageName(), BackgroundLocationService.class.getName());
        ComponentName service = this.startService(new Intent().setComponent(comp));
    }

    public void launchMapsView(View view) {
        Intent intent = new Intent(this, CityViewActivity.class);
        startActivity(intent);

    }

    protected void onStart() {
        googleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
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
            if (PermissionHelper.hasPermission(this)) {
                onHaveLocationPermission();
            }
        }
    }

    // == Location settings
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
        if (connectionResult.hasResolution()) {
            // TODO: Display an appropriate result to the user.
        }
        System.out.println("Could not connect to Google Play Services.");
    }
}
