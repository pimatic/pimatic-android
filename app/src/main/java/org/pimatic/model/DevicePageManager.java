package org.pimatic.model;

/**
 * Created by h3llfire on 13.05.15.
 */

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by h3llfire on 21.06.14.
 */
public class DevicePageManager {

    private static ArrayList<UpdateListener> listeners = new ArrayList<>();
    private static ArrayList<DevicePage> pages = new ArrayList<DevicePage>();

    public static void updateFromJson(JSONArray DevicePageArray) throws JSONException {
        pages.clear();
        for(int i = 0; i < DevicePageArray.length(); i++) {
            JSONObject DevicePageObj = DevicePageArray.getJSONObject(i);
            DevicePage page = createDevicePageFromJson(DevicePageObj);
            pages.add(page);
        }
        didChange();
    }


    public static DevicePage getDevicePageById(String id) {
        for(DevicePage d : pages) {
            if(d.getId().equals(id)) {
                return d;
            }
        }
        return null;
    }


    private static DevicePage createDevicePageFromJson(JSONObject obj) throws JSONException {
        return new DevicePage(obj);
    }

    public static void updateDevicePageFromJson(JSONObject DevicePageObject) throws JSONException {
        DevicePage oldDevicePage = getDevicePageById(DevicePageObject.getString("id"));
        if(oldDevicePage == null) {
            addDevicePageFromJson(DevicePageObject);
            return;
        }
        int index = pages.indexOf(oldDevicePage);
        pages.set(index, createDevicePageFromJson(DevicePageObject));
        didChange();
    }

    public static void addDevicePageFromJson(JSONObject DevicePageObject) throws JSONException {
        DevicePage d = createDevicePageFromJson(DevicePageObject);
        pages.add(d);
        didChange();
    }

    public static void removeDevicePageById(String id) {
        DevicePage oldDevicePage = getDevicePageById(id);
        if(oldDevicePage != null) {
            pages.remove(oldDevicePage);
        }
        didChange();
    }

    private static void didChange() {
        for (UpdateListener l : listeners ) {
            l.onChange();
        }
    }

    public static ArrayList<DevicePage> getDevicePages() {
        return pages;
    }

    public static void onChange(UpdateListener l) {
        listeners.add(l);
    }

    public static abstract class UpdateListener {
        public abstract void onChange();
    }
}
