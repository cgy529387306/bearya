package com.bearya.robot.household.activity;

import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import com.bearya.robot.household.R;
import com.bearya.robot.household.utils.NavigationHelper;
import com.bearya.robot.household.utils.SharedPrefUtil;
import com.bearya.robot.household.views.BaseActivity;

/**
 * Created by cgy on 2018/4/19 0019.
 */

public class LoadingActivity extends BaseActivity {
    private static final int LOADING_TIME_OUT = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        new Handler().postDelayed(new Runnable() {

            public void run() {
                if (SharedPrefUtil.getInstance(LoadingActivity.this).getBoolean(SharedPrefUtil.KEY_LOGIN_STATE)){
                    NavigationHelper.startActivity(LoadingActivity.this, MainActivity.class, null, true);
                }else{
                    NavigationHelper.startActivity(LoadingActivity.this, LoginActivity.class, null, true);
                }
            }
        }, LOADING_TIME_OUT);
    }
}
