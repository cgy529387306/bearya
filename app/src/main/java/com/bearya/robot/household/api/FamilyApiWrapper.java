package com.bearya.robot.household.api;

import com.bearya.robot.household.entity.BabyInfo;
import com.bearya.robot.household.entity.DeviceInfo;
import com.bearya.robot.household.entity.DeviceListData;
import com.bearya.robot.household.entity.HabitData;
import com.bearya.robot.household.entity.KeyInfo;
import com.bearya.robot.household.entity.ProductInfo;
import com.bearya.robot.household.entity.UserData;
import com.bearya.robot.household.entity.WakeupInfo;
import com.bearya.robot.household.http.retrofit.HttpRetrofitClient;

import retrofit2.http.Query;
import rx.Observable;

@SuppressWarnings("unchecked")
public class FamilyApiWrapper extends HttpRetrofitClient {
    private static FamilyApiWrapper trailApiWrapper;

    private FamilyApiWrapper() {

    }

    public static FamilyApiWrapper getInstance() {
        if (trailApiWrapper == null) {
            synchronized (FamilyApiWrapper.class) {
                if (trailApiWrapper == null) {
                    return new FamilyApiWrapper();
                }
            }
        }
        return trailApiWrapper;
    }

    public Observable<UserData> register(String mobile, String password, String code) {
        return getService(FamilyApiService.class).register(mobile, password, code).compose(this.applySchedulers());
    }

    public Observable<Object> sendSms(String mobile, String action) {
        return getService(FamilyApiService.class).sendSms(mobile, action).compose(this.applySchedulers());
    }

    public Observable<UserData> mobileLogin(String mobile, String password) {
        return getService(FamilyApiService.class).mobileLogin(mobile, password).compose(this.applySchedulers());
    }

    public Observable<UserData> mobileBind(String mobile,String password, String code,String unionid, String openid) {
        return getService(FamilyApiService.class).mobileBind(mobile,password,code,unionid,openid,"family").compose(this.applySchedulers());
    }

    public Observable<Object> logout() {
        return getService(FamilyApiService.class).logout().compose(this.applySchedulers());
    }

    public Observable<Object> setPwd(String mobile, String password, String code) {
        return getService(FamilyApiService.class).getpw(mobile, password, code).compose(this.applySchedulers());
    }

    public Observable<BabyInfo> create(String name, String relationship, String birthday, int gender, String avatar, String tags, int is_default) {
        return getService(FamilyApiService.class).create(name, relationship, birthday, gender, avatar, tags, is_default).compose(this.applySchedulers());
    }

    public Observable<UserData> getUserInfo(String code, String app) {
        return getService(FamilyApiService.class).getUserInfo(code, app).compose(this.applySchedulers());
    }

    public Observable<HabitData> getHabitList() {
        return getService(FamilyApiService.class).getHabitList(1, Integer.MAX_VALUE).compose(this.applySchedulers());
    }

    public Observable<DeviceListData> getDeviceList(String pos,  String limit) {
        return getService(FamilyApiService.class).getDeviceList(pos,limit).compose(this.applySchedulers());
    }

    public Observable<Object> bindDevice(String sn) {
        return getService(FamilyApiService.class).bindDevice(sn).compose(this.applySchedulers());
    }

    public Observable<Object> unBindDevice(String sn) {
        return getService(FamilyApiService.class).unBindDevice(sn).compose(this.applySchedulers());
    }

    public Observable<DeviceInfo> getDeviceDetail(String sn) {
        return getService(FamilyApiService.class).getDeviceDetail(sn).compose(this.applySchedulers());
    }

    public Observable<WakeupInfo> wakeupTest(String word) {
        return getService(FamilyApiService.class).wakeupTest(word).compose(this.applySchedulers());
    }

    //    public Observable<DeviceInfo> modify(DeviceInfo deviceInfo) {
//        RequestBody body = RequestBody.create(okhttp3.MediaType.parse("text/json; charset=utf-8"), JsonHelper.toJson(deviceInfo));
//        return getService(FamilyApiService.class).modify(body).compose(this.applySchedulers());
//    }

    public Observable<DeviceInfo> modify(String sn, String wakeup, String name, int gender, long birthday, String mother_name, String father_name) {
        return getService(FamilyApiService.class).modify(sn, wakeup, name, gender, birthday, mother_name, father_name).compose(this.applySchedulers());
    }

    public Observable<KeyInfo> getMonitorKey(String deviceId, int uid) {
        return getService(FamilyApiService.class).getMonitorKey(deviceId, uid).compose(this.applySchedulers());
    }

    public Observable<ProductInfo> getProductCode(String url) {
        return getService(FamilyApiService.class).getProduceCode(url).compose(this.applySchedulers());
    }

    public FamilyApiService getService(Class clz) {
        super.setIsShowTips(true);
        return (FamilyApiService) super.getService(clz);
    }
}
