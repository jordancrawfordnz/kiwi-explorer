package kiwi.jordancrawford.kiwiexplorer;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Geocoder;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;

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

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by Jordan on 22/09/16.
 */
public class BackgroundLocationService extends Service implements
        GoogleApiClient.ConnectionCallbacks {
    public static final int LOCATION_REQUEST_RESULT_CODE = 1;

    private Date creationTime;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private boolean isRequestingLocationUpdates;

    private static final long LOCATION_INTERVAL = 1000;
    private static final long FASTEST_LOCATION_INTERVAL = 1000;

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
        System.out.println("Background location service created. Creation time: " + DateFormat.getDateTimeInstance().format(creationTime));

        // Setup the location request.
        locationRequest = new LocationRequest();
        locationRequest.setInterval(LOCATION_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_LOCATION_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    @Override
    public void onDestroy() {
        System.out.println("Service destroyed. Creation time: " + DateFormat.getDateTimeInstance().format(creationTime));
        isRequestingLocationUpdates = false;
        if (googleApiClient != null) {
            googleApiClient.disconnect();
            googleApiClient.unregisterConnectionCallbacks(this);
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
        System.out.println("On start command. Creation time: " + DateFormat.getDateTimeInstance().format(creationTime));
        if (isRequestingLocationUpdates) {
            return START_STICKY;
        }

        // Get a Google API client if one is needed.
        if (googleApiClient == null || !googleApiClient.isConnected()) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addApi(LocationServices.API)
                    .build();
            googleApiClient.connect();
        } else {
            onHasGooglePlayAPI(); // continue, don't need to request Google Play API.
        }

        return START_STICKY;
    }

    /**
     * Called when the Google APlay API is present.
     *
     * Gets permissions and checks the location settings are correct. If these checks are successful, calls onReadyToGetLocation.
     */
    public void onHasGooglePlayAPI() {
        System.out.println("Has Google API.");
        // Check the permissions.
        if (PermissionHelper.hasPermission(getApplicationContext())) {
            System.out.println("Has permission");
            // Check the location settings are correct.
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);
            System.out.println(googleApiClient);
            System.out.println(locationRequest);
            PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient,
                            builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    System.out.println("Has location settings result.");
                    System.out.println(result);
                    final Status status = result.getStatus();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            // No problems with the settings.
                            onReadyToGetLocation();
                            break;
                    }
                }
            });
        }
    }

    /**
     * Called when all permissions have been checked and the location can be requested.
     */
    public void onReadyToGetLocation() {
        System.out.println("Ready to get location");
        if (!isRequestingLocationUpdates) {
            Intent intent = new Intent(this, BackgroundLocationReceiver.class);
            PendingIntent locationIntent = PendingIntent.getBroadcast(getApplicationContext(), LOCATION_REQUEST_RESULT_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            //noinspection MissingPermission (we request permissions in onHasGooglePlayAPI).
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, locationIntent);
            isRequestingLocationUpdates = true;
        }
    }

}
