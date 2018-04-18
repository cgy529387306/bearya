package com.bearya.robot.household.networkInteraction;

/**
 * Created by Qiujz on 2018/2/9.
 */

public interface BYValueEventListener {
    void onStateChange(boolean isOnline);
    void onDataChange(String data);
}
