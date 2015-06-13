package org.pimatic.model;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.pimatic.helpers.SerializeableJSONObject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Oliver Schneider <oliverschneider89+sweetpi@gmail.com>
 */
public class Device implements Serializable {

    private String name;
    private String id;
    private String template;
    private SerializeableJSONObject config;

    private ArrayList<Attribute> attributes;

    public Device(JSONObject obj) throws JSONException {
        this(obj.getString("name"), obj.getString("id"), obj.getString("template"), obj.getJSONObject("config"));
        this.attributes = new ArrayList<Attribute>();
        JSONArray attrs = obj.getJSONArray("attributes");
        for(int i = 0; i < attrs.length(); i++) {
            addAttribute(attrs.getJSONObject(i));
        }
    }

    public Device(String name, String id, String template, JSONObject config) {
        this.name = name;
        this.id = id;
        this.template = template;
        this.config = new SerializeableJSONObject(config);
    }

    public static JSONObject getJsonAttribute(JSONObject deviceObj, String name) throws JSONException {
        JSONArray attrs = deviceObj.getJSONArray("attributes");
        for (int i = 0; i < attrs.length(); i++) {
            JSONObject attr = attrs.getJSONObject(i);
            if (attr.getString("name").equals(name)) {
                return attr;
            }
        }
        return null;
    }

    private void addAttribute(JSONObject attr) throws JSONException {
        Attribute a = null;
        String type = attr.getString("type");
        if(type.equals("boolean")) {
            a = new BooleanAttribute(attr);
        } else if(type.equals("string")) {
            a = new StringAttribute(attr);
        }else if(type.equals("number")) {
            a = new NumberAttribute(attr);
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

    public String getTemplate() {
        return template;
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

    public JSONObject getConfig() {
        return config.get();
    }

    public abstract class Attribute<T> implements Serializable {
        protected String name;
        protected String acronym;
        protected String label;
        protected boolean hidden;

        protected T value;

        public Attribute(JSONObject attr, T value) throws JSONException {
            this.name = attr.getString("name");
            this.acronym = attr.optString("acronym");
            this.label = attr.optString("label");
            this.hidden = attr.optBoolean("hidden");
            this.value = value;
        }
        public T getValue() {
            return value;
        }

        public void setValue(T value) {
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public abstract String getFormatedValue();

        public String getAcronym() {
            return acronym;
        }

        public String getLabel() {
            return label;
        }

        public boolean isHidden() {
            return hidden;
        }
    }

    public class BooleanAttribute extends Attribute<Boolean> implements Serializable {
        protected String[] labels = {"false", "true"};
        
        public BooleanAttribute(JSONObject attr) throws JSONException {
            super(attr, attr.optBoolean("value", false));
            JSONArray labels = attr.optJSONArray("labels");
            if(labels != null && labels.length() == 2) {
                this.labels[0] = labels.optString(0, "false");
                this.labels[1] = labels.optString(1, "true");
            }
        }

        @Override
        public String getFormatedValue() {
            return (value ? labels[0] : labels[1]);
        }
    }

    public class StringAttribute extends Attribute<String> implements Serializable {
        public StringAttribute(JSONObject attr) throws JSONException {
            super(attr, attr.optString("value", "Unknown"));
        }

        @Override
        public String getFormatedValue() {
            return value;
        }
    }

    public class NumberAttribute extends Attribute<Double> implements Serializable {
        private String unit;

        public NumberAttribute(JSONObject attr) throws JSONException {
            super(attr, attr.optDouble("value", Double.NaN));
            this.unit = attr.optString("unit", "");
        }

        @Override
        public String getFormatedValue() {
            if(value == null || value.isNaN()) {
                return "unknown";
            }
            double roundedValue = Math.round(value * 1e2) / 1e2;
            return "" + roundedValue;
        }

        public String getUnit() {
            return unit;
        }
    }

}
