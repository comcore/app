package com.gmail.comcorecrew.comcore.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * BroadcastReceiver to reschedule all alarms when the system boots.
 */
public class BootBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            return;
        }

        NotificationScheduler.init(context);
        NotificationScheduler.resetAllAlarms();
    }
}