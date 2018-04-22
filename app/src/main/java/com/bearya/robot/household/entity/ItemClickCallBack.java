package com.bearya.robot.household.entity;

import android.view.View;

/**
 * dialog按钮回调
 * 
 * @author zach
 * 
 * @date 2017-06-14 下午2:15:36
 */
public interface ItemClickCallBack {

	void onLongClick(View view) throws Exception;

	void onClick(View view) throws Exception;
}
