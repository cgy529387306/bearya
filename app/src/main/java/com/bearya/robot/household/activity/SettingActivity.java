package com.bearya.robot.household.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.bearya.robot.household.R;
import com.bearya.robot.household.api.FamilyApiWrapper;
import com.bearya.robot.household.entity.UserData;
import com.bearya.robot.household.utils.CommonUtils;
import com.bearya.robot.household.utils.NavigationHelper;
import com.bearya.robot.household.utils.ProjectHelper;
import com.bearya.robot.household.utils.SharedPrefUtil;
import com.bearya.robot.household.utils.UserInfoManager;
import com.bearya.robot.household.videoCall.AgoraService;
import com.bearya.robot.household.views.BYCheckDialog;
import com.bearya.robot.household.views.BaseActivity;
import com.bearya.robot.household.views.DialogCallback;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by cgy on 2018/4/19 0019.
 */

public class SettingActivity extends BaseActivity implements View.OnClickListener{
    private CompositeSubscription subscription;
    private BYCheckDialog checkDialog;
    private TextView tvVersion;
    private TextView tvTel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.string.title_setting,R.layout.activity_setting);
        initView();
        initData();
    }

    public void initView() {
        tvVersion = (TextView) findViewById(R.id.tv_version);
        tvTel = (TextView) findViewById(R.id.tv_tel);
        tvTel.setOnClickListener(this);
        findViewById(R.id.tv_logout).setOnClickListener(this);
    }

    private void initData() {
        subscription = new CompositeSubscription();
        tvVersion.setText(String.format("V%s", CommonUtils.getVersionName(this)));
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tv_logout){
            if (checkDialog == null) {
                checkDialog = new BYCheckDialog();
                checkDialog.createDialog(SettingActivity.this).setConfirmCallback(new DialogCallback() {
                    @Override
                    public void callback() {
                        logout();
                    }
                }).setDismisCallback(new DialogCallback() {
                    @Override
                    public void callback() {
                        checkDialog = null;
                    }
                });
            }
            checkDialog.setMessage(getString(R.string.device_exit));
            checkDialog.showDialog();
        }else if (id == R.id.tv_tel){
            callPhone(tvTel.getText().toString());
        }
    }

    public void callPhone(String phoneNum) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        Uri data = Uri.parse("tel:" + phoneNum);
        intent.setData(data);
        startActivity(intent);
    }

    private void logout(){
        showLoadingView();
        Subscription subscribe = FamilyApiWrapper.getInstance().logout()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Object>() {

                    @Override
                    public void onCompleted() {
                        closeLoadingView();
                    }

                    @Override
                    public void onError(Throwable e) {
                        closeLoadingView();
                        showErrorMessage(e);
                    }

                    @Override
                    public void onNext(Object result) {
                        closeLoadingView();
                        stopService(new Intent(SettingActivity.this, AgoraService.class));//停止通话服务
                        UserInfoManager.getInstance().loginOut();
                        launcherLogin();
                    }
                });
        subscription.add(subscribe);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(subscription != null) {
            subscription.unsubscribe();
        }
    }

}
