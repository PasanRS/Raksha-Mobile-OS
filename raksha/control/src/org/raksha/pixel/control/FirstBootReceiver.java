package org.raksha.pixel.control;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;

public class FirstBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;
        SharedPreferences prefs = context.getSharedPreferences("raksha_prefs", Context.MODE_PRIVATE);
        if (prefs.getBoolean("onboarding_launched", false)) return;
        int prov = Settings.Global.getInt(context.getContentResolver(), Settings.Global.DEVICE_PROVISIONED, 0);
        if (prov != 1) return;
        prefs.edit().putBoolean("onboarding_launched", true).apply();
        try {
            Intent launch = new Intent(context, ControlActivity.class);
            launch.putExtra("first_boot_onboarding", true);
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(launch);
        } catch (RuntimeException ignored) {
        }
    }
}
