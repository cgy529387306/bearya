package com.bearya.robot.household.entity;

/**
 * Created by Administrator on 2018\4\23 0023.
 */

public class HabitInfo {
    private int tag_id;
    private String tag_name;

    public int getTag_id() {
        return tag_id;
    }

    public void setTag_id(int tag_id) {
        this.tag_id = tag_id;
    }

    public String getTag_name() {
        return tag_name == null ? "" : tag_name;
    }

    public void setTag_name(String tag_name) {
        this.tag_name = tag_name;
    }
}
