package com.bearya.robot.household.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bearya.robot.household.R;
import com.bearya.robot.household.activity.DataBean;
import com.bearya.robot.household.activity.HabActivity;
import com.bearya.robot.household.views.TextCircleView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by MQ on 2016/11/11.
 */

public class GridViewAdapter extends BaseAdapter {
    private List<DataBean> dataList;

    public GridViewAdapter(List<DataBean> datas, int page) {
        dataList = new ArrayList<>();
        //start end分别代表要显示的数组在总数据List中的开始和结束位置
        int start = page * HabActivity.item_grid_num;
        int end = start + HabActivity.item_grid_num;
        while ((start < datas.size()) && (start < end)) {
            dataList.add(datas.get(start));
            start++;
        }
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public Object getItem(int i) {
        return dataList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View itemView, ViewGroup viewGroup) {
        ViewHolder mHolder;
        if (itemView == null) {
            mHolder = new ViewHolder();
            itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_gridview, viewGroup, false);
            mHolder.tv_circle_name = (TextCircleView) itemView.findViewById(R.id.tv_circle_name);
            itemView.setTag(mHolder);
        } else {
            mHolder = (ViewHolder) itemView.getTag();
        }
        DataBean bean = dataList.get(i);
        if (bean != null) {
            mHolder.tv_circle_name.setText(bean.name);
        }
        return itemView;
    }

    private class ViewHolder {
        private TextCircleView tv_circle_name;
    }
}
