package org.pimatic.model;

import android.content.SharedPreferences;
import android.content.res.Resources;

import org.pimatic.app.R;

/**
 * Created by h3llfire on 15.05.15.
 */
public class ConnectionOptions {
    public String protocol;
    public String host;
    public int port;
    public String username;
    public String password;


    public String getBaseUrl() {
        return protocol + "://" + host + ":" + port;
    }

    public String getBaseUrlWithLogin() {
        return protocol + "://" + host + ":" + port;
    }

    public static ConnectionOptions fromSettings(Resources res, SharedPreferences settings) {
        ConnectionOptions conOpts = new ConnectionOptions();
        conOpts.protocol = settings.getString("protocol", res.getString(R.string.default_protocol));
        conOpts.host = settings.getString("host", res.getString(R.string.default_host));
        conOpts.port = settings.getInt("port", res.getInteger(R.integer.default_port));
        conOpts.username = settings.getString("username", res.getString(R.string.default_username));
        conOpts.password = settings.getString("password", res.getString(R.string.default_password));
        return conOpts;
    }

}


