package com.bearya.robot.household.http;

import com.android.volley.VolleyError;

import org.json.JSONObject;

/**
 * Created by yexifeng on 16/9/17.
 */
public abstract class DefaultListener implements BaseListener<JSONObject>{
    @Override
    public void onErrorResponse(VolleyError error) {

    }
    
}
