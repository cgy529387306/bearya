package com.bearya.robot.household.update;

import android.content.Context;


import com.bearya.robot.household.entity.VersionInfo;
import com.bearya.robot.household.utils.CommonUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xifengye on 2017/2/17.
 */

public class ApkUpdateChecker implements IUpdateChecker {

    private Context mContext;

    public ApkUpdateChecker(Context context) {
        this.mContext = context;
    }

    @Override
    public List<VersionInfo> check(List<VersionInfo> versionInfos) {
        List<VersionInfo> result = new ArrayList<>();
        for(VersionInfo versionInfo:versionInfos){
            int versionCode = CommonUtils.getVersionCodeByAppId(mContext,versionInfo.appid);
            try {
                if(versionCode<Integer.valueOf(versionInfo.version)){
                    versionInfo.setExtenionName(SupportExtionsFile.apk.name());
                    result.add(versionInfo);
                }
            }catch (Exception e){}

        }
        return result;
    }

    @Override
    public String getExtensionName() {
        return SupportExtionsFile.apk.name();
    }

    @Override
    public int getType() {
        return IUpdateChecker.UPDATE_APK;
    }

    enum CompareResult{
        Less,Equals,More;
    }

    private CompareResult compareVersionName(String left, String right){
        if(left==null || right == null){
            return CompareResult.Equals;
        }
        String[] lefts = left.split("\\.");
        String[] rights = right.split("\\.");
        for(int i=0;i<lefts.length;i++){
            if(i>=rights.length){
                return CompareResult.More;
            }
            int leftValue = Integer.valueOf(lefts[i]);
            int rightValue = Integer.valueOf(rights[i]);
            if(leftValue>rightValue){
                return CompareResult.More;
            }
            if(leftValue<rightValue){
                return CompareResult.Less;
            }
        }
        if(lefts.length<rights.length){
            return CompareResult.Less;
        }
        return CompareResult.Equals;

    }
}
