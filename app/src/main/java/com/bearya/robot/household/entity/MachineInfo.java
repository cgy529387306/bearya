package com.bearya.robot.household.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Qiujz on 2017/12/1.
 */

public class MachineInfo implements Parcelable{
    public int uid;
    public String name;
    public String dtype;
    public String sn;
    public int state = 0;
    public MachineInfo() {

    }

    public MachineInfo(int uid, String name, String dtype, String serial_num, int state) {
        this.uid = uid;
        this.name = name;
        this.dtype = dtype;
        this.sn = serial_num;
        this.state = state;
    }

    protected MachineInfo(Parcel in) {
        uid = in.readInt();
        name = in.readString();
        dtype = in.readString();
        sn = in.readString();
        state = in.readInt();
    }

    public static final Creator<MachineInfo> CREATOR = new Creator<MachineInfo>() {
        @Override
        public MachineInfo createFromParcel(Parcel in) {
            return new MachineInfo(in);
        }

        @Override
        public MachineInfo[] newArray(int size) {
            return new MachineInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(uid);
        parcel.writeString(name);
        parcel.writeString(dtype);
        parcel.writeString(sn);
        parcel.writeInt(state);
    }
}
