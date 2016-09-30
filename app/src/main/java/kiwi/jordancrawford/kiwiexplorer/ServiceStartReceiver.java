package kiwi.jordancrawford.kiwiexplorer;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

/**
 * Receives the boot and providers changed intents and starts the service.
 *
 * Created by Jordan on 22/09/16.
 */
public class ServiceStartReceiver extends BroadcastReceiver {
    private static String ON_BOOT_INTENT = "android.intent.action.BOOT_COMPLETED";
    private static String LOCATION_SETTING_CHANGE_INTENT = "android.location.PROVIDERS_CHANGED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ON_BOOT_INTENT)) {
            startService(context);
        } else if (intent.getAction().equals(LOCATION_SETTING_CHANGE_INTENT)) {
            startService(context);
        }
    }

    public void startService(Context context) {
        ComponentName comp = new ComponentName(context.getPackageName(), BackgroundLocationService.class.getName());
        ComponentName service = context.startService(new Intent().setComponent(comp));
    }
}
