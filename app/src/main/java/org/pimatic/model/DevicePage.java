package org.pimatic.model;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by h3llfire on 21.06.14.
 */
public class DevicePage {

    private String name;
    private String id;
    private ArrayList<String> deviceIds = new ArrayList<>();

    public DevicePage(JSONObject obj) throws JSONException {
        this.id = obj.getString("id");
        this.name = obj.getString("name");
        JSONArray deviceIdsArray = obj.getJSONArray("devices");
        for (int i = 0; i < deviceIdsArray.length(); i++) {
            String deviceId = deviceIdsArray.getJSONObject(i).getString("deviceId");
            deviceIds.add(deviceId);
        }
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public List<Device> getDevices() {
        ArrayList<Device> devices = new ArrayList<>();
        for (int i = 0; i < deviceIds.size(); i++) {
            String deviceId = deviceIds.get(i);
            Device d = DeviceManager.getDeviceById(deviceId);
            if (d == null) {
                Log.w("DevicePage", "Could not find device: " + deviceId);
            } else {
                devices.add(d);
            }
        }
        return devices;
    }

    public List<GroupDevicePair> getDevicesInGroups() {
        List<Device> devices = getDevices();
        //Log.w("DevicePage", "Devices count: " + devices.size());
        List<GroupDevicePair> pairs = new ArrayList<>();
        for(Device d : devices) {
            Group g = GroupManager.getGroupOfDevice(d);
            //find pair
            GroupDevicePair pair = null;
            for(GroupDevicePair p : pairs) {
                if(p.group == g) {
                    pair = p;
                    break;
                }
            }
            if(pair != null) {
                pair.devices.add(d);
            } else {
                pair = new GroupDevicePair();
                pair.group = g;
                pair.devices = new ArrayList<>();
                pair.devices.add(d);
                pairs.add(pair);
            }
        }

        //order pairs by group order:
        Collections.sort(pairs);

        return pairs;
    }

    public static class GroupDevicePair implements Comparable<GroupDevicePair> {
        public Group group;
        public List<Device> devices;

        @Override
        public int compareTo(GroupDevicePair another) {
            if(this.group == null) {
                return 1;
            }
            if(another.group == null) {
                return -1;
            }

            return GroupManager.getIndexOf(this.group) - GroupManager.getIndexOf(another.group);
        }
    }

}
