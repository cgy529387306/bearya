package com.bearya.robot.household.entity;

import android.view.View;
import android.widget.TextView;

import com.bearya.robot.household.adapter.ItemValueEventListener;
//import com.bearya.robot.household.networkInteraction.BYValueEventListener;
import com.bearya.robot.household.networkInteraction.FamilyInteraction;
//import com.bearya.robot.household.utils.CommonUtils;
//import com.wilddog.client.WilddogSync;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Qiujz on 2017/12/8.
 */

public class DeviceStateManage {

    private Map<Integer, FamilyInteraction> familyDevices = new HashMap<>();

    public void addDeviceStateListener(View view, TextView textView, MachineInfo item) {
        if (familyDevices.containsKey(item.uid)) {
            delDeviceStateListener(item.uid);
        }
        FamilyInteraction familyInteraction = new FamilyInteraction();
        familyInteraction.init(item.dtype, item.serial_num);
        familyInteraction.setValueEventListener(new ItemValueEventListener(view, textView));

        /*WildDogDevice wildDogDevice = new WildDogDevice();
        wildDogDevice.stateRef = WilddogSync.getInstance().getReference().child(CommonUtils.WILDDOG_ON_LINE).child(item.dtype+"_"+item.serial_num);
        wildDogDevice.itemValueEventListener = new ItemValueEventListener(view, textView);
        wildDogDevice.stateRef.addValueEventListener(wildDogDevice.itemValueEventListener);*/
        familyDevices.put(item.uid, familyInteraction);
    }

    public void delDeviceStateListener(int uid) {
        FamilyInteraction familyDevice = familyDevices.get(uid);
        //wildDogDevice.stateRef.removeEventListener(wildDogDevice.itemValueEventListener);
        if (familyDevice != null) {
            familyDevice.close();
        }
        familyDevices.remove(uid);
    }

    public void clearAllDeviceStateListener() {
        for (Map.Entry<Integer, FamilyInteraction> entry : familyDevices.entrySet()) {
            FamilyInteraction familyDevice = entry.getValue();
            familyDevice.close();
            //wildDogDevice.stateRef.removeEventListener(wildDogDevice.itemValueEventListener);
        }
        familyDevices.clear();
    }
}
