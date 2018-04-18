package com.bearya.robot.household.http.retrofit;

import com.bearya.robot.household.MyApplication;
import com.bearya.robot.household.utils.CommonUtils;

import rx.Subscriber;

/**
 * Created by lianweidong on 2017/9/16.
 */

public abstract class HttpSubscriber<T> extends Subscriber<T> {

    @Override
    public void onStart() {
        if (!CommonUtils.isNetAvailable(MyApplication.getContext())) {//网络不可用统一处理
            onCompleted();
        }
    }

    @Override
    public void onError(Throwable e) {
        if(e instanceof ExceptionHandle.ResponeThrowable){
            onError((ExceptionHandle.ResponeThrowable)e);
        } else {
            onError(new ExceptionHandle.ResponeThrowable(e, ExceptionHandle.ERROR.UNKNOWN));
        }
    }

    public abstract void onError(ExceptionHandle.ResponeThrowable e);
}
