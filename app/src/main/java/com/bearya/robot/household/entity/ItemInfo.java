package com.bearya.robot.household.entity;

/**
 * Created by Qiujz on 2017/12/1.
 */

public class ItemInfo {
    public String id;
    public int resId;
    public String name;

    public ItemInfo() {

    }

    public ItemInfo(String i, String n,int res) {
        id = i;
        name = n;
        resId = res;
    }
}
