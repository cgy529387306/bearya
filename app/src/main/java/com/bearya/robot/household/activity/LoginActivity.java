package com.bearya.robot.household.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import com.bearya.robot.household.R;
import com.bearya.robot.household.api.FamilyApiWrapper;
import com.bearya.robot.household.entity.LoginData;
import com.bearya.robot.household.entity.LoginInfo;
import com.bearya.robot.household.http.retrofit.HttpRetrofitClient;
import com.bearya.robot.household.utils.LogUtils;
import com.bearya.robot.household.utils.NavigationHelper;
import com.bearya.robot.household.utils.ProjectHelper;
import com.bearya.robot.household.utils.SharedPrefUtil;
import com.bearya.robot.household.videoCall.RxConstants;
import com.bearya.robot.household.views.BaseActivity;
import com.google.gson.Gson;
import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.thread.EventThread;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class LoginActivity extends BaseActivity implements View.OnClickListener {
    private IWXAPI iwxapi;
    private CompositeSubscription subscription;
    private static final int PERMISSION_PHONE = 10;
    private static final int PERMISSION_CAMERA = PERMISSION_PHONE + 1;
    private static final int PERMISSION_MICROPHONE = PERMISSION_PHONE + 2;
    private static final int PERMISSION_STORAGE = PERMISSION_PHONE + 3;
    private EditText etTel;
    private EditText etPwd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
        setContentView(R.layout.activity_login);
        subscription = new CompositeSubscription();
        RxBus.get().register(this);
        setSupportExit(true);
        initView();
        initPermission();
    }

    public void initView() {
        etTel = (EditText) findViewById(R.id.et_tel);
        etPwd = (EditText) findViewById(R.id.et_pwd);
        findViewById(R.id.im_wx_login).setOnClickListener(this);
        findViewById(R.id.tv_register).setOnClickListener(this);
        findViewById(R.id.tv_forget_pwd).setOnClickListener(this);
        findViewById(R.id.tv_login).setOnClickListener(this);
    }

    public void initPermission() {
        checkSelfPermission(Manifest.permission.READ_PHONE_STATE, PERMISSION_PHONE);
    }

    public void registerToWX() {
        iwxapi = WXAPIFactory.createWXAPI(this, getString(R.string.wx_app_id), true);
        iwxapi.registerApp(getString(R.string.wx_app_id));
        if (!iwxapi.isWXAppInstalled()) {
            showToast(getString(R.string.not_install_wx));
            return;
        }
        LogUtils.d(BaseActivity.Tag, "getUserInfo registerToWX");
        SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
        req.state = "ifjiaewfa";
        iwxapi.sendReq(req);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.im_wx_login:
                LogUtils.d(BaseActivity.Tag, "getUserInfo onClick");
                registerToWX();
                break;
            case R.id.tv_register:
                NavigationHelper.startActivity(LoginActivity.this,RegisterActivity.class,null,false);
                break;
            case R.id.tv_forget_pwd:
                NavigationHelper.startActivity(LoginActivity.this,ForgetPwdActivity.class,null,false);
                break;
            case R.id.tv_login:
                doMobileLogin();
                break;
            default:
                break;
        }
    }

    @Subscribe(
            thread = EventThread.IO,
            tags = {
                    @com.hwangjr.rxbus.annotation.Tag(RxConstants.RxEventTag.RESULT_WX_LOGIN)
            }
    )

    public void loginFromWeiXin(final String code) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                boolean isOK = !TextUtils.isEmpty(code);
                if (isOK) {
                    getUserInfo(code);
                } else {

                }
            }
        });
    }

    public void getUserInfo(String code) {
        /*if (!CommonUtils.isNetAvailable(this)) {
            Toast.makeText(this, getString(R.string.wifi_disconnect), Toast.LENGTH_SHORT).show();
            return;
        }*/
        showLoadingView();
        String app = getString(R.string.app_evn).equals("dev")?"family":"household";
        Subscription subscribe = FamilyApiWrapper.getInstance().getUserInfo(code , app)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<LoginInfo>() {

                    @Override
                    public void onCompleted() {
                        closeLoadingView();
                        LogUtils.d(BaseActivity.Tag, "getUserInfo onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        closeLoadingView();
                        LogUtils.d(BaseActivity.Tag, "getUserInfo onError");
                    }

                    @Override
                    public void onNext(LoginInfo loginInfo) {
                        LogUtils.d(BaseActivity.Tag, "getUserInfo loginInfo:"+loginInfo.toString());
                        closeLoadingView();
                        if (loginInfo.user != null && !TextUtils.isEmpty(loginInfo.token)) {
                            Gson gson = new Gson();
                            SharedPrefUtil.getInstance(LoginActivity.this).put(SharedPrefUtil.KEY_USER_INFO, gson.toJson(loginInfo.user));
                            SharedPrefUtil.getInstance(LoginActivity.this).put(SharedPrefUtil.KEY_TOKEN, loginInfo.token);
                            SharedPrefUtil.getInstance(LoginActivity.this).put(SharedPrefUtil.KEY_LOGIN_STATE, true);
                            NavigationHelper.startActivity(LoginActivity.this, MainActivity.class,null,true);
                        }
                    }
                });
        subscription.add(subscribe);
    }

    public boolean checkSelfPermission(String permission, int requestCode) {
        Log.i(Tag, "checkSelfPermission " + permission + " " + requestCode);
        if (ContextCompat.checkSelfPermission(this,
                permission)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{permission},
                    requestCode);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        Log.i(Tag, "onRequestPermissionsResult " + grantResults[0] + " " + requestCode);

        switch (requestCode) {
            case PERMISSION_PHONE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_CAMERA);
                } else {
                    showToast(getString(R.string.permission_need));
                    exitApp();
                }
                break;
            }
            case PERMISSION_CAMERA: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_MICROPHONE);
                } else {
                    showToast(getString(R.string.camera_permission));
                    exitApp();
                }
                break;
            }
            case PERMISSION_MICROPHONE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE, PERMISSION_STORAGE);
                } else {
                    showToast(getString(R.string.call_permission));
                    exitApp();
                }
                break;
            }
            case PERMISSION_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    showToast(getString(R.string.storage_permission));
                    exitApp();
                }
                break;
            }
        }
    }

    public void doMobileLogin() {
        String mobile = etTel.getText().toString().trim();
        String password = etPwd.getText().toString().trim();
        if (TextUtils.isEmpty(mobile)) {
            showToast(getString(R.string.input_correct_tel));
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
        Subscription subscribe = FamilyApiWrapper.getInstance().mobileLogin(mobile,password)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<LoginData>() {

                    @Override
                    public void onCompleted() {
                        closeLoadingView();
                    }

                    @Override
                    public void onError(Throwable e) {
                        closeLoadingView();
                        if (e instanceof HttpRetrofitClient.APIException){
                            String message = ((HttpRetrofitClient.APIException)e).getMessage();
                            if (!TextUtils.isEmpty(message)){
                                showToast(message);
                            }
                        }
                    }

                    @Override
                    public void onNext(LoginData result) {
                        closeLoadingView();
                        showToast(getString(R.string.register_login));
                        NavigationHelper.startActivity(LoginActivity.this, MainActivity.class,null,true);
                    }
                });
        subscription.add(subscribe);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxBus.get().unregister(this);
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }
}
