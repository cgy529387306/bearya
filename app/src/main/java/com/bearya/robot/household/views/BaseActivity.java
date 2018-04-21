package com.bearya.robot.household.views;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bearya.robot.household.MainActivity;
import com.bearya.robot.household.MyApplication;
import com.bearya.robot.household.R;
import com.bearya.robot.household.activity.RootActivity;
import com.bearya.robot.household.entity.VersionInfo;
import com.bearya.robot.household.http.retrofit.HttpRetrofitClient;
import com.bearya.robot.household.update.CommonDialog;
import com.bearya.robot.household.update.SoftUpgradeActivity;
import com.bearya.robot.household.utils.CommonUtils;
import com.bearya.robot.household.utils.LogUtils;
import com.bearya.robot.household.utils.NavigationHelper;
import com.google.gson.Gson;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import okhttp3.Call;

//import android.support.v4.app.FragmentActivity;
//import android.view.WindowManager;

public abstract class BaseActivity extends AppCompatActivity implements CommonDialog.DialogUpdateListener {
    public static final String Tag = BaseActivity.class.getSimpleName();
    public final static int MSG_UPDATE_APP = 12003;
    public static String EXTRA_FLAG = "com.bearya.flag";
    private static UpdateDialog updateDialog;
    private FrameLayout mFlContent;
    private RelativeLayout mRootView;
    private RelativeLayout mLoadingView;
    private RelativeLayout mWifiDisconnectView;
    private TextView mTitle;
    private ImageView mTitleLeft;
    private ImageView mTitleRight;
    private ImageView mSettingWifi;
    protected boolean isResume = false;
    protected SystemBarTintManager tintManager;
    private long lastBackTime = 0;
    private boolean isSupportExit = false;
    private List<VersionInfo> mVersionInfos = new ArrayList<>();
    private boolean isInitRobotUpdater = false;

    public static void checkAppVersion(final Context context) {
        String url = "https://api.bearya.com/v1/source/apk/update?device=3";
        OkHttpUtils.get()
                .url(url)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int i) {
                        LogUtils.d(Tag, "checkAppVersion onError:"+e.toString());
                    }

                    @Override
                    public void onResponse(String s, int i) {
                        LogUtils.d(Tag, "checkAppVersion:"+s);
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
                            int code = CommonUtils.getVersionCode(MyApplication.getContext());
                            LogUtils.d(Tag, "App version = "+code);
                            LogUtils.d(Tag, "App version.name = "+CommonUtils.getVersionName(MyApplication.getContext()));
                            Gson gson = new Gson();
                            VersionInfo versionInfo = gson.fromJson(apk.toString(), VersionInfo.class);
                            LogUtils.d(Tag, "checkAppVersion --> versionInfo.download_url = "+versionInfo.download_url);
                            if (code < versionInfo.version) {
                                showUpdateDialog(context, versionInfo);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private static void  showUpdateDialog(Context context, VersionInfo versionInfo) {
        if (updateDialog == null) {
            updateDialog = new UpdateDialog();
            LogUtils.d(Tag, "checkAppVersion --> showUpdateDialog ");
            updateDialog.setDismisCallback(new DialogCallback() {
                @Override
                public void callback() {
                    updateDialog = null;
                }
            });
            updateDialog.createDialog(context).setVersionInfo(versionInfo).setMessage("检查到新版本\n是否更新？").showDialog();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus(true);
        }
        tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setStatusBarTintResource(android.R.color.transparent);  //设置上方状态栏透明
    }

    @TargetApi(19)
    private void setTranslucentStatus(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isResume = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isResume = false;
    }

    protected void setSupportExit(boolean isSupportExit) {
        this.isSupportExit = isSupportExit;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (isSupportExit && keyCode == KeyEvent.KEYCODE_BACK) {
            long nowTime = Calendar.getInstance().getTimeInMillis();
            if (nowTime - lastBackTime < 1000) {
                exitApp();
            } else {//按下的如果是BACK，同时没有重复
                CommonUtils.showToast(this,"再按一次退出应用！");
            }
            lastBackTime = nowTime;
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    protected void onBack() {
        finish();
    }

    protected void onRightMenu() {

    }

    public Activity getActivityParent() {
        if (this.getParent() != null) {
            return this.getParent();
        }
        return this;
    }

    public void setContentView(int layout) {
        contentView(0, "", 0, 0, 0, 0, null, layout, null, false);
    }

    protected void setContentView(CharSequence title, int layout) {
        contentView(0, title == null ? "" : title, 0, 0, 0, 0, null, layout, null,
                false);
    }

    protected void setContentView(int title, int layout, int bgId, boolean withMenu) {
        if (title == 0) {
            super.setContentView(layout);
        } else {
            contentView(title, null, bgId, 0, 0, 0, null, layout, null, withMenu);
        }
    }

    protected void setContentView(CharSequence title, View view) {
        contentView(0, title == null ? "" : title, 0, 0, 0, 0, null, 0, view, false);
    }

    protected void setContentView(int title, View view) {
        contentView(title, null, 0, 0, 0, 0, null, 0, view, false);
    }

    protected void setContentView(int title, int rightResId, int layout) {
        contentView(title, null, 0,  0, rightResId, 0, null, layout, null, false);
    }

    protected void setContentView(int title, int layout) {
        contentView(title, null, 0, 0, 0, 0, null, layout, null, false);
    }

    protected void setContentView(String title, int rightResId, int layout) {
        contentView(0, title,  0,  0, rightResId, 0, null, layout, null, false);
    }

    protected void setContentViewAndImage(int title, int rightImageResId,
                                          int layout) {
        if (title == 0) {
            super.setContentView(layout);
        } else {
            contentView(title, null, 0, 0, 0, rightImageResId, null, layout, null,
                    false);
        }
    }

    protected void setContentView(int title, int leftResId, String rightString,
                                  int layout) {
        contentView(title, null, 0, leftResId, 0, 0, rightString, layout, null, false);
    }

    private void contentView(int titleResId, CharSequence title, int bgId, int leftResId,
                             int rightResId, int rightIvResId, String rightString,
                             int layout, View vv, final boolean withMenu) {

        String titleString = title == null ? null : title.toString();
        super.setContentView(R.layout.base_layout);
        mFlContent = (FrameLayout) findViewById(R.id.frame_content);
        mLoadingView = (RelativeLayout) findViewById(R.id.pb_content_loading);
        mWifiDisconnectView = (RelativeLayout) findViewById(R.id.ll_wifi_disconnect);
        mRootView = (RelativeLayout) findViewById(R.id.root);
        RelativeLayout rlTitle = (RelativeLayout) findViewById(R.id.rl_title);
        mTitleRight = (ImageView) findViewById(R.id.btn_right);
        mTitleLeft = (ImageView) findViewById(R.id.btn_left);
        mTitle = (TextView) findViewById(R.id.tv_title);
        String titleRes = titleResId > 0 ? getString(titleResId)
                : titleString;
        if (TextUtils.isEmpty(titleRes)) {
            rlTitle.setVisibility(View.GONE);
        } else {
            mTitle.setText(titleRes);
        }
        mTitleRight.setOnClickListener(listener);
        mTitleLeft.setOnClickListener(listener);

        if (leftResId > 0) {
            mTitleLeft.setImageResource(leftResId);
        } else {
            mTitleLeft.setImageResource(R.mipmap.icon_back);
        }

        if (rightResId > 0){
            mTitleRight.setImageResource(rightResId);
            mTitleRight.setVisibility(View.VISIBLE);
        }else {
            mTitleRight.setVisibility(View.GONE);
        }

        if (bgId == 0) {
            mRootView.setBackgroundResource(R.color.colorViewBg);
        } else {
            mRootView.setBackgroundResource(bgId);
        }
        if (layout != 0) {
            LayoutInflater inflater = LayoutInflater.from(this);
            mFlContent.addView(inflater.inflate(layout, null));
        } else if (vv != null) {
            mFlContent.addView(vv);
        }
    }

    public void showWifiDisconnectView() {
        mWifiDisconnectView.setVisibility(View.VISIBLE);
    }

    public void closeWifiDisconnectView() {
        mWifiDisconnectView.setVisibility(View.GONE);
    }

    public synchronized void showLoadingView() {
        mLoadingView.setVisibility(View.VISIBLE);
    }

    public synchronized void closeLoadingView() {
        mLoadingView.setVisibility(View.GONE);
    }

    OnClickListener listener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if(id == R.id.btn_right) {
                onRightMenu();
            } else if(id == R.id.btn_left) {
                onBack();
            }
        }
    };

    public void exitApp() {
        Intent intent = new Intent(this, RootActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(EXTRA_FLAG, true);
        startActivity(intent);
        finish();
    }

    public void launcherMain() {
        NavigationHelper.startActivity(this, MainActivity.class,null,true);
//        Intent intent = new Intent(this, RootActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
//                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        intent.putExtra(EXTRA_FLAG, false);
//        startActivity(intent);
//        finish();
    }

    @Override
    public void onUpdate() {
        Intent intent = new Intent(this, SoftUpgradeActivity.class);
        intent.putParcelableArrayListExtra("versionInfo", (ArrayList<? extends Parcelable>) mVersionInfos);
        startActivity(intent);
    }

    public void showToast(String text){
        CommonUtils.showToast(this,text);
    }

    @Override
    public void onCancel() {

    }

    public void showErrorMessage(Throwable e){
        if (e instanceof HttpRetrofitClient.APIException){
            String message = ((HttpRetrofitClient.APIException)e).message;
            if (!TextUtils.isEmpty(message)){
                showToast(message);
            }
        }
    }

}
