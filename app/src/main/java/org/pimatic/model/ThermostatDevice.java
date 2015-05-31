package org.pimatic.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by h3llfire on 17.05.15.
 */
public class ThermostatDevice extends Device implements Serializable {

    public ThermostatDevice(JSONObject obj) throws JSONException {
        super(obj);
    }

    public String getMode() {
        return ((StringAttribute)getAttribute("mode")).getValue();
    }

    public double getTemperatureSetpoint() {
        return ((NumberAttribute)getAttribute("temperatureSetpoint")).getValue();
    }

    public double getPresetTemp(String type) {
        try {
            return getConfig().getDouble(type + "Temp");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return Double.NaN;
    }


    public <T> T visit(DeviceVisitor<T> v) {
        return v.visitThermostatDevice(this);
    }

}
