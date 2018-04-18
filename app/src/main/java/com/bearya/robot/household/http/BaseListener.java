package com.bearya.robot.household.http;

import com.android.volley.Response;


/**
 * Created by yexifeng on 16/9/17.
 */
public interface BaseListener<T> extends Response.Listener<T>, Response.ErrorListener {

    public class ErrorCode {
    }
}
