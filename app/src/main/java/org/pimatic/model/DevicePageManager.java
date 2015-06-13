package org.pimatic.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.pimatic.helpers.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Oliver Schneider <oliverschneider89+sweetpi@gmail.com>
 */
public class DevicePageManager extends UpdateEventEmitter<DevicePageManager.UpdateListener> {

    private List<DevicePage> pages = new ArrayList<DevicePage>();

    private static DevicePageManager instance;
    public static DevicePageManager getInstance() {
        if(instance == null) {
            instance = new DevicePageManager();
        }
        return instance;
    }

    private DevicePageManager() {

    }

    public void updateFromJson(JSONArray DevicePageArray) throws JSONException {
        pages.clear();
        for(int i = 0; i < DevicePageArray.length(); i++) {
            JSONObject DevicePageObj = DevicePageArray.getJSONObject(i);
            DevicePage page = createDevicePageFromJson(DevicePageObj);
            pages.add(page);
        }
        didChange();
    }


    public DevicePage getDevicePageById(String id) {
        for(DevicePage d : pages) {
            if(d.getId().equals(id)) {
                return d;
            }
        }
        return null;
    }


    private DevicePage createDevicePageFromJson(JSONObject obj) throws JSONException {
        return new DevicePage(obj);
    }

    public void updateDevicePageFromJson(JSONObject DevicePageObject) throws JSONException {
        DevicePage oldDevicePage = getDevicePageById(DevicePageObject.getString("id"));
        if(oldDevicePage == null) {
            addDevicePageFromJson(DevicePageObject);
            return;
        }
        int index = pages.indexOf(oldDevicePage);
        pages.set(index, createDevicePageFromJson(DevicePageObject));
        didChange();
    }

    public void addDevicePageFromJson(JSONObject DevicePageObject) throws JSONException {
        DevicePage d = createDevicePageFromJson(DevicePageObject);
        pages.add(d);
        didChange();
    }

    public void removeDevicePageById(String id) {
        DevicePage oldDevicePage = getDevicePageById(id);
        if(oldDevicePage != null) {
            pages.remove(oldDevicePage);
        }
        didChange();
    }



    public List<DevicePage> getDevicePages() {
        return pages;
    }


    public void setPages(List<DevicePage> pages) {
        this.pages = pages;
        didChange();
    }

    public interface UpdateListener extends UpdateEventEmitter.UpdateListener {
        void onChange();
    }
}
