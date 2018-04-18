package com.bearya.robot.household.http;

import com.google.gson.Gson;

import org.json.JSONObject;

/**
 * Created by yexifeng on 17/1/16.
 */

public abstract class CheckUpdateJsonObjectListener extends DefaultListener {

    @Override
    public void onResponse(JSONObject response) {
//        CheckUpdateResponse checkUpdateResponse =  JsonUtil.parseJavaBean(CheckUpdateResponse.class,response);
//        onCheckResp(checkUpdateResponse);
        CheckUpdateResponse checkUpdateResponse = null;
        try{
            checkUpdateResponse = new Gson().fromJson(response.toString(), CheckUpdateResponse.class);
        }catch (Exception e){}
        onCheckResp(checkUpdateResponse);
    }

    public abstract void onCheckResp(CheckUpdateResponse response);
}
