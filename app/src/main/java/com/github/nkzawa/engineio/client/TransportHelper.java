package com.github.nkzawa.engineio.client;

/**
 * Created by h3llfire on 27.06.14.
 */
public class TransportHelper {

    public static Transport getTransport(Socket engine) {
        return engine.transport;
    }
}
