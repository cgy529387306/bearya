package com.bearya.robot.household.activity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.bearya.robot.household.R;
import com.bearya.robot.household.api.FamilyApiWrapper;
import com.bearya.robot.household.entity.UserData;
import com.bearya.robot.household.entity.UserInfo;
import com.bearya.robot.household.utils.NavigationHelper;
import com.bearya.robot.household.utils.ProjectHelper;
import com.bearya.robot.household.utils.UserInfoManager;
import com.bearya.robot.household.views.BaseActivity;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by cgy on 2018/4/19 0019.
 */

public class BindMobileActivity extends BaseActivity implements View.OnClickListener{
    private CompositeSubscription subscription;
    private EditText etTel;
    private EditText etCode;
    private EditText etPwd;
    private TextView tvSend;
    private UserInfo userInfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.string.title_bind_mobile,R.layout.activity_bind_mobile);
        getIntentData();
        initView();
        initData();
    }

    private void getIntentData() {
        userInfo = (UserInfo) getIntent().getSerializableExtra("userInfo");
    }

    private void initView() {
        etTel = (EditText) findViewById(R.id.et_tel);
        etCode = (EditText) findViewById(R.id.et_code);
        etPwd = (EditText) findViewById(R.id.et_pwd);
        tvSend = (TextView) findViewById(R.id.tv_send);
        tvSend.setOnClickListener(this);
        findViewById(R.id.tv_confirm).setOnClickListener(this);
    }

    private void initData() {
        subscription = new CompositeSubscription();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.tv_send){
            if (TextUtils.isEmpty(etTel.getText().toString().trim()) ||
                    !ProjectHelper.isMobiPhoneNum(etTel.getText().toString().trim())) {
                showToast(getString(R.string.input_correct_tel));
            }else {
                new TimeCount(60000, 1000).start();
                getValidCode();
            }
        }else if (id == R.id.tv_confirm){
            doBind();
        }
    }

    class TimeCount extends CountDownTimer {
        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);// 参数依次为总时长,和计时的时间间隔
        }

        @Override
        public void onFinish() {// 计时完毕时触发
            tvSend.setText(R.string.get_valid_code);
            tvSend.setClickable(true);
        }

        @Override
        public void onTick(long millisUntilFinished) {// 计时过程显示
            tvSend.setClickable(false);
            tvSend.setText(millisUntilFinished / 1000 + getString(R.string.reget_after_seconds));
        }
    }

    public void getValidCode() {
        showLoadingView();
        Subscription subscribe = FamilyApiWrapper.getInstance().sendSms(etTel.getText().toString().trim(),"bind")
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
                        showToast(getString(R.string.send_success));
                    }
                });
        subscription.add(subscribe);
    }




    public void doBind() {
        String mobile = etTel.getText().toString().trim();
        String password = etPwd.getText().toString().trim();
        String code = etCode.getText().toString().trim();
        if (userInfo==null){
            userInfo = new UserInfo();
        }
        if (TextUtils.isEmpty(mobile)) {
            showToast(getString(R.string.input_correct_tel));
            return;
        }else if (TextUtils.isEmpty(code)){
            showToast(getString(R.string.input_valid_code));
            return;
        }else if (TextUtils.isEmpty(password)){
            showToast(getString(R.string.input_password));
            return;
        }else if (!ProjectHelper.isMobiPhoneNum(mobile)) {
            showToast(getString(R.string.tel_error));
            return;
        }else if (!ProjectHelper.isPwdValid(password)) {
            showToast(getString(R.string.password_error));
            return;
        }
        showLoadingView();
        Subscription subscribe = FamilyApiWrapper.getInstance().mobileBind(mobile,password,code,userInfo.getUnionid(),userInfo.getOpenid())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<UserData>() {

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
                    public void onNext(UserData result) {
                        closeLoadingView();
                        if (result.getUser() != null && !TextUtils.isEmpty(result.getToken())) {
                            showToast(getString(R.string.bind_success));
                            UserInfoManager.getInstance().login(result);
                            launcherMain();
                        }
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
