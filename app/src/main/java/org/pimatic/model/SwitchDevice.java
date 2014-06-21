package org.pimatic.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by h3llfire on 21.06.14.
 */
public class SwitchDevice extends Device {

    public SwitchDevice(JSONObject obj) throws JSONException {
        super(obj);
        boolean state = Device.getJsonAttribute(obj, "state").optBoolean("value", false);
    }

    public boolean getState() {
        return ((Device.Attribute<Boolean>)getAttribute("state")).getValue();
    }

    public <T> T visit(DeviceVisitor<T> v) {
        return v.visitSwitchDevice(this);
    }
}
