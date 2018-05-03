package com.bearya.robot.household;

import android.app.Application;
import android.content.Context;

import com.bearya.robot.household.networkInteraction.FamilyInteraction;
import com.tencent.bugly.crashreport.CrashReport;

/**
 * Created by qiujz on 2017/5/28.
 */

public class MyApplication extends Application {

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;// 初始化
        FamilyInteraction.initInteraction(this);
        CrashReport.initCrashReport(this);
    }

    public static Context getContext() {
        return  mContext;
    }
}
