package org.pimatic.helpers;

import android.app.Activity;
import android.text.TextUtils;

/**
 * Created by Oliver Schneider <oliverschneider89+sweetpi@gmail.com>
 */
public class Toast {

    public static void showMessage(final Activity activity, final String msg) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                android.widget.Toast.makeText(activity.getBaseContext(), msg, android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }
}
