package com.bearya.robot.household.adapter;

import com.bearya.robot.household.R;
import com.bearya.robot.household.entity.ItemInfo;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

/**
 * Created by Qiujz on 2017/7/3.
 */

public class DanceListAdapter extends BaseQuickAdapter<ItemInfo, BaseViewHolder> {

    public DanceListAdapter(int layoutResId, List data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, ItemInfo item) {
        helper.setText(R.id.tv_dance_name, item.name);
    }
}
