package com.bearya.robot.household.adapter;

import android.view.View;
import android.widget.RelativeLayout;

import com.bearya.robot.household.R;
import com.bearya.robot.household.entity.ItemClickCallBack;
import com.bearya.robot.household.entity.MachineInfo;
import com.bearya.robot.household.entity.DeviceStateManage;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

/**
 * Created by Qiujz on 2017/7/3.
 */

public class DeviceListAdapter extends BaseQuickAdapter<MachineInfo, BaseViewHolder> {

    private ItemClickCallBack itemClickCallBack;
    private DeviceStateManage familyDeviceManage;

    public DeviceListAdapter(int layoutResId, List data) {
        super(layoutResId, data);
        familyDeviceManage = new DeviceStateManage();
    }

    public void setItemClickCallBack(ItemClickCallBack itemClickCallBack) {
        this.itemClickCallBack = itemClickCallBack;
    }

    @Override
    protected void convert(BaseViewHolder helper, MachineInfo item) {
        helper.setText(R.id.tv_device_name, item.name);
        RelativeLayout itemView = helper.getView(R.id.rl_device_item);
        itemView.setTag(helper.getAdapterPosition());
        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (itemClickCallBack != null) {
                    try {
                        itemClickCallBack.onLongClick(view);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
        });
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (itemClickCallBack != null) {
                    try {
                        itemClickCallBack.onClick(view);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        familyDeviceManage.addDeviceStateListener(helper.getView(R.id.im_device_icon), null, item);
    }

    public void delDeviceListener(int uid) {
        familyDeviceManage.delDeviceStateListener(uid);
    }

    public void clearDevicesListener() {
        familyDeviceManage.clearAllDeviceStateListener();
    }
}
