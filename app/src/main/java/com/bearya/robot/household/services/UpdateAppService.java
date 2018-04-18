package com.bearya.robot.household.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.bearya.robot.household.MyApplication;
import com.bearya.robot.household.entity.VersionInfo;
import com.bearya.robot.household.utils.LogUtils;
import com.bearya.robot.household.views.UpdateDialog;
import com.google.gson.Gson;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import okhttp3.Call;

/**
 * Created by Qiujz on 2017/12/9.
 */

public class UpdateAppService extends Service {
    private final static String TAG = "UpdateAppService";
    private VersionInfo mVersionInfo;
    private UpdateDialog updateDialog;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.d(TAG, "onStartCommand");
        checkAppVersion();
        return START_REDELIVER_INTENT;
    }

    public void checkAppVersion() {
        String url = "https://api.bearya.com/v1/source/apk/update?device=3";
        OkHttpUtils.get()
                .url(url)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int i) {
                        LogUtils.d(TAG, "checkAppVersion onError:"+e.toString());
                    }

                    @Override
                    public void onResponse(String s, int i) {
                        LogUtils.d(TAG, "checkAppVersion:"+s);
                        try {
                            JSONObject jsonObject = new JSONObject(s);
                            if ((int)jsonObject.get("status") != 1) {
                                return;
                            }
                            JSONObject list = jsonObject.getJSONObject("list");
                            if (list == null) {
                                return;
                            }
                            JSONArray apks = list.getJSONArray("apk");
                            if (apks == null || apks.length() < 0) {
                                return;
                            }
                            JSONObject apk = apks.getJSONObject(0);
                            Gson gson = new Gson();
                            mVersionInfo = gson.fromJson(apk.toString(), VersionInfo.class);
                            LogUtils.d(TAG, "checkAppVersion --> versionInfo.download_url = "+mVersionInfo.download_url);
                            //showUpdateDialog(mVersionInfo);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void showUpdateDialog(VersionInfo versionInfo) {
        updateDialog = new UpdateDialog();
        LogUtils.d(TAG, "checkAppVersion --> showUpdateDialog ");
        updateDialog.createDialog(MyApplication.getContext()).setMessage("检查到新版本\n是否更新？").showDialog();
    }

}
