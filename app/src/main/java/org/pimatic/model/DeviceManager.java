package org.pimatic.model;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Oliver Schneider <oliverschneider89+sweetpi@gmail.com>
 */
public class DeviceManager {

    private static List<UpdateListener> listeners = new ArrayList<>();
    private static List<Device> devices = new ArrayList<Device>();

    public static void updateFromJson(JSONArray deviceArray) throws JSONException {
        devices.clear();
        for(int i = 0; i < deviceArray.length(); i++) {
            JSONObject deviceObj = deviceArray.getJSONObject(i);
            Device d = createDeviceFromJson(deviceObj);
            devices.add(d);
        }
        didChange();
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
        switch (template) {
            case "switch":
                return new SwitchDevice(obj);
            case "buttons":
                return new ButtonsDevice(obj);
            case "thermostat":
                return new ThermostatDevice(obj);
            default:
                return new Device(obj);
        }
    }

    public static void updateDeviceFromJson(JSONObject deviceObject) throws JSONException {
        Device oldDevice = getDeviceById(deviceObject.getString("id"));
        if(oldDevice == null) {
            addDeviceFromJson(deviceObject);
            return;
        }
        int index = devices.indexOf(oldDevice);
        devices.set(index, createDeviceFromJson(deviceObject));
        didChange();
    }

    public static void addDeviceFromJson(JSONObject deviceObject) throws JSONException {
        Device d = createDeviceFromJson(deviceObject);
        devices.add(d);
        didChange();
    }

    public static void removeDeviceById(String id) {
        Device oldDevice = getDeviceById(id);
        if(oldDevice != null) {
            devices.remove(oldDevice);
        }
        didChange();
    }

    public static void deviceAttributeChanged(String deviceId, String attributeName, int time, Object value) {
        Device d = getDeviceById(deviceId);
        if(d == null) {
            Log.v("DeviceManager", "Device " +  deviceId + " not found");
            return;
        }
        Device.Attribute attr = d.getAttribute(attributeName);
        if(attr == null) {
            Log.v("DeviceManager", "Could not find " + attributeName + " of device " +  deviceId + " not found");
            return;
        }

        if(value == null) {
            attr.setValue(null);
            return;
        }

        if(attr instanceof Device.NumberAttribute) {
            //make sure that value is a number:
            Number numVal = (Number)value;
            attr.setValue(numVal.doubleValue());
        }

        if(attr instanceof Device.StringAttribute) {
            attr.setValue(value.toString());
        }

        if(attr instanceof Device.BooleanAttribute) {
            if(value instanceof Boolean) {
                attr.setValue(value);
            } else {
                Log.e("DeviceManager", "Illegal value for boolean attribute: " + value);
            }
        }


        attributeValueChanged(d, attr);
    }

    private static void attributeValueChanged(Device d, Device.Attribute attr) {
        for (UpdateListener l : listeners ) {
            l.onAttributeValueChange(d, attr);
        }
    }


    private static void didChange() {
        for (UpdateListener l : listeners ) {
            l.onChange();
        }
    }

    public static List<Device> getDevices() {
        return devices;
    }

    public static void setDevices(List<Device> devices) {
        DeviceManager.devices = devices;
        didChange();
    }

    public static void onChange(UpdateListener l) {
        listeners.add(l);
    }

    public static void removeListener(UpdateListener listener) {
        listeners.remove(listener);
    }

    public static abstract class UpdateListener {
        public abstract void onChange();
        public abstract void onAttributeValueChange(Device d, Device.Attribute attr);
    }
}
