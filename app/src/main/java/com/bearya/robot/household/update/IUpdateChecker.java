package com.bearya.robot.household.update;

import com.bearya.robot.household.entity.VersionInfo;

import java.util.List;

/**
 * Created by xifengye on 2017/2/17.
 */
public interface IUpdateChecker {
    public static final int UPDATE_ZIP = 1;
    public static final int UPDATE_DB = 2;
    public static final int UPDATE_APK = 4;
    public static final int UPDATE_ALL = UPDATE_APK|UPDATE_DB|UPDATE_ZIP;
    /**
     * 支持的拓展名文件
     */
    enum SupportExtionsFile{
        apk,
        zip,
        db,
        ;
    }

    List<VersionInfo> check(List<VersionInfo> versionInfos);
    public String getExtensionName();
    public int getType();


}
