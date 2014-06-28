package com.github.nkzawa.socketio.client;

import com.github.nkzawa.engineio.client.*;
import com.github.nkzawa.engineio.client.Socket;

/**
 * Created by h3llfire on 27.06.14.
 */
public class EngineHelper {

    public static Socket getEngine(Manager manager) {
        return manager.engine;
    }

}
