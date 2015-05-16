package org.pimatic.connection;

import android.app.Activity;
import android.util.Base64;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;
import org.pimatic.app.MainActivity;
import org.pimatic.model.ConnectionOptions;
import org.pimatic.model.Device;

import java.util.HashMap;
import java.util.Map;

public class RestClient {
    private final RequestQueue queue;
    private final String baseUrl;
    private final Map<String, String> headers;

    public RestClient(Activity mainActivity, ConnectionOptions conOpts) {
        this.queue = Volley.newRequestQueue(mainActivity);
        this.baseUrl = conOpts.getBaseUrl();
        this.headers = buildHeaders(conOpts);
    }

    private Map<String, String> buildHeaders(ConnectionOptions conOpts) {
        HashMap<String, String> params = new HashMap<String, String>();
        String creds = String.format("%s:%s",conOpts.username, conOpts.password);
        String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.NO_WRAP);
        params.put("Authorization", auth);
        return params;
    }

    public JsonObjectRequest createJsonRequest(final int method, final String url,
                                               final JSONObject params,
                                               final Response.Listener<JSONObject> onResponse,
                                               final Response.ErrorListener onError) {
        String fullUrl = baseUrl  + url;
        JsonObjectRequest request = new JsonObjectRequest(method, fullUrl, params, onResponse, onError) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return headers;
            }

        };
        request.setShouldCache(false);
        return request;
    }

    public JsonRequest<JSONObject> createParamRequest(final int method, final String url,
                                           final Map<String, String> params,
                                           final Response.Listener<JSONObject> onResponse,
                                           final Response.ErrorListener onError) {
        String fullUrl = baseUrl  + url;
        JsonObjectRequest request = new JsonObjectRequest(method, fullUrl, onResponse, onError) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return headers;
            }

            @Override
            public Map<String, String> getParams() {
                return params;
            }

        };
        request.setShouldCache(false);
        return request;
    }

    public JsonRequest<JSONObject> getMessages() {
        HashMap<String, String> params = new HashMap<String, String>();;
        JsonRequest<JSONObject> request = createParamRequest(Request.Method.GET, "/api/database/messages", params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                Log.v("RestClient", jsonObject.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e("RestClient", volleyError.getMessage());
            }
        });
        queue.add(request);
        return request;
    }

    public JsonRequest<JSONObject> callDeviceAction(Device device, String actionName, Map<String, String> params,
                                              Response.Listener<JSONObject> onResponse,
                                              Response.ErrorListener onError) {

        JsonRequest<JSONObject> request = createParamRequest(
                Request.Method.GET,
                "/api/device/" + device.getId() + "/" + actionName,
                params,
                onResponse,
                onError
        );
        queue.add(request);
        return request;
    }
}