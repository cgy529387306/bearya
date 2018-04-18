package com.bearya.robot.household.adapter;

import com.bearya.robot.household.R;
import com.bearya.robot.household.entity.MenuInfo;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

/**
 * Created by Qiujz on 2017/7/3.
 */

public class MenuAdapter extends BaseQuickAdapter<MenuInfo, BaseViewHolder> {

    public MenuAdapter(int layoutResId, List data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, MenuInfo item) {
        helper.setText(R.id.tv_menu_name, item.name);
        helper.setImageResource(R.id.im_menu_icon, item.icon);
        helper.setVisible(R.id.tv_menu_more, false);
    }
}
