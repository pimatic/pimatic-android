package org.pimatic.helpers;

import android.os.Looper;

/**
 * Created by Oliver Schneider <oliverschneider89+sweetpi@gmail.com>
 */
public class Assert {

    public static void isMainThread() {
        if (Looper.getMainLooper().getThread() != Thread.currentThread()) {
            throw new AssertionError("Not on main thread");
        }
    }
}
