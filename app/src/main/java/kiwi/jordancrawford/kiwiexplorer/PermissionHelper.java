package kiwi.jordancrawford.kiwiexplorer;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

/**
 * Created by Jordan on 28/09/16.
 */
public class PermissionHelper {
    public static final String REQUIRED_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION;

    public static boolean hasPermission(Context context) {
        return ActivityCompat.checkSelfPermission(context, REQUIRED_PERMISSION) == PackageManager.PERMISSION_GRANTED;
    }
}
