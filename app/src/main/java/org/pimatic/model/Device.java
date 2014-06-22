package org.pimatic.model;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by h3llfire on 21.06.14.
 */
public class Device {

    public abstract class Attribute<T> {
        private String name;
        protected T value;

        public Attribute(JSONObject attr, T value) throws JSONException {
            this.name = attr.getString("name");
            this.value = value;
        }
        public T getValue() {
            return value;
        }

        public String getName() {
            return name;
        }

        public abstract String getFormatedValue();
    }

    public class BooleanAttribute extends Attribute<Boolean> {
        public BooleanAttribute(JSONObject attr) throws JSONException {
            super(attr, attr.optBoolean("value", false));
        }

        @Override
        public String getFormatedValue() {
            return value.toString();
        }
    }

    public class StringAttribute extends Attribute<String> {
        public StringAttribute(JSONObject attr) throws JSONException {
            super(attr, attr.optString("value", "Unknown"));
        }

        @Override
        public String getFormatedValue() {
            return value;
        }
    }


    private String name;
    private String id;
    private String template;
    private ArrayList<Attribute> attributes;

    public Device(JSONObject obj) throws JSONException {
        this(obj.getString("name"), obj.getString("id"), obj.getString("id"));
        this.attributes = new ArrayList<Attribute>();
        JSONArray attrs = obj.getJSONArray("attributes");
        for(int i = 0; i < attrs.length(); i++) {
            addAttribute(attrs.getJSONObject(i));
        }
    }

    public Device(String name, String id, String template) {
        this.name = name;
        this.id = id;
        this.template = template;
    }

    private void addAttribute(JSONObject attr) throws JSONException {
        Attribute a = null;
        String type = attr.getString("type");
        if(type.equals("boolean")) {
            a = new BooleanAttribute(attr);
        } else if(type.equals("string")) {
            a = new StringAttribute(attr);
        }
        if (a == null) {
            Log.e("Device", "Unhandled attribute type");
        } else {
            this.attributes.add(a);
        }
    }

    public String getId() {
        return id;
    }

    public String toString() {
        return "Device: " + id;
    }

    public String getName() {
        return name;
    }

    public static JSONObject getJsonAttribute(JSONObject deviceObj, String name) throws JSONException {
        JSONArray attrs = deviceObj.getJSONArray("attributes");
        for(int i = 0; i < attrs.length(); i++) {
            JSONObject attr = attrs.getJSONObject(i);
            if(attr.getString("name").equals(name)) {
                return attr;
            }
        }
        return null;
    }

    public Attribute<?> getAttribute(String name) {
        for(Attribute a : attributes) {
            if(a.getName().equals(name)) {
                return a;
            }
        }
        return null;
    }

    public ArrayList<Attribute> getAttributes() {
        return attributes;
    }


    public <T> T visit(DeviceVisitor<T> v) {
        return v.visitDevice(this);
    }

}
