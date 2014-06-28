package org.pimatic.connection;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by h3llfire on 28.06.14.
 */
public abstract class SocketIOClient {

    private HashMap<String, ArrayList<JsonEventListener>> listener = new HashMap<String, ArrayList<JsonEventListener>>();
    public String sid;
    public int pingInterval = 0;
    public int pingTimeout = 0;
    private Timer timer;

    private EngineIOClient engine;

    public SocketIOClient(String host, int port) {
        timer = new Timer();
        engine = new EngineIOClient(host, port) {
            @Override
            public void onMessage(EngineIOPackage enginePackage) {
                try {
                    SocketIOPackage socketPackage = SocketIOClient.this.parsePackage(enginePackage.data);
                    switch (socketPackage.type) {
                        case SocketIOPackage.TYPE_EVENT:
                            SocketIOClient.this.handleEventPackage(socketPackage);
                            break;
                        default:
                            Log.w("Websocket", "Unhandled socket.io package type " + socketPackage.type);
                    }
                } catch (SocketIoPackageParseException e) {
                    SocketIOClient.this.onError(e);
                }
            }

            @Override
            public void onOpen(EngineIOPackage enginePackage) {
                try {
                    SocketIOClient.this.handleOpenPackage(enginePackage);
                } catch (SocketIoPackageParseException e) {
                    SocketIOClient.this.onError(e);
                }
            }

            @Override
            public void onError(Exception e) {
                SocketIOClient.this.onError(e);
            }

            @Override
            public void onClose() {
                SocketIOClient.this.timer.cancel();
                SocketIOClient.this.onClose();
            }
        };

    }


    private void handleEventPackage(SocketIOPackage socketPackage) throws SocketIoPackageParseException {
        try {
            JSONArray data = new JSONArray(socketPackage.data);
            String eventId = data.getString(0);
            Object jsonData = data.get(1);
            messageReceived(eventId, jsonData);
        } catch (JSONException e) {
            throw new SocketIoPackageParseException("Error parsing event package", e);
        }
    }

    private void messageReceived(String eventId, Object jsonData) {
        onMessage(eventId, jsonData);
        ArrayList<JsonEventListener> list = listener.get(eventId);
        if(list != null) {
            for(JsonEventListener l : list) {
                try {
                    l.onEvent(jsonData);
                } catch (Exception e) {
                    SocketIOClient.this.onError(e);
                }
            }
        }
    }

    public void addListener(String eventId, JsonEventListener l) {
        ArrayList<JsonEventListener> list = listener.get(eventId);
        if(list == null) {
            list = new ArrayList<JsonEventListener>();
            listener.put(eventId, list);
        }
        list.add(l);
    }

    public abstract void onMessage(String eventId, Object jsonData);
    public abstract void onOpen();
    public abstract void onError(Exception e);
    public abstract void onClose();

    private void doPing() {
        engine.send(new EngineIOClient.EngineIOPackage(EngineIOClient.EngineIOPackage.TYPE_PING, "probe"));
    }
    public void connect() {
        engine.connect();
    }

    //TODO: sendEvent
//    public void sendEvent(String eventId, JSONObject obj) {
//        doSendEvent(eventId, obj);
//    }
//
//    private void doSendEvent(String eventId, Object obj) {
//        engine.send(new EngineIOClient.EngineIOPackage(EngineIOClient.EngineIOPackage.TYPE_MESSAGE, obj.toString()));
//    }


    private void handleOpenPackage(EngineIOClient.EngineIOPackage p) throws SocketIoPackageParseException {
        try {
            JSONObject data = new JSONObject(p.data);
            sid = data.getString("sid");
            pingInterval = data.optInt("pingInterval", pingInterval);
            pingTimeout = data.optInt("pingTimeout", pingTimeout);
            timer.schedule(new PingTask(), pingInterval, pingInterval);
        } catch (JSONException e) {
            throw new SocketIoPackageParseException("Error parsing open package", e);
        }
    }

    private class PingTask extends TimerTask {
        @Override
        public void run() {
            doPing();
        }
    }

    private class SocketIOPackage {
        public static final int TYPE_CONNECT = 0;
        public static final int TYPE_DISCONNECT = 1;
        public static final int TYPE_EVENT = 2;
        public static final int TYPE_ACK = 3;
        public static final int TYPE_ERROR = 4;
        public static final int TYPE_BINARY_EVENT = 5;
        public static final int TYPE_BINARY_ACK = 6;

        public int type;
        public String data;

        public SocketIOPackage(int type, String data) {
            this.type = type;
            this.data = data;
        }
    }

    private SocketIOPackage parsePackage(String message) throws SocketIoPackageParseException {
        if (message.length() == 0) {
            throw new SocketIoPackageParseException("Package to parse was empty.");
        }
        char typeChar = message.charAt(0);
        int typeInt = typeChar - '0';
        if (typeInt < 0 || typeInt > 6) {
            throw new SocketIoPackageParseException("Illegal socketIo package type: " + typeInt);
        }
        String payload = message.substring(1);
        return new SocketIOPackage(typeInt, payload);
    }

    public class SocketIoPackageParseException extends Exception {
        public SocketIoPackageParseException(String message) {
            super(message);
        }

        public SocketIoPackageParseException(String detailMessage, Throwable cause) {
            super(detailMessage, cause);
        }
    }


    public interface JsonEventListener<T> {
        public void onEvent(T o);
    }


}
