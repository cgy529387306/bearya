package com.bearya.robot.household.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018\4\23 0023.
 */

public class HabitData {
    private int total;
    private int limit;
    private int count;
    private int pos;
    private List<HabitInfo> list;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public List<HabitInfo> getList() {
        if (list == null) {
            return new ArrayList<>();
        }
        return list;
    }

    public void setList(List<HabitInfo> list) {
        this.list = list;
    }
}
