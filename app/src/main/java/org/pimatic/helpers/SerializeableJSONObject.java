package org.pimatic.helpers;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by Oliver Schneider <oliverschneider89+sweetpi@gmail.com>
 */
public class SerializeableJSONObject implements Serializable {
    private JSONObject obj;

    public SerializeableJSONObject(JSONObject obj) {
        this.obj = obj;
    }

    public void set(JSONObject obj) {
        this.obj = obj;
    }

    public JSONObject get() {
        return this.obj;
    }

    private void writeObject(ObjectOutputStream oos)
            throws IOException {
        oos.writeUTF(obj != null ? obj.toString() : "");
    }

    private void readObject(ObjectInputStream ois)
            throws ClassNotFoundException, IOException {
        String json = ois.readUTF();
        if (json.length() > 0) {
            try {
                this.obj = new JSONObject(json);
            } catch (JSONException e) {
                throw new IOException(e);
            }
        }
    }

}
