package com.bearya.robot.household.entity;

import com.bearya.robot.household.adapter.ItemValueEventListener;
import com.wilddog.client.SyncReference;

/**
 * Created by Qiujz on 2017/12/8.
 */

public class WildDogDevice {
    public SyncReference stateRef;
    public ItemValueEventListener itemValueEventListener;
}
