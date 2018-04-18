package com.bearya.robot.household.http;

import android.content.Context;
import android.text.TextUtils;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bearya.robot.household.utils.LogUtils;

import org.json.JSONObject;

/**
 * Created by yexifeng on 17/8/23.
 */

public class VolleyRequestQueue {
    private RequestQueue mQueue;

    class MyJsonObjectRequest extends JsonObjectRequest{

        private static final int SOCKET_TIMEOUT = 10000;

        public MyJsonObjectRequest( String url, JSONObject jsonRequest, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
            super(url, jsonRequest, listener, errorListener);
        }

        @Override
        public RetryPolicy getRetryPolicy() {
            RetryPolicy retryPolicy = new DefaultRetryPolicy(SOCKET_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            return retryPolicy;
        }
    }

    public VolleyRequestQueue(Context context){
        mQueue = Volley.newRequestQueue(context);
    }

    public void postRespJsonObject(final String url, JSONObject params, final BaseListener callback) {
        if (TextUtils.isEmpty(url)) {
            LogUtils.e("Error param url is null!");
            return;
        }
        if (!url.startsWith("http")) {
            return;
        }

        JsonObjectRequest request = new MyJsonObjectRequest(url, params, callback, callback);
        mQueue.add(request);
    }

    public void doGet(String url,String param,BaseListener callback){
        doGet(String.format("%s?%s",url, param),callback);
    }
    public void doGet(String url,BaseListener callback){
        StringRequest request = new StringRequest(Request.Method.GET,url,callback,callback);
        mQueue.add(request);
    }

    public void clear() {
        if (mQueue != null) {
            mQueue.cancelAll(new RequestQueue.RequestFilter() {
                @Override
                public boolean apply(Request<?> request) {
                    return true;
                }
            });
        }
        mQueue.stop();
        mQueue = null;
    }

}
