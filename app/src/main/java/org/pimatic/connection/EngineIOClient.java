package org.pimatic.connection;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

/**
* Created by h3llfire on 29.06.14.
*/
abstract class EngineIOClient {

    private WebSocketClient ws;

    public EngineIOClient(String host, int port) {

        Map<String, String> headers = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
        try {
            ws = new WebSocketClient(new URI("ws://"+host+":"+port+"/socket.io/?EIO=2&transport=websocket"), new Draft_17(), headers, 0) {
                @Override
                public void onOpen(final ServerHandshake serverHandshake) {
                    Log.i("Websocket", "Open");
                    Map<String, String> headers = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
                    Iterator<String> it = serverHandshake.iterateHttpFields();
                    while (it.hasNext()) {
                        String field = it.next();
                        if (field == null) continue;
                        String val = serverHandshake.getFieldValue(field);
                        Log.i("Websocket", "Header field " + field + ": " + val);
                        //headers.put(field, );
                    }
                }

                @Override
                public void onClose(int i, String s, boolean b) {
                    Log.i("Websocket", "Closed " + s + i);
                    EngineIOClient.this.onClose();
                }

                @Override
                public void onMessage(String rawMsg) {
                    Log.i("Websocket", "Message: " + rawMsg);
                    EngineIOPackage enginePackage = null;
                    try {
                        enginePackage = EngineIOClient.this.parsePackage(rawMsg);
                    } catch (EngineIOClient.EngineIoPackageParseException e) {
                        EngineIOClient.this.onError(e);
                        return;
                    }
                    switch (enginePackage.type) {
                        case EngineIOPackage.TYPE_OPEN:
                            EngineIOClient.this.onOpen(enginePackage);
                            break;
                        case EngineIOPackage.TYPE_CLOSE:
                            ws.close();
                            break;
                        case EngineIOPackage.TYPE_NOOP:
                            break;
                        case EngineIOPackage.TYPE_MESSAGE:
                            EngineIOClient.this.onMessage(enginePackage);
                            break;
                        case EngineIOPackage.TYPE_PONG:
                            /* ignore pong package*/
                            break;
                        default:
                            Log.w("Websocket", "Uknown engine io package type: " + enginePackage.type);
                    }

                }

                @Override
                public void onError(final Exception e) {
                    Log.i("Websocket", "Error " + e.getMessage());
                    EngineIOClient.this.onError(e);
                }


            };
        } catch (URISyntaxException e) {
            EngineIOClient.this.onError(e);
        }
    }

    public abstract void onMessage(EngineIOPackage enginePackage);
    public abstract void onOpen(EngineIOPackage enginePackage);
    public abstract void onError(Exception e);
    public abstract void onClose();

    public void connect() {
        ws.connect();
    }

    public void send(EngineIOPackage p) {
        ws.send(""+p.type+p.data);
    }


    public EngineIOPackage parsePackage(String message) throws EngineIoPackageParseException {
        if (message.length() == 0) {
            throw new EngineIoPackageParseException("Package to parse was empty.");
        }
        char typeChar = message.charAt(0);
        int typeInt = typeChar - '0';
        if (typeInt < 0 || typeInt > 6) {
            throw new EngineIoPackageParseException("Illegal engineIO package type: " + typeInt);
        }
        String payload = message.substring(1);
        return new EngineIOPackage(typeInt, payload);
    }

    public class EngineIoPackageParseException extends Exception {
        public EngineIoPackageParseException(String message) {
            super(message);
        }

        public EngineIoPackageParseException(String detailMessage, Throwable cause) {
            super(detailMessage, cause);
        }
    }

    public static class EngineIOPackage {
        public static final int TYPE_OPEN = 0;
        public static final int TYPE_CLOSE = 1;
        public static final int TYPE_PING = 2;
        public static final int TYPE_PONG = 3;
        public static final int TYPE_MESSAGE = 4;
        public static final int TYPE_UPGRADE = 5;
        public static final int TYPE_NOOP = 6;

        public int type;
        public String data;

        public EngineIOPackage(int type, String data) {
            this.type = type;
            this.data = data;
        }

    }
}
