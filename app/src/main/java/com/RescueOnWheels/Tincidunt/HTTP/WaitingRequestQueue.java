package com.RescueOnWheels.Tincidunt.HTTP;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class WaitingRequestQueue {
    private static final String TAG = WaitingRequestQueue.class.getSimpleName();
    private final RequestQueue mQueue;

    private boolean ready = true;

    private String baseUrl;

    private Map<String, String> params = new HashMap<>();

    private boolean stopped = false;

    public WaitingRequestQueue(Context context, String baseUrl) {
        mQueue = Volley.newRequestQueue(context);
        mQueue.addRequestFinishedListener(new RequestQueue.RequestFinishedListener<String>() {
            @Override
            public void onRequestFinished(Request<String> request) {
                if (!stopped) {
                    ready = true;
                }
            }
        });
        this.baseUrl = baseUrl;
    }

    public void addRequest(final float pitch, final float yaw) {
        addRequest(pitch, yaw, false);
    }

    private void addRequest(final float pitch, final float yaw, boolean priority) {
        if (priority || (ready && !stopped)) {
            StringRequest req = new StringRequest(Request.Method.GET, buildUrl(pitch, yaw),
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.v(TAG, "ok");
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "That didn't work!");
                }
            }) {
                @Override
                public Map<String, String> getParams() {
                    params.put("pitch", Float.toString(pitch));
                    params.put("yaw", Float.toString(yaw));
                    return params;
                }
            };
            mQueue.add(req);
            ready = false;
        }
    }

    private String buildUrl(float pitch, float yaw) {
        return baseUrl + "/" + pitch + "/" + yaw;
    }

    public void stopAndRecenter() {
        mQueue.cancelAll(new RequestQueue.RequestFilter() {
            @Override
            public boolean apply(Request<?> request) {
                return true;
            }
        });
        addRequest(0, 0, true);
        stopped = true;
    }

    public void start() {
        stopped = false;
        ready = true;
    }
}
