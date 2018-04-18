package com.bearya.robot.household.utils;

import java.util.Collection;
import java.util.HashMap;

/**
 * Created by yexifeng on 17/9/5.
 */

public class CodeUtils {
    public static boolean isEmpty(Collection collection){
        return collection==null || collection.isEmpty();
    }


    public static String toHttpParam(HashMap<String, Object> mParamMap) {
        if(mParamMap == null || mParamMap.size()<=0){
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for(String key:mParamMap.keySet()){
            if(sb.length()>0){
                sb.append("&");
            }
            sb.append(key+"="+mParamMap.get(key));
        }
        return sb.toString();
    }
}
