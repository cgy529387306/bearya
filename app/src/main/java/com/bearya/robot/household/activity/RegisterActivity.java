package com.bearya.robot.household.activity;

import android.os.Bundle;

import com.bearya.robot.household.R;
import com.bearya.robot.household.views.BaseActivity;

/**
 * Created by cgy on 2018/4/19 0019.
 */

public class RegisterActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.string.title_register,R.layout.activity_register);
    }
}
