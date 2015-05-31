package org.pimatic.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by h3llfire on 21.06.14.
 */
public class SwitchDevice extends Device implements Serializable {

    public SwitchDevice(JSONObject obj) throws JSONException {
        super(obj);
    }

    public boolean getState() {
        return ((Device.Attribute<Boolean>)getAttribute("state")).getValue();
    }

    public <T> T visit(DeviceVisitor<T> v) {
        return v.visitSwitchDevice(this);
    }
}
