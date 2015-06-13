package org.pimatic.app;

import android.app.Application;
import android.content.Intent;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by Oliver Schneider <oliverschneider89+sweetpi@gmail.com>
 */
public class PimaticApplication extends Application {
    public void onCreate ()
    {
        // Setup handler for uncaught exceptions.
        Thread.setDefaultUncaughtExceptionHandler (new Thread.UncaughtExceptionHandler()
        {
            @Override
            public void uncaughtException (Thread thread, Throwable e)
            {
                handleUncaughtException (thread, e);
            }
        });
    }

    public void handleUncaughtException (Thread thread, Throwable e)
    {
        e.printStackTrace(); // not all Android versions will print the stack trace automatically
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        Intent intent = new Intent ();
        intent.setAction("org.pimatic.app.SEND_LOG"); // see step 5.
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // required when starting from Application
        intent.putExtra(SendLogActivity.ARG_STACKTRACE, sw.toString());
        startActivity (intent);
        System.exit(1); // kill off the crashed app
    }
}
