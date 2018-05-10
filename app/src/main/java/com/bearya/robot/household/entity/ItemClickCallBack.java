package com.bearya.robot.household.entity;

/**
 * dialog按钮回调
 * 
 * @author zach
 * 
 * @date 2017-06-14 下午2:15:36
 */
public interface ItemClickCallBack {

	void onDeleteClick(MachineInfo machineInfo);

	void onClick(MachineInfo machineInfo);
}
