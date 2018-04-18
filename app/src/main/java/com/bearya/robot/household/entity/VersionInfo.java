package com.bearya.robot.household.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by user on 2015/8/4.
 */
public class VersionInfo implements Parcelable {
    public String appid;
    public String download_url;
    public String tips;
    public long pack_size;
    public String minimum_version;
    public int version;
    public int reboot;

    public String getExtenionName() {
        return extenionName;
    }

    public void setExtenionName(String extenionName) {
        this.extenionName = extenionName;
    }

    private String extenionName;

    @Override
    public String toString() {
        return "VersionInfo{" +
                "appid='" + appid + '\'' +
                ",download_url='" + download_url + '\'' +
                ", tips='" + tips + '\'' +
                ", pack_size=" + pack_size +
                ", minimum_version=" + minimum_version +
                ", version='" + version + '\'' +
                '}';
    }


    public boolean isRebootToInstall() {
        return reboot>0;
    }

    public String getDownloadFileName(){
        return String.format("%s_%s_%d.%s", appid, version,pack_size,getExtenionName());

    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.appid);
        dest.writeString(this.download_url);
        dest.writeString(this.tips);
        dest.writeLong(this.pack_size);
        dest.writeString(this.minimum_version);
        dest.writeInt(this.version);
        dest.writeInt(this.reboot);
        dest.writeString(this.extenionName);
    }

    public VersionInfo() {
    }

    protected VersionInfo(Parcel in) {
        this.appid = in.readString();
        this.download_url = in.readString();
        this.tips = in.readString();
        this.pack_size = in.readLong();
        this.minimum_version = in.readString();
        this.version = in.readInt();
        this.reboot = in.readInt();
        this.extenionName = in.readString();
    }

    public static final Creator<VersionInfo> CREATOR = new Creator<VersionInfo>() {
        @Override
        public VersionInfo createFromParcel(Parcel source) {
            return new VersionInfo(source);
        }

        @Override
        public VersionInfo[] newArray(int size) {
            return new VersionInfo[size];
        }
    };
}
