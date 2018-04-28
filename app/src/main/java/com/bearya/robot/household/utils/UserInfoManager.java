package com.bearya.robot.household.utils;

import com.bearya.robot.household.MyApplication;
import com.bearya.robot.household.entity.BabyInfo;
import com.bearya.robot.household.entity.UserData;
import com.bearya.robot.household.entity.UserInfo;

/**
 * 作者：cgy on 16/12/26 22:53
 * 邮箱：529387306@qq.com
 */
public class UserInfoManager {

    private static UserInfoManager instance;
    private final static String KEY_IS_LOGIN = "KEY_IS_LOGIN";
    private final static String KEY_USER_INFO = "KEY_USER_INFO";
    private final static String KEY_TOKEN = "KEY_TOKEN";
    private final static String KEY_BABY_INFO = "KEY_BABY_INFO";
    public static UserInfoManager getInstance() {
        if (instance == null) {
            instance = new UserInfoManager();
        }
        return instance;
    }


    public void login(UserData userData){
        setUserInfo(userData.getUser());
        setBayInfo(userData.getBaby());
        setToken(userData.getToken());
        setIsLogin(true);
    }

    public UserInfo getUserInfo(){
       String userInfo = SharedPrefUtil.getInstance(MyApplication.getContext()).getString(KEY_USER_INFO);
       return JsonHelper.fromJson(userInfo,UserInfo.class);
    }

    public void setUserInfo(UserInfo userInfo){
        SharedPrefUtil.getInstance(MyApplication.getContext()).put(KEY_USER_INFO, JsonHelper.toJson(userInfo));
    }

    public BabyInfo getBabyInfo(){
        String bayInfo = SharedPrefUtil.getInstance(MyApplication.getContext()).getString(KEY_BABY_INFO);
        return JsonHelper.fromJson(bayInfo,BabyInfo.class);
    }

    public void setBayInfo(BabyInfo bayInfo){
        SharedPrefUtil.getInstance(MyApplication.getContext()).put(KEY_BABY_INFO, JsonHelper.toJson(bayInfo));
    }

    public String getToken(){
        return SharedPrefUtil.getInstance(MyApplication.getContext()).getString(KEY_TOKEN);
    }

    public void setToken(String token){
        SharedPrefUtil.getInstance(MyApplication.getContext()).put(KEY_TOKEN, token);
    }

    public Boolean isLogin(){
        return SharedPrefUtil.getInstance(MyApplication.getContext()).getBoolean(KEY_IS_LOGIN,false);
    }

    public void setIsLogin(boolean isLogin){
        SharedPrefUtil.getInstance(MyApplication.getContext()).put(KEY_IS_LOGIN, isLogin);
    }

    public void loginOut() {
        setUserInfo(null);
        setToken("");
        setIsLogin(false);
    }
}
