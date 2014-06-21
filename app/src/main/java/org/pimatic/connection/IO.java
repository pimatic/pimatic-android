package org.pimatic.connection;

import com.github.nkzawa.socketio.client.Socket;
import com.github.nkzawa.socketio.client.Manager;
import com.github.nkzawa.socketio.client.Url;
import com.github.nkzawa.socketio.parser.Parser;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;


public class IO {

    private static final Logger logger = Logger.getLogger(IO.class.getName());

    private static final ConcurrentHashMap<String, Manager> managers = new ConcurrentHashMap<String, Manager>();

    /**
     * Protocol version.
     */
    public static int protocol = Parser.protocol;


    private IO() {}

    public static Socket socket(String uri) throws URISyntaxException {
        return socket(uri, null);
    }

    public static Socket socket(String uri, com.github.nkzawa.socketio.client.IO.Options opts) throws URISyntaxException {
        return socket(new URI(uri), opts);
    }

    public static Socket socket(URI uri) throws URISyntaxException {
        return socket(uri, null);
    }

    public static Manager io;

    /**
     * Initializes a {@link Socket} from an existing {@link Manager} for multiplexing.
     *
     * @param uri uri to connect.
     * @param opts options for socket.
     * @return {@link Socket} instance.
     * @throws URISyntaxException
     */
    public static Socket socket(URI uri, com.github.nkzawa.socketio.client.IO.Options opts) throws URISyntaxException {
        if (opts == null) {
            opts = new com.github.nkzawa.socketio.client.IO.Options();
        }

        URL parsed;
        try {
            parsed = Url.parse(uri);
        } catch (MalformedURLException e) {
            throw new URISyntaxException(uri.toString(), e.getMessage());
        }
        URI source = parsed.toURI();


        if (opts.forceNew || !opts.multiplex) {
            logger.info(String.format("ignoring socket cache for %s", source));
            io = new Manager(source, opts);
        } else {
            String id = Url.extractId(parsed);
            if (!managers.containsKey(id)) {
                logger.info(String.format("new io instance for %s", source));
                managers.putIfAbsent(id, new Manager(source, opts));
            }
            io = managers.get(id);
        }

        return io.socket(parsed.getPath());
    }

}