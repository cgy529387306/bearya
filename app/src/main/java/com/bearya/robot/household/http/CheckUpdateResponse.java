package com.bearya.robot.household.http;


import com.bearya.robot.household.entity.VersionInfo;
import com.bearya.robot.household.update.IUpdateChecker;

import java.util.List;

/**
 * Created by yexifeng on 17/1/16.
 */

public class CheckUpdateResponse {

    public List<VersionInfo> getVersionList(int type) {
        switch (type){
            case IUpdateChecker.UPDATE_APK:
                return list.apk;
            case IUpdateChecker.UPDATE_DB:
                return list.database;
            case IUpdateChecker.UPDATE_ZIP:
                return list.zip;

        }
        return null;
    }

    public static class list{
        List<VersionInfo> apk;
        List<VersionInfo> database;
        List<VersionInfo> zip;
    }


    public int status;
    public String text;
    public list list;

    public boolean isSuccess(){
        return status == 1;
    }

    @Override
    public String toString() {
        return "CheckUpdateResponse{" +
                "status=" + status +
                ", text='" + text + '\'' +
                ", list=" + list +
                '}';
    }


}
