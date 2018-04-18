package com.bearya.robot.household.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by Qiujz on 2017/12/4.
 */

public class BindDeviceList implements Parcelable {
    public List<MachineInfo> devices;

    public BindDeviceList() {

    }

    public BindDeviceList(Parcel in) {
        devices = in.createTypedArrayList(MachineInfo.CREATOR);
    }

    public static final Creator<BindDeviceList> CREATOR = new Creator<BindDeviceList>() {
        @Override
        public BindDeviceList createFromParcel(Parcel in) {
            return new BindDeviceList(in);
        }

        @Override
        public BindDeviceList[] newArray(int size) {
            return new BindDeviceList[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeTypedList(devices);
    }
}
