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

import com.bearya.robot.household.*;
import com.bearya.robot.household.api.FamilyApiWrapper;
import com.bearya.robot.household.entity.LoginInfo;
import com.bearya.robot.household.utils.CommonUtils;
import com.bearya.robot.household.utils.LogUtils;
import com.bearya.robot.household.utils.NavigationHelper;
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
    private CompositeSubscription sc;
    private static final int PERMISSION_PHONE = 10;
    private static final int PERMISSION_CAMERA = PERMISSION_PHONE + 1;
    private static final int PERMISSION_MICROPHONE = PERMISSION_PHONE + 2;
    private static final int PERMISSION_STORAGE = PERMISSION_PHONE + 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
        setContentView(R.layout.activity_login);
        sc = new CompositeSubscription();
        RxBus.get().register(this);
        setSupportExit(true);
        initView();
        initPermission();
    }

    public void initView() {
        findViewById(R.id.im_wx_login).setOnClickListener(this);
        findViewById(R.id.tv_register).setOnClickListener(this);
        findViewById(R.id.tv_forget_pwd).setOnClickListener(this);
    }

    public void initPermission() {
        checkSelfPermission(Manifest.permission.READ_PHONE_STATE, PERMISSION_PHONE);
    }

    public void registerToWX() {
        iwxapi = WXAPIFactory.createWXAPI(this, getString(R.string.wx_app_id), true);
        iwxapi.registerApp(getString(R.string.wx_app_id));
        if (!iwxapi.isWXAppInstalled()) {
            CommonUtils.showToast(this, "没有安装微信,请先安装微信!");
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
        sc.add(subscribe);
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
                    CommonUtils.showToast(this, "您需要授权此权限!");
                    exitApp();
                }
                break;
            }
            case PERMISSION_CAMERA: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_MICROPHONE);
                } else {
                    CommonUtils.showToast(this, "您需要授权摄像头权限才可以视频通话!");
                    exitApp();
                }
                break;
            }
            case PERMISSION_MICROPHONE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE, PERMISSION_STORAGE);
                } else {
                    CommonUtils.showToast(this, "您需要授权麦克权限才可以通话!");
                    exitApp();
                }
                break;
            }
            case PERMISSION_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    LogUtils.d(Tag,"已经获取所有权限，可以使用！");
                } else {
                    CommonUtils.showToast(this, "您需要授权读写权限才可以保存用户信息!");
                    exitApp();
                }
                break;
            }
        }
    }

    /**
     * 一次申请多个权限
     */
    /*public static void requestMultiPermissions(final Activity activity, PermissionGrant grant) {

        final List<String> permissionsList = getNoGrantedPermission(activity, false);
        final List<String> shouldRationalePermissionsList = getNoGrantedPermission(activity, true);

        //TODO checkSelfPermission
        if (permissionsList == null || shouldRationalePermissionsList == null) {
            return;
        }
        Log.d(TAG, "requestMultiPermissions permissionsList:" + permissionsList.size() + ",shouldRationalePermissionsList:" + shouldRationalePermissionsList.size());

        if (permissionsList.size() > 0) {
            ActivityCompat.requestPermissions(activity, permissionsList.toArray(new String[permissionsList.size()]),
                    CODE_MULTI_PERMISSION);
            Log.d(TAG, "showMessageOKCancel requestPermissions");

        } else if (shouldRationalePermissionsList.size() > 0) {
            showMessageOKCancel(activity, "should open those permission",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(activity, shouldRationalePermissionsList.toArray(new String[shouldRationalePermissionsList.size()]),
                                    CODE_MULTI_PERMISSION);
                            Log.d(TAG, "showMessageOKCancel requestPermissions");
                        }
                    });
        } else {
            grant.onPermissionGranted(CODE_MULTI_PERMISSION);
        }

    }*/


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
        if (sc != null) {
            sc.unsubscribe();
        }
    }
}
