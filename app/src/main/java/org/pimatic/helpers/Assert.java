package org.pimatic.helpers;

import android.os.Looper;

/**
 * Created by h3llfire on 31.05.15.
 */
public class Assert {

    public static void isMainThread() {
        if (Looper.getMainLooper().getThread() != Thread.currentThread()) {
            throw new AssertionError("Not on main thread");
        }
    }
}
