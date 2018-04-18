package com.bearya.robot.household.entity;

/**
 * Created by Qiujz on 2017/12/1.
 */

public class MenuInfo {
    public int icon;
    public int name;
    public ItemCallback itemCallback;
    public boolean isMore;

    public MenuInfo() {

    }

    public MenuInfo(int i, int n, boolean is, ItemCallback ic) {
        icon = i;
        name = n;
        itemCallback = ic;
        isMore = is;
    }
}
