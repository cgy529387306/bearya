package com.bearya.robot.household.update;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.bearya.robot.household.R;
import com.bearya.robot.household.entity.VersionInfo;
import com.bearya.robot.household.utils.CodeUtils;
import com.bearya.robot.household.utils.LogUtils;
import com.bearya.robot.household.views.BYProgressView;

import java.util.ArrayList;

/**
 * Created by yexifeng on 17/8/23.
 */


public class SoftUpgradeActivity extends Activity implements View.OnClickListener, RobotUpdater.UpdateListener {
    private View checkView;//检查更新
    private View downloadView;//下载进度
    private BYProgressView downloadProgressBar;//进度条
    private TextView downloadTipView;//提示
    private TextView precentView;//百分比
    private TextView tipView;
    private long lastDownloadId;
    private String descript;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
        setContentView(R.layout.activity_soft_update);
        initView();
        RobotUpdater.getInstance().init(getApplicationContext());
        RobotUpdater.getInstance().setUpdateListener(this);
        Bundle bundle = getIntent().getExtras();
        if(bundle.containsKey("type")) {//外部发起更新类型,检查更新都在Activity中有UI配合
            getUpgradeInfo();
        }else if(bundle.containsKey("versionInfo")){
            ArrayList<VersionInfo> versionInfos = getIntent().getParcelableArrayListExtra("versionInfo");//在外部已做完更新检查,这里直接就开始下载步骤
            LogUtils.e("versionInfos-size=%d"+versionInfos.size());
            if(CodeUtils.isEmpty(versionInfos)){
                tipAndDelayFinish("更新出错");
                return;
            }else{
                RobotUpdater.getInstance().downloadWithVersionInfos(versionInfos);
            }
        }
    }

    private void initView() {
        checkView = findViewById(R.id.soft_update_check_layout);
        downloadView = findViewById(R.id.soft_update_progress_layout);
        downloadProgressBar = (BYProgressView) findViewById(R.id.download_progress);
        downloadTipView = (TextView) findViewById(R.id.download_tip);
        precentView = (TextView) findViewById(R.id.download_precent);
        tipView = (TextView)findViewById(R.id.soft_update_tip);
    }


    private void showCheckView(){
        checkView.setVisibility(View.VISIBLE);
        downloadView.setVisibility(View.GONE);
        tipView.setVisibility(View.GONE);
    }

    private void showDownloadProgressView(){
        checkView.setVisibility(View.GONE);
        downloadView.setVisibility(View.VISIBLE);
        tipView.setVisibility(View.GONE);
    }

    private void showTipView(String tip){
        checkView.setVisibility(View.GONE);
        downloadView.setVisibility(View.GONE);
        tipView.setVisibility(View.VISIBLE);
        tipView.setText(tip);
    }

    private void getUpgradeInfo() {
        showCheckView();
        requestUpdate();
    }

    private void requestUpdate() {
        RobotUpdater.getInstance().requestUdate(getUpdateType());
    }

    private int getUpdateType() {
        int type = IUpdateChecker.UPDATE_ALL;
        if(getIntent().getExtras()!=null && getIntent().getExtras().containsKey("type")){
            type = getIntent().getExtras().getInt("type");
        }
        return type;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        RobotUpdater.getInstance().destory();
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
    }




    private void upateUi(int progress, long downloadId) {
        int lastProgress = downloadProgressBar.getProgress();

        if (lastProgress != 100 && progress < lastProgress && lastDownloadId > 0 && lastDownloadId == downloadId) {
            return;
        }

        String tip = String.format("%s(%d/%d)", getDescript(), RobotUpdater.getInstance().getCountOfDownloaded(), RobotUpdater.getInstance().getCountOfDownload());
        setProgressUi(progress, tip);

    }

    private void setProgressUi(final int progress, final String tip){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                downloadProgressBar.setProgress(progress);
                precentView.setText(progress+"%");
                if(!TextUtils.isEmpty(tip)) {
                    downloadTipView.setText(tip);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public void onNewDownloadTask(String url, long taskId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                Toast.makeText(SoftUpgradeActivity.this,"开始下载", Toast.LENGTH_LONG).show();
                showDownloadProgressView();
            }
        });
    }

    @Override
    public void onDownloadOne(String filePath) {
//        Toast.makeText(SoftUpgradeActivity.this,"下载完成", Toast.LENGTH_LONG).show();
        setProgressUi(100,"下载完成");
    }

    @Override
    public void onDownloadAll() {
        tipAndDelayFinish("下载完成");
    }

    @Override
    public void onNothingUpdate(String updateTypeName) {
        tipAndDelayFinish(updateTypeName+"已经是最新版本");
    }

    @Override
    public void onError(final String error) {
        tipAndDelayFinish(error);
    }

    private void tipAndDelayFinish(final String tip){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showTipView(tip);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                },4000);
            }
        });
    }

    @Override
    public void onProgress(final long taskId, int progress) {
        LogUtils.e("progress=%d"+progress);
        int between = progress-downloadProgressBar.getProgress();
        if(between>0 && lastDownloadId == taskId) {
            ValueAnimator valueAnimator = ValueAnimator.ofInt(downloadProgressBar.getProgress(), progress);
            valueAnimator.setDuration(between*10);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    upateUi((int) animation.getAnimatedValue(), taskId);
                }
            });
            valueAnimator.start();
        }else{
            lastDownloadId = taskId;
            setProgressUi(progress,"");
        }
    }

    @Override
    public void updateTip(String tip) {
        descript = tip;
    }

    public String getDescript() {
        return descript;
    }

}
