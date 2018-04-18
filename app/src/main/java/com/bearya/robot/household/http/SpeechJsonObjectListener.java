package com.bearya.robot.household.http;

import android.text.TextUtils;

import com.bearya.robot.household.utils.LogUtils;

import org.json.JSONObject;

/**
 * Created by xifengye on 2016/12/12.
 */

public abstract class SpeechJsonObjectListener extends DefaultListener{
    @Override
    public void onResponse(JSONObject response) {
        LogUtils.e("请求返回:%s"+response.toString());
        int code = response.optInt("code");
        switch (code){
            case 40001:
            case 40002:
            case 40004:
            case 40007:{
                onError(code,response.optString("text"));
                break;
            }

            default:
            {
                String tts = response.optString("text");
                String url = response.optString("url");
                if(!TextUtils.isEmpty(url)){
                    onResponse(tts,url);
                }else {
                    onResponse(tts);
                }
                break;
            }
        }
    }

    public abstract void onResponse(String tts);
    public abstract void onResponse(String tts,String url);

    /**
     * 40001	参数key错误
     40002	请求内容info为空
     40004	当天请求次数已使用完
     40007	数据格式异常
     * @param code
     */
    public abstract void onError(int code,String msg);
}
