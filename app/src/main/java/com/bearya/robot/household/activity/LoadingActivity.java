package com.bearya.robot.household.activity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;

import com.bearya.robot.household.R;
import com.bearya.robot.household.utils.NavigationHelper;
import com.bearya.robot.household.utils.UserInfoManager;
import com.bearya.robot.household.views.BaseActivity;
import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGPushConfig;
import com.tencent.android.tpush.XGPushManager;
import com.tencent.android.tpush.common.Constants;

/**
 * Created by cgy on 2018/4/19 0019.
 */

public class LoadingActivity extends BaseActivity {
    private static final int LOADING_TIME_OUT = 1500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        initXg();
        new Handler().postDelayed(new Runnable() {

            public void run() {
                if (UserInfoManager.getInstance().isLogin()){
                    NavigationHelper.startActivity(LoadingActivity.this, MainActivity.class, null, true);
                }else{
                    NavigationHelper.startActivity(LoadingActivity.this, LoginActivity.class, null, true);
                }
            }
        }, LOADING_TIME_OUT);
    }


    private void initXg(){
        XGPushConfig.enableDebug(this, true);
        XGPushConfig.getToken(this);
        XGPushManager.registerPush(getApplicationContext(),
                new XGIOperateCallback() {
                    @Override
                    public void onSuccess(Object data, int flag) {
                        Log.w(Constants.LogTag, "+++ register push sucess. token:" + data + "flag" + flag);
                    }

                    @Override
                    public void onFail(Object data, int errCode, String msg) {
                        Log.w(Constants.LogTag,
                                "+++ register push fail. token:" + data
                                        + ", errCode:" + errCode + ",msg:"
                                        + msg);
                    }
                });

        XGPushConfig.getToken(this);

    }
}
