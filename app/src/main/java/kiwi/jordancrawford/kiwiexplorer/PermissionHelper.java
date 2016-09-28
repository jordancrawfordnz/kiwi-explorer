package kiwi.jordancrawford.kiwiexplorer;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

/**
 * Created by Jordan on 28/09/16.
 */
public class PermissionHelper {
    public static final String  REQUIRED_PERMISSION = android.Manifest.permission.ACCESS_COARSE_LOCATION;

    public static boolean hasPermission(Context context) {
        return ActivityCompat.checkSelfPermission(context, REQUIRED_PERMISSION) == PackageManager.PERMISSION_GRANTED;
    }
}
