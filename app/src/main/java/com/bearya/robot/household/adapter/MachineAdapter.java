package com.bearya.robot.household.adapter;

import android.util.Log;
import android.widget.TextView;

import com.bearya.robot.household.R;
import com.bearya.robot.household.entity.MachineInfo;
import com.bearya.robot.household.entity.DeviceStateManage;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

/**
 * Created by Qiujz on 2017/7/3.
 */

public class MachineAdapter extends BaseQuickAdapter<MachineInfo, BaseViewHolder> {
    private int centerPosition = 0;
    private DeviceStateManage familyDeviceManage;

    public MachineAdapter(int layoutResId, List data) {
        super(layoutResId, data);
        familyDeviceManage = new DeviceStateManage();
    }

    public void setCenterPosition(int position) {
        centerPosition = position;
        notifyDataSetChanged();
    }

    @Override
    protected void convert(BaseViewHolder helper, MachineInfo item) {
        helper.setText(R.id.tv_machine_name, item.name);
        helper.setVisible(R.id.im_add_icon, item.uid <=0);
        Log.d("MachineInfo", "position="+helper.getLayoutPosition()+" item.serial_num="+item.serial_num+" item.dtype="+item.dtype);
        familyDeviceManage.addDeviceStateListener(helper.getView(R.id.rl_machine_item), (TextView) helper.getView(R.id.tv_machine_state), item);
        //helper.getView(R.id.rl_machine_item).setSelected(helper.getAdapterPosition() == centerPosition);
        //helper.setText(R.id.tv_machine_state, item.state);
    }

    public void clearDevicesListener() {
        familyDeviceManage.clearAllDeviceStateListener();
    }

}
