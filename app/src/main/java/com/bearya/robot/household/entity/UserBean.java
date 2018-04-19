package com.bearya.robot.household.entity;

/**
 * Created by Administrator on 2018\4\20 0020.
 */

public class UserBean {
    private int user_id;
    private String openid;
    private String unionid;
    private String avatar;
    private String nickname;
    private int gender;
    private int uid;
    private String mobile;

    public int getUser_id() {
        return user_id;
    }

    public String getOpenid() {
        return openid == null ? "" : openid;
    }

    public String getUnionid() {
        return unionid == null ? "" : unionid;
    }

    public String getAvatar() {
        return avatar == null ? "" : avatar;
    }

    public String getNickname() {
        return nickname == null ? "" : nickname;
    }

    public int getGender() {
        return gender;
    }

    public int getUid() {
        return uid;
    }

    public String getMobile() {
        return mobile == null ? "" : mobile;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public void setUnionid(String unionid) {
        this.unionid = unionid;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
}
