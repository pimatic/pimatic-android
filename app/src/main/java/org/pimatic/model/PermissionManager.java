package org.pimatic.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Hashtable;
import java.util.Iterator;

/**
 * Created by Kevin Schreiber on 25.05.2015.
 */
public class PermissionManager {
    private static Hashtable<String, Permission> permissions = new Hashtable<String, Permission>();

    public static void updateFromJson(JSONObject PermissionsObject) throws JSONException {
        permissions.clear();
        Iterator<?> keys = PermissionsObject.keys();
        while( keys.hasNext() ) {
            String key = (String)keys.next();

            if(PermissionsObject.get(key) instanceof String)
            {
                permissions.put(key, createPermissionFromJson(key, PermissionsObject.getString(key)));
            }
            else if(PermissionsObject.get(key) instanceof Boolean)
            {
                permissions.put(key, createPermissionFromJson(key, PermissionsObject.getBoolean(key)));

            }
        }
    }

    private static Permission createPermissionFromJson(String key, String value) throws JSONException {
        return new Permission(key, value);
    }

    private static Permission createPermissionFromJson(String key, Boolean value) throws JSONException {
        return new Permission(key, value);
    }

    public static Boolean hasValue(String key, String value)
    {
        return permissions.get(key).hasValue(value);
    }

    public static Boolean hasValue(String key, Boolean value)
    {
        return permissions.get(key).hasValue(value);
    }

}
