package org.pimatic.connection;

import android.content.Context;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;
import org.pimatic.model.ConnectionOptions;
import org.pimatic.model.Device;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class RestClient {
    private final RequestQueue queue;
    private final String baseUrl;
    private final Map<String, String> headers;

    public RestClient(Context context, ConnectionOptions conOpts) {
        this.queue = Volley.newRequestQueue(context);
        this.baseUrl = conOpts.getBaseUrl();
        this.headers = buildHeaders(conOpts);
    }

    private Map<String, String> buildHeaders(ConnectionOptions conOpts) {
        HashMap<String, String> params = new HashMap<String, String>();
        String creds = String.format("%s:%s", conOpts.username, conOpts.password);
        String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.NO_WRAP);
        params.put("Authorization", auth);
        return params;
    }

    public JsonObjectRequest createJsonRequest(final int method, final String url,
                                               final JSONObject params,
                                               final Response.Listener<JSONObject> onResponse,
                                               final Response.ErrorListener onError) {
        String fullUrl = baseUrl + url;
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
        String queryString = UrlQueryString.buildQueryString(params);
        String fullUrl = baseUrl + url + (queryString.length() > 0 ? "?" + queryString : "");
        JsonObjectRequest request = new JsonObjectRequest(method, fullUrl, onResponse, onError) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return headers;
            }

            @Override
            public Map<String, String> getParams() {
                Log.v("getParams", params.toString());
                return params;
            }

        };
        request.setShouldCache(false);
        return request;
    }

    public JsonRequest<JSONObject> getMessages() {
        HashMap<String, String> params = new HashMap<String, String>();
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

    public JsonRequest<JSONObject> login(String username, String password,
                                         Response.Listener<JSONObject> onResponse,
                                         Response.ErrorListener onError) {
        JSONObject params = new JSONObject();
        try {
            params.put("username", username);
            params.put("password", password);
        } catch (Exception e) {
            e.printStackTrace();
        }
        JsonRequest<JSONObject> request = createJsonRequest(Request.Method.POST, "/login", params, onResponse, onError);
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

    public static abstract class ErrorListener implements Response.ErrorListener {
        @Override
        public void onErrorResponse(VolleyError volleyError) {
            if (volleyError.networkResponse != null && volleyError.networkResponse.data != null) {
                try {
                    String responseBody = new String(volleyError.networkResponse.data, "utf-8");
                    JSONObject jsonObject = new JSONObject(responseBody);
                    onJsonResult(volleyError, jsonObject);
                    return;
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException error) {
                    error.printStackTrace();
                }
            }
            onNetworkError(volleyError);
        }

        public abstract void onNetworkError(VolleyError error);

        public abstract void onJsonResult(VolleyError error, JSONObject errorResult);
    }

    public static class ErrorToater extends ErrorListener {
        Context context;

        public ErrorToater(Context context) {
            this.context = context;
        }

        @Override
        public void onNetworkError(VolleyError error) {
            showToast(error.getLocalizedMessage());
        }

        @Override
        public void onJsonResult(VolleyError error, JSONObject errorResult) {
            String message = error.getLocalizedMessage();
            try {
                message = errorResult.getString("message");
            } catch (JSONException e) {
            }
            showToast(message);
        }

        protected void showToast(String message) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }

}

class UrlQueryString {
    private static final String DEFAULT_ENCODING = "UTF-8";

    public static String buildQueryString(final Map<String, String> map) {
        try {
            final Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
            final StringBuilder sb = new StringBuilder(map.size() * 8);
            while (it.hasNext()) {
                final Map.Entry<String, String> entry = it.next();
                final String key = entry.getKey();
                if (key != null) {
                    sb.append(URLEncoder.encode(key, DEFAULT_ENCODING));
                    sb.append("=");
                    final String value = entry.getValue();
                    final String valueAsString = value != null ? URLEncoder.encode(value, DEFAULT_ENCODING) : "";
                    sb.append(valueAsString);
                    if (it.hasNext()) {
                        sb.append("&");
                    }
                } else {
                    // Do what you want...for example:
                    Log.e("RestClient", String.format("Null key in query map: %s", map.entrySet()));
                }
            }
            return sb.toString();
        } catch (final UnsupportedEncodingException e) {
            throw new UnsupportedOperationException(e);
        }
    }

}