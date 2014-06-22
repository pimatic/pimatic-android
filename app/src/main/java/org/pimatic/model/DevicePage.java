package org.pimatic.model;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by h3llfire on 21.06.14.
 */
public class DevicePage {

    private String name;
    private String id;
    private ArrayList<Device> devices = new ArrayList<Device>();

    public DevicePage(JSONObject obj) throws JSONException {
        this.id = obj.getString("id");
        this.name = obj.getString("name");

        JSONArray deviceIds = obj.getJSONArray("devices");
        for(int i = 0; i < deviceIds.length(); i++) {
            String deviceId = deviceIds.getJSONObject(i).getString("deviceId");
            Device d = DeviceManager.getDeviceById(deviceId);
            if (d == null) {
                Log.w("DevicePage", "Could not find device: " + deviceId);
            } else {
                devices.add(d);
            }
        }
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }
}
