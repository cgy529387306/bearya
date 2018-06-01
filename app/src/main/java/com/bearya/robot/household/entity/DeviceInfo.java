package com.bearya.robot.household.entity;


public class DeviceInfo{

    private String sn;
    private String name;
    private String dtype;
    private int uid;
    private int gender;
    private String father_name;
    private String mother_name;
    private long birthday;
    private String wakeup;

    public String getSn() {
        return sn == null ? "" : sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getName() {
        return name == null ? "" : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDtype() {
        return dtype == null ? "" : dtype;
    }

    public void setDtype(String dtype) {
        this.dtype = dtype;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public String getFather_name() {
        return father_name == null ? "" : father_name;
    }

    public void setFather_name(String father_name) {
        this.father_name = father_name;
    }

    public String getMother_name() {
        return mother_name == null ? "" : mother_name;
    }

    public void setMother_name(String mother_name) {
        this.mother_name = mother_name;
    }

    public long getBirthday() {
        return birthday;
    }

    public void setBirthday(long birthday) {
        this.birthday = birthday;
    }

    public String getWakeup() {
        return wakeup == null ? "" : wakeup;
    }

    public void setWakeup(String wakeup) {
        this.wakeup = wakeup;
    }
}

