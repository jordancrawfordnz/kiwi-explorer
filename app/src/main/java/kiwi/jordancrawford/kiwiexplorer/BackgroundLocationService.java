package kiwi.jordancrawford.kiwiexplorer;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by Jordan on 22/09/16.
 */
public class BackgroundLocationService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    public static final int LOCATION_REQUEST_RESULT_CODE = 1;

    private Date creationTime;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private boolean isRequestingLocationUpdates;

    IBinder mBinder = new LocalBinder();

    // == Service setup.
    public class LocalBinder extends Binder {
        public BackgroundLocationService getServerInstance() {
            return BackgroundLocationService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        creationTime = new Date();
        locationRequest = LocationSettingHelper.getLocationRequest();
        System.out.println("Background location service created. Creation time: " + DateFormat.getDateTimeInstance().format(creationTime));
    }

    @Override
    public void onDestroy() {
        System.out.println("Service destroyed. Creation time: " + DateFormat.getDateTimeInstance().format(creationTime));
        isRequestingLocationUpdates = false;
        if (googleApiClient != null) {
            googleApiClient.disconnect();
            googleApiClient.unregisterConnectionCallbacks(this);
            googleApiClient.unregisterConnectionFailedListener(this);
            googleApiClient = null;
        }
        super.onDestroy();
    }

    // == Google Play Services Callbacks
    /**
     * When Google Play service connects, call the onHasGooglePlayAPI callback.
     * @param bundle
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        onHasGooglePlayAPI();
    }

    /**
     * When the Google Play service connection suspends, set the object to null so it will be re-setup when next started.
     * @param i
     */
    @Override
    public void onConnectionSuspended(int i) {
        googleApiClient = null; // Create a new connection when next needed.
        isRequestingLocationUpdates = false;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        googleApiClient = null;
        isRequestingLocationUpdates = false;
        onCannotGetLocation();
    }

    // == Service events.

    /**
     * Starts the service processing flow. If this fails at any stage of the process, execution stops and will be re-tried the next time the command is started.
     *
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    public int onStartCommand (Intent intent, int flags, int startId) {
        if (isRequestingLocationUpdates) {
            return START_STICKY;
        }

        // Get a Google API client if one is needed.
        if (googleApiClient == null || !googleApiClient.isConnected()) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            googleApiClient.connect();
        } else {
            onHasGooglePlayAPI(); // continue, don't need to request Google Play API.
        }

        return START_STICKY;
    }

    /**
     * Called when the Google Play API is present.
     *
     * Gets permissions and checks the location settings are correct. If these checks are successful, calls onReadyToGetLocation.
     */
    public void onHasGooglePlayAPI() {
        // Check the permissions.
        if (PermissionHelper.hasPermission(getApplicationContext())) {
            // Check the location settings are correct.
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);
            PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient,
                            builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            // No problems with the settings.
                            onReadyToGetLocation();
                            break;
                        default:
                            onCannotGetLocation();
                            break;
                    }
                }
            });
        }
    }

    /**
     * Called if location cannot be determined for any reason. This unsets the current location.
     */
    public void onCannotGetLocation() {
        // TODO: Unset the current location.
    }

    /**
     * Called when all permissions have been checked and the location can be requested.
     */
    public void onReadyToGetLocation() {
        if (!isRequestingLocationUpdates) {
            Intent intent = new Intent(this, BackgroundLocationReceiver.class);
            PendingIntent locationIntent = PendingIntent.getBroadcast(getApplicationContext(), LOCATION_REQUEST_RESULT_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            //noinspection MissingPermission (we request permissions in onHasGooglePlayAPI).
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, locationIntent);
            isRequestingLocationUpdates = true;
        }
    }

}
