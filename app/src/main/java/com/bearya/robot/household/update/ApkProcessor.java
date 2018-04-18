package com.bearya.robot.household.update;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.bearya.robot.household.utils.CommonUtils;
import com.bearya.robot.household.utils.FileUtil;


/**
 * Created by yexifeng on 17/8/26.
 */

public class ApkProcessor implements IProcessor {
    private String filePath;
    private boolean reboot;
    private Context context;
    private FileUtil.ProcessorListener listener;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable checkNotCompleteRunnable = new Runnable() {
        @Override
        public void run() {
            complete();
        }
    };

    public ApkProcessor(Context context, RobotUpdater.InstallInfo info) {
        this.filePath = info.filePath;
        this.reboot = info.reboot;
        this.context = context;
        //EventBus.getDefault().register(this);
    }

    @Override
    public boolean processor(FileUtil.ProcessorListener listener) {
        this.listener = listener;
        if(reboot) {
            complete();
//            RobotUpdater.rebootToInstallApk(context, filePath);
        }else{
            if(listener!=null){
                listener.onProgress(100);
            }
            CommonUtils.installPackage(context, filePath);
            handler.postDelayed(checkNotCompleteRunnable,30000);//如果30秒内还未收到安装成功的广播,主动结束它
        }
        return true;
    }

    @Override
    public boolean reboot() {
        return reboot;
    }

    public int getRebootFlag() {
        return reboot?1:0;
    }

    public String getFilePath() {
        return filePath;
    }
    private void complete(){
        handler.removeCallbacks(checkNotCompleteRunnable);
       /* if(EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().unregister(this);
        }*/
        if(listener!=null){
            listener.onComplete(IUpdateChecker.UPDATE_APK,0,filePath);
        }
    }

    /*@Subscribe
    public void onPackageAdded(EventPackageAdded eventPackageAdded){//TODO 安装成功退出
        DebugUtil.error("Apk安装成功");
        complete();
    }*/
}
