package com.bearya.robot.household.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by user on 2015/8/4.
 */
public class WxUserInfo implements Parcelable {
    public int id;
    public int uid;
    public String openid;
    public String unionid;
    public String avatar;
    public String nickname;
    public int gender;

    public WxUserInfo() {

    }

    protected WxUserInfo(Parcel in) {
        id = in.readInt();
        uid = in.readInt();
        openid = in.readString();
        unionid = in.readString();
        avatar = in.readString();
        nickname = in.readString();
        gender = in.readInt();
    }

    public static final Creator<WxUserInfo> CREATOR = new Creator<WxUserInfo>() {
        @Override
        public WxUserInfo createFromParcel(Parcel in) {
            return new WxUserInfo(in);
        }

        @Override
        public WxUserInfo[] newArray(int size) {
            return new WxUserInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeInt(uid);
        parcel.writeString(openid);
        parcel.writeString(unionid);
        parcel.writeString(avatar);
        parcel.writeString(nickname);
        parcel.writeInt(gender);
    }
}
