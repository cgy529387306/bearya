package com.bearya.robot.household.networkInteraction;

import android.text.TextUtils;

import com.bearya.robot.household.entity.MoveAction;
import com.bearya.robot.household.entity.MsgAction;
import com.bearya.robot.household.entity.MsgDance;
import com.bearya.robot.household.entity.MsgExpression;
import com.bearya.robot.household.entity.MsgMonitor;
import com.bearya.robot.household.entity.MsgTTS;
import com.bearya.robot.household.utils.LogUtils;
import com.wilddog.client.DataSnapshot;
import com.wilddog.client.SyncError;
import com.wilddog.client.SyncReference;
import com.wilddog.client.ValueEventListener;
import com.wilddog.client.WilddogSync;

/**
 * Created by Qiujz on 2018/2/9.
 */

public class WilddogInteraction  extends BaseInteraction {
    public static final String WILDDOG_BASE_URL = "https://wd2884820246jgtlik.wilddogio.com";
    public static final String WILDDOG_ON_LINE = "online";
    public static final String WILDDOG_DEVICES = "devices";
    public static final String WILDDOG_SERVER = "server";
    public static final String WILDDOG_CLIENT = "client";
    private ValueEventListener stateValueEventListener;
    private ValueEventListener deviceEventListener;
    private BYValueEventListener mListener;
    private SyncReference mDeviceRef;
    private SyncReference mStateRef;

    @Override
    public boolean init(String type, String serial) {
        if (!TextUtils.isEmpty(serial)) {
            addWildDogStateListener(type, serial);
            addWildDogDeviceListener(type, serial);
            return true;
        }
        return false;
    }

    public void addWildDogStateListener(String type, String serial) {
        mStateRef = WilddogSync.getInstance().getReference().child(WILDDOG_ON_LINE).child(type+"_"+serial);
        stateValueEventListener =  new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (mListener != null) {
                    mListener.onStateChange(dataSnapshot.exists());
                }
                LogUtils.d("UserWildDog", "stateValueEventListener onDataChange isOnLine = "+dataSnapshot.exists());
            }

            @Override
            public void onCancelled(SyncError syncError) {
                LogUtils.d("UserWildDog", "stateValueEventListener onCancelled syncError = "+syncError.getMessage());
            }
        };
        mStateRef.addValueEventListener(stateValueEventListener);
    }

    private void addWildDogDeviceListener(String type, String serial) {
        mDeviceRef = WilddogSync.getInstance().getReference().child(WILDDOG_DEVICES).child(type+"_"+serial);
        deviceEventListener =  new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LogUtils.d("UserWildDog", "deviceEventListener onDataChange dataSnapshot = "+dataSnapshot.getValue());
                if (mListener != null) {
                    mListener.onDataChange("");
                }
            }

            @Override
            public void onCancelled(SyncError syncError) {
                LogUtils.d("UserWildDog", "deviceEventListener onCancelled syncError = "+syncError.getMessage());
            }
        };
        mDeviceRef.child(WILDDOG_SERVER).addValueEventListener(deviceEventListener);
    }

    @Override
    public void sendTTS(String tts) {
        MsgTTS msgTTS = new MsgTTS(tts);
        mDeviceRef.child(WILDDOG_CLIENT).push().setValue(msgTTS);
    }

    @Override
    public void sendAction(String id) {
        MsgAction action = new MsgAction(id);
        mDeviceRef.child(WILDDOG_CLIENT).push().setValue(action);
    }

    @Override
    public void sendMove(String id) {
        MoveAction action = new MoveAction(id);
        mDeviceRef.child(WILDDOG_CLIENT).push().setValue(action);
    }

    @Override
    public void sendExpression(String id) {
        MsgExpression msgExpression = new MsgExpression(id);
        mDeviceRef.child(WILDDOG_CLIENT).push().setValue(msgExpression);
    }

    @Override
    public void sendDance(String id) {
        MsgDance msgDance = new MsgDance(id);
        mDeviceRef.child(WILDDOG_CLIENT).push().setValue(msgDance);
    }

    @Override
    public void sendMonitor(MsgMonitor msgMonitor) {
        mDeviceRef.child(WILDDOG_CLIENT).push().setValue(msgMonitor);
    }

    @Override
    public void setValueEventListener(BYValueEventListener listener) {
        mListener = listener;
    }

    @Override
    public void close() {
        if (mStateRef != null && stateValueEventListener != null) {
            mStateRef.removeEventListener(stateValueEventListener);
        }
        if (mDeviceRef != null && deviceEventListener != null) {
            mDeviceRef.child(WILDDOG_SERVER).removeEventListener(deviceEventListener);
        }
    }
}
