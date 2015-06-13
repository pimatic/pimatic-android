package org.pimatic.connection;

import android.app.Activity;

import org.pimatic.model.ConnectionOptions;

/**
 * Created by Oliver Schneider <oliverschneider89+sweetpi@gmail.com>
 */
public class Connection {

    private static RestClient rest;
    private static SocketClient socket;
    private static ConnectionOptions conOpts;

    public static void setup(Activity mainActivity, ConnectionOptions conOpts) {
        Connection.conOpts = conOpts;
        socket = new SocketClient(mainActivity, conOpts);
        rest = new RestClient(mainActivity, conOpts);
    }

    public static void switchConnection(Activity mainActivity, ConnectionOptions conOpts) {
        socket.disconnect();
        Connection.conOpts = conOpts;
        socket = new SocketClient(mainActivity, conOpts);
        rest = new RestClient(mainActivity, conOpts);
    }

    public static void connect() {
        if(socket != null) {
            socket.connect();
        }
    }

    public static RestClient getRest() {
        return rest;
    }

    public static SocketClient getSocket() {
        return socket;
    }

    public static void disconnect() {
        if(socket != null) {
            socket.disconnect();
        }
    }

    public static ConnectionOptions getOptions() {
        return Connection.conOpts;
    }
}
