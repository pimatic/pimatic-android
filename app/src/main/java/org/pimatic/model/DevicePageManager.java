package org.pimatic.model;

/**
 * Created by h3llfire on 13.05.15.
 */

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.pimatic.helpers.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by h3llfire on 21.06.14.
 */
public class DevicePageManager {

    private static List<UpdateListener> listeners = new ArrayList<>();
    private static List<DevicePage> pages = new ArrayList<DevicePage>();

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
        Assert.isMainThread();
        for (UpdateListener l : listeners ) {
            l.onChange();
        }
    }

    public static List<DevicePage> getDevicePages() {
        return pages;
    }

    public static void onChange(UpdateListener l) {
        Assert.isMainThread();
        listeners.add(l);
    }

    public static void removeListener(UpdateListener listener) {
        Assert.isMainThread();
        listeners.remove(listener);
    }

    public static void setPages(List<DevicePage> pages) {
        DevicePageManager.pages = pages;
        didChange();
    }

    public static abstract class UpdateListener {
        public abstract void onChange();
    }
}
