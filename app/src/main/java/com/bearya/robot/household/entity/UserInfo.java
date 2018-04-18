package com.bearya.robot.household.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by user on 2015/8/4.
 */
public class UserInfo implements Parcelable {
    public int id;
    public int uid;
    public String openid;
    public String unionid;
    public String avatar;
    public String nickname;
    public int gender;

    public UserInfo() {

    }

    protected UserInfo(Parcel in) {
        id = in.readInt();
        uid = in.readInt();
        openid = in.readString();
        unionid = in.readString();
        avatar = in.readString();
        nickname = in.readString();
        gender = in.readInt();
    }

    public static final Creator<UserInfo> CREATOR = new Creator<UserInfo>() {
        @Override
        public UserInfo createFromParcel(Parcel in) {
            return new UserInfo(in);
        }

        @Override
        public UserInfo[] newArray(int size) {
            return new UserInfo[size];
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
