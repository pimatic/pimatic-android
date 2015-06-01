package org.pimatic.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Kevin Schreiber on 25.05.2015.
 */
public class Permission {

    private String name;
    private String type;
    private boolean booleanValue;
    private String readwriteValue;

    public Permission(String key, String value) {
        name = key;
        type = "String";
        readwriteValue = value;
    }

    public Permission(String key, Boolean value) {
        name = key;
        type = "Boolean";
        booleanValue = value;
    }

    public Boolean hasValue(String value)
    {
        if(type == "String")
            if (readwriteValue == value) return true;
            else return false;
        else
            return false;
    }

    public Boolean hasValue(Boolean value)
    {
        if(type == "Boolean")
            if (booleanValue == value) return true;
            else return false;
        else
            return false;
    }

}
