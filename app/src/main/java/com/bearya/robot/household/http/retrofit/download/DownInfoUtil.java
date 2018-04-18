package com.bearya.robot.household.http.retrofit.download;

import android.content.Context;
import android.text.TextUtils;

import com.bearya.robot.household.MyApplication;
import com.bearya.robot.household.utils.SharedPrefUtil;
import com.google.gson.Gson;

/**
 * 断点续传
 * 数据库工具类-geendao运用
 * Created by WZG on 2016/10/25.
 */

public class DownInfoUtil {
    private static DownInfoUtil instance;
    private Context context;


    public DownInfoUtil() {
        context= MyApplication.getContext();
    }


    /**
     * 获取单例
     * @return
     */
    public static DownInfoUtil getInstance() {
        if (instance == null) {
            synchronized (DownInfoUtil.class) {
                if (instance == null) {
                    instance = new DownInfoUtil();
                }
            }
        }
        return instance;
    }

    public void save(DownloadInfo info){
        SharedPrefUtil.getInstance(context).put(info.getUrl(), new Gson().toJson(info));
    }

    public void update(DownloadInfo info){
        SharedPrefUtil.getInstance(context).put(info.getUrl(), new Gson().toJson(info));
    }

    public void deleteDowninfo(DownloadInfo info){
        SharedPrefUtil.getInstance(context).remove(info.getUrl());
    }


    public DownloadInfo query(String url) {
        String json = SharedPrefUtil.getInstance(context).getString(url);
        if (TextUtils.isEmpty(json)) {
            return null;
        }
        return new Gson().fromJson(json, DownloadInfo.class);
    }
}
