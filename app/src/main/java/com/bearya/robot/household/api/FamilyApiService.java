package com.bearya.robot.household.api;

import com.bearya.robot.household.entity.BabyInfo;
import com.bearya.robot.household.entity.DeviceInfo;
import com.bearya.robot.household.entity.DeviceListData;
import com.bearya.robot.household.entity.HabitData;
import com.bearya.robot.household.entity.KeyInfo;
import com.bearya.robot.household.entity.UserData;
import com.bearya.robot.household.entity.ProductInfo;
import com.bearya.robot.household.http.retrofit.HttpResult;

import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;

public interface FamilyApiService {
    @POST("v1/user/auth/register")
    Observable<HttpResult<UserData>> register(@Query("mobile") String mobile, @Query("password") String password, @Query("code") String code);
    @POST("v1/user/sms/send")
    Observable<HttpResult<Object>> sendSms(@Query("mobile") String mobile, @Query("action") String action);
    @POST("v1/user/login/mobile")
    Observable<HttpResult<UserData>> mobileLogin(@Query("mobile") String mobile, @Query("password") String password);
    @POST("v1/user/account/mbind")
    Observable<HttpResult<UserData>> mobileBind(@Query("mobile") String mobile);
    @POST("v1/user/account/logout")
    Observable<HttpResult<Object>> logout();
    @POST("v1/user/login/wechat")
    Observable<HttpResult<UserData>> wxLogin(@Query("mobile") String mobile, @Query("password") String password);
    @POST("v1/user/auth/getpw")
    Observable<HttpResult<Object>> getpw(@Query("mobile") String mobile, @Query("password") String password,@Query("code") String code);
    @POST("v1/user/baby/create")
    Observable<HttpResult<BabyInfo>> create(@Query("name") String name, @Query("relationship") String relationship, @Query("birthday") String birthday,
                                            @Query("gender") int gender, @Query("avatar") String avatar, @Query("tags") String tags, @Query("is_default") int is_default);
    @POST("v1/baby/tag/list")
    Observable<HttpResult<HabitData>> getHabitList(@Query("pos") int pos, @Query("limit") int limit);
    @POST("v1/user/login/wechat")
    Observable<HttpResult<UserData>> getUserInfo(@Query("code") String code, @Query("app") String app);
    @POST("v1/client/device/list")
    Observable<HttpResult<DeviceListData>> getDeviceList();
    @POST("v1/baby/bind/create")
    Observable<HttpResult<Object>> bindDevice(@Query("sn") String sn);
    @POST("v1/baby/bind/delete")
    Observable<HttpResult<Object>> unBindDevice(@Query("sn") String sn);
    @POST("v1/baby/info/detail")
    Observable<HttpResult<DeviceInfo>> getDeviceDetail(@Query("sn") String sn);
    @POST("v1/baby/info/modify")
    Observable<HttpResult<DeviceInfo>> modify(@Body RequestBody requestBody);


    @POST("v1/service/live/getKey")
    Observable<HttpResult<KeyInfo>> getMonitorKey(@Query("device_id") String device_id, @Query("uid") int uid);
    @POST("v1/client/device/qrnum")
    Observable<HttpResult<ProductInfo>> getProduceCode(@Query("url") String url);
}
