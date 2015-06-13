package org.pimatic.model;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Oliver Schneider <oliverschneider89+sweetpi@gmail.com>
 */
public class GroupManager extends UpdateEventEmitter<GroupManager.UpdateListener>{

    private List<Group> groups = new ArrayList<Group>();
    private HashMap<String, Group> groupOfDevice = new HashMap<>();
    private HashMap<String, Integer> indexOfGroup = new HashMap<>();

    private static GroupManager instance;
    public static GroupManager getInstance() {
        if(instance == null) {
            instance = new GroupManager();
        }
        return instance;
    }

    private GroupManager() {
        // bind groupOfDevice mapping
        onChange(new GroupManager.UpdateListener(){
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

    public void updateFromJson(JSONArray GroupArray) throws JSONException {
        groups.clear();
        for (int i = 0; i < GroupArray.length(); i++) {
            JSONObject GroupObj = GroupArray.getJSONObject(i);
            Group page = createGroupFromJson(GroupObj);
            groups.add(page);
        }
        didChange();
    }


    public Group getGroupById(String id) {
        Integer index = indexOfGroup.get(id);
        if(index == null) {
            return null;
        }
        return groups.get(index);
    }

    public int getIndexOf(Group g) {
        Integer index = indexOfGroup.get(g.getName());
        Log.v("GroupManager", g.getName() + " " + index);
        if(index == null) {
            return -1;
        } else {
            return index;
        }
    }

    public Group getGroupOfDevice(Device d) {
        return groupOfDevice.get(d.getId());
    }

    private Group createGroupFromJson(JSONObject obj) throws JSONException {
        return new Group(obj);
    }

    public void updateGroupFromJson(JSONObject GroupObject) throws JSONException {
        Group oldGroup = getGroupById(GroupObject.getString("id"));
        if (oldGroup == null) {
            addGroupFromJson(GroupObject);
            return;
        }
        int index = getIndexOf(oldGroup);
        groups.set(index, createGroupFromJson(GroupObject));
        didChange();
    }

    public void addGroupFromJson(JSONObject GroupObject) throws JSONException {
        Group d = createGroupFromJson(GroupObject);
        groups.add(d);
        didChange();
    }

    public void removeGroupById(String id) {
        Group oldGroup = getGroupById(id);
        if (oldGroup != null) {
            groups.remove(oldGroup);
            didChange();
        }
    }


    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
        didChange();
    }

    public interface UpdateListener extends UpdateEventEmitter.UpdateListener {
        void onChange();
    }


}
