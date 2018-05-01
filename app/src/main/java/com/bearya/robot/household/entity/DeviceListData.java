package com.bearya.robot.household.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Qiujz on 2017/12/4.
 */

public class DeviceListData implements Parcelable {
    public List<MachineInfo> list;
    private int total;
    private int limit;
    private int count;
    private int pos;
    public DeviceListData(){

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(this.list);
        dest.writeInt(this.total);
        dest.writeInt(this.limit);
        dest.writeInt(this.count);
        dest.writeInt(this.pos);
    }

    protected DeviceListData(Parcel in) {
        this.list = in.createTypedArrayList(MachineInfo.CREATOR);
        this.total = in.readInt();
        this.limit = in.readInt();
        this.count = in.readInt();
        this.pos = in.readInt();
    }

    public static final Parcelable.Creator<DeviceListData> CREATOR = new Parcelable.Creator<DeviceListData>() {
        @Override
        public DeviceListData createFromParcel(Parcel source) {
            return new DeviceListData(source);
        }

        @Override
        public DeviceListData[] newArray(int size) {
            return new DeviceListData[size];
        }
    };

    public List<MachineInfo> getDevices() {
        if (list == null) {
            return new ArrayList<>();
        }
        return list;
    }

    public void setDevices(List<MachineInfo> devices) {
        this.list = devices;
    }

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
}

