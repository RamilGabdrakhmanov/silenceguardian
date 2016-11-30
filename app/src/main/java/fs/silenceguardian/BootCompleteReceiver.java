package fs.silenceguardian;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by ramilgabdrakhmanov on 11/30/16.
 */

public class BootCompleteReceiver extends BroadcastReceiver {

    @Override public void onReceive(Context context, Intent intent) {

        if (intent.getAction() != null &&
            intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
        }
    }
}
