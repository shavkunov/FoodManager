package ru.spbau.mit.foodmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Узнает о запуске Андроида и запускает процесс нотификаций
 */

public class BootCompleteReceiver extends BroadcastReceiver {
    public BootCompleteReceiver() {

    }
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            context.startService(new Intent(context, NotificationService.class));
        }
    }
}
