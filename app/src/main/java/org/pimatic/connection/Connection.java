package org.pimatic.connection;

import android.app.Activity;

import org.pimatic.model.ConnectionOptions;

/**
 * Created by h3llfire on 16.05.15.
 */
public class Connection {

    private static RestClient rest;
    private static SocketClient socket;

    public static void setup(Activity mainActivity, ConnectionOptions conOpts) {
        socket = new SocketClient(mainActivity, conOpts);
        rest = new RestClient(mainActivity, conOpts);
    }

    public static void connect() {
        socket.connect();
    }

    public static RestClient getRest() {
        return rest;
    }

    public static SocketClient getSocket() {
        return socket;
    }
}
