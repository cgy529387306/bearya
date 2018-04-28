package com.bearya.robot.household.entity;

/**
 * Created by Administrator on 2018\4\20 0020.
 */

public class UserData {
//    private String agrora_token;
    private String token;
    private UserInfo user;
    private BabyInfo baby;

    public String getToken() {
        return token == null ? "" : token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserInfo getUser() {
        return user;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }

    public BabyInfo getBaby() {
        return baby;
    }

    public void setBaby(BabyInfo baby) {
        this.baby = baby;
    }
}
