package org.pimatic.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by h3llfire on 21.06.14.
 */
public class Device {

    private String name;
    private String id;
    private String template;


    public static Device createDeviceFromJson(JSONObject obj) throws JSONException {
        return new Device(
                obj.getString("name"),
                obj.getString("id"),
                obj.getString("template")
        );
    }

    public Device(String name, String id, String template) {
        this.name = name;
        this.id = id;
        this.template = template;
    }

}
