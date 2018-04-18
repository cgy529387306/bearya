package com.bearya.robot.household.activity;

import android.content.Intent;
import android.os.Bundle;

import com.bearya.robot.household.utils.SharedPrefUtil;
import com.bearya.robot.household.views.BaseActivity;


public class RootActivity extends BaseActivity {

	private boolean mIsEngineInitSuccess = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Bundle extras = getIntent().getExtras();

        if (extras != null && extras.getBoolean(BaseActivity.EXTRA_FLAG, false)) {
            finish();
            return;
        }

        /* No extras, we're started by launcher. Just start the SplashActivity. */
        if (/*extras == null &&*/ !SharedPrefUtil.getInstance(this).getBoolean(SharedPrefUtil.KEY_LOGIN_STATE)) {
        	Intent intent = new Intent(this, LoginActivity.class);
        	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            return;
        }

        Intent toStart;
        toStart = new Intent(this, MainActivity.class);
        toStart.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(toStart);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

}
