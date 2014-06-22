package org.pimatic.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by h3llfire on 21.06.14.
 */
public class DeviceManager {

    private static ArrayList<Device> devices = new ArrayList<Device>();

    public static void updateFromJson(JSONArray deviceArray) throws JSONException {
        for(int i = 0; i < deviceArray.length(); i++) {
            JSONObject deviceObj = deviceArray.getJSONObject(i);
            updateOrAddDevice(deviceObj);
        }
    }

    public static Device updateOrAddDevice(JSONObject obj) throws JSONException {
        String id = obj.getString("id");
        Device d = getDeviceById(id);
        if (d == null) {
            d = createDeviceFromJson(obj);
        } else {
            //TODO: Update device
        }
        devices.add(d);
        return d;
    }

    public static Device getDeviceById(String id) {
        for(Device d : devices) {
            if(d.getId().equals(id)) {
                return d;
            }
        }
        return null;
    }


    private static Device createDeviceFromJson(JSONObject obj) throws JSONException {
        String template = obj.getString("template");
        if(template.equals("switch")) {
            return new SwitchDevice(obj);
        } else {
            return new Device(obj);
        }
    }


    public static ArrayList<Device> getDevices() {
        return devices;
    }
}
