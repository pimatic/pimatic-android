package org.pimatic.model;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by h3llfire on 14.05.15.
 */
public class GroupManager {

    private static List<UpdateListener> listeners = new ArrayList<>();
    private static List<Group> groups = new ArrayList<Group>();
    private static HashMap<String, Group> groupOfDevice = new HashMap<>();
    private static HashMap<String, Integer> indexOfGroup = new HashMap<>();

    static {
        // bind groupOfDevice mapping
        GroupManager.onChange(new GroupManager.UpdateListener(){
            @Override
            public void onChange() {
                groupOfDevice.clear();
                indexOfGroup.clear();
                int index = 0;
                for(Group g : groups) {
                    List<String> devicesInGroup = g.getDeviceIds();
                    for(String deviceId : devicesInGroup) {
                        groupOfDevice.put(deviceId, g);
                    }
                    indexOfGroup.put(g.getName(), index++);
                }
            }
        });
    }

    public static void updateFromJson(JSONArray GroupArray) throws JSONException {
        groups.clear();
        for (int i = 0; i < GroupArray.length(); i++) {
            JSONObject GroupObj = GroupArray.getJSONObject(i);
            Group page = createGroupFromJson(GroupObj);
            groups.add(page);
        }
        didChange();
    }


    public static Group getGroupById(String id) {
        Integer index = indexOfGroup.get(id);
        if(index == null) {
            return null;
        }
        return groups.get(index);
    }

    public static int getIndexOf(Group g) {
        Integer index = indexOfGroup.get(g.getName());
        Log.v("GroupManager", g.getName() + " " + index);
        if(index == null) {
            return -1;
        } else {
            return index;
        }
    }

    public static Group getGroupOfDevice(Device d) {
        return groupOfDevice.get(d.getId());
    }

    private static Group createGroupFromJson(JSONObject obj) throws JSONException {
        return new Group(obj);
    }

    public static void updateGroupFromJson(JSONObject GroupObject) throws JSONException {
        Group oldGroup = getGroupById(GroupObject.getString("id"));
        if (oldGroup == null) {
            addGroupFromJson(GroupObject);
            return;
        }
        int index = getIndexOf(oldGroup);
        groups.set(index, createGroupFromJson(GroupObject));
        didChange();
    }

    public static void addGroupFromJson(JSONObject GroupObject) throws JSONException {
        Group d = createGroupFromJson(GroupObject);
        groups.add(d);
        didChange();
    }

    public static void removeGroupById(String id) {
        Group oldGroup = getGroupById(id);
        if (oldGroup != null) {
            groups.remove(oldGroup);
            didChange();
        }
    }

    private static void didChange() {
        for (UpdateListener l : listeners) {
            l.onChange();
        }
    }

    public static List<Group> getGroups() {
        return groups;
    }

    public static void setGroups(List<Group> groups) {
        GroupManager.groups = groups;
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
    }


}
