package com.bearya.robot.household.update;

import android.content.SharedPreferences;


import com.bearya.robot.household.entity.VersionInfo;
import com.bearya.robot.household.utils.LogUtils;
import com.bearya.robot.household.utils.SharedPrefUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yexifeng on 17/2/18.
 * 检查Zip资源是否需要更新,每解压成功一个就会记录下该zip 资源为最新更新资源版本号
 */

public class ZipUpdateChecker implements IUpdateChecker {
    private final SharedPreferences preferences;

    public ZipUpdateChecker(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    @Override
    public List<VersionInfo> check(List<VersionInfo> versionInfos) {
        List<VersionInfo> result = new ArrayList<>();
        for(VersionInfo versionInfo:versionInfos) {
            if (versionInfo != null && checkOnlineVersionUpdate(versionInfo.version)) {
                versionInfo.setExtenionName(SupportExtionsFile.zip.name());
                result.add(versionInfo);
            }
        }
        return result;
    }

    @Override
    public String getExtensionName() {
        return SupportExtionsFile.zip.name();
    }

    @Override
    public int getType() {
        return IUpdateChecker.UPDATE_ZIP;
    }

    private boolean checkOnlineVersionUpdate(int version){
        int currentVersionCode = getLastZipVersionCode(preferences);
        LogUtils.e("ZIP比较更新:更新版本号%s,"+version+" 本地版本号:%d"+currentVersionCode);
        return version > currentVersionCode;
    }

    public void onUpdateSuccess(int versionCode) {
        LogUtils.e("记录当前更新ZIP的版本号是:%d"+versionCode);
        preferences.edit().putInt(SharedPrefUtil.KEY_ZIP_VERSION,versionCode).commit();
    }

    public static int getLastZipVersionCode(SharedPreferences preferences){
        return preferences.getInt(SharedPrefUtil.KEY_ZIP_VERSION,0);
    }

}
