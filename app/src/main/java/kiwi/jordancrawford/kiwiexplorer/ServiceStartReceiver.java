package kiwi.jordancrawford.kiwiexplorer;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Jordan on 22/09/16.
 */
public class ServiceStartReceiver extends BroadcastReceiver {
    private static String ON_BOOT_INTENT = "android.intent.action.BOOT_COMPLETED";
    private static String LOCATION_SETTING_CHANGE_INTENT = "android.location.PROVIDERS_CHANGED";

    // TODO: Call this from the MainActivity as a way to start the service.
        // Potentially make this check if the service is running before trying to start it?
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ON_BOOT_INTENT)) {
            System.out.println("Got a boot intent.");
            startService(context);
        } else if (intent.getAction().equals(LOCATION_SETTING_CHANGE_INTENT)) { // TODO: Is this one actually needed? I guess the service would stop if it doesn't have permissions or the correct setting.
            System.out.println("Got a change location setting intent.");
            startService(context);
        }
    }

    public void startService(Context context) {
        System.out.println("Starting the service.");
        ComponentName comp = new ComponentName(context.getPackageName(), BackgroundLocationService.class.getName());
        ComponentName service = context.startService(new Intent().setComponent(comp));
    }
}
