package com.bearya.robot.household.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.bearya.robot.household.R;
import com.bearya.robot.household.adapter.GridViewAdapter;
import com.bearya.robot.household.adapter.ViewPagerAdapter;
import com.bearya.robot.household.views.BaseActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/4/27.
 */

public class HabActivity extends BaseActivity {
    public static int item_grid_num = 4;//每一页中GridView中item的数量
    public static int number_columns = 2;//gridview一行展示的数目
    private ViewPager view_pager;
    private ViewPagerAdapter mAdapter;
    private List<DataBean> dataList;
    private List<GridView> gridList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView("兴趣标签",R.layout.activity_hab,"跳过");
        initViews();
        initDatas();
    }

    private void initViews() {
        //初始化ViewPager
        view_pager = (ViewPager) findViewById(R.id.view_pager);
        mAdapter = new ViewPagerAdapter();
        view_pager.setAdapter(mAdapter);
        dataList = new ArrayList<>();
    }

    private void initDatas() {
        if (dataList.size() > 0) {
            dataList.clear();
        }
        if (gridList.size() > 0) {
            gridList.clear();
        }
        //初始化数据
        for (int i = 0; i < 60; i++) {
            DataBean bean = new DataBean();
            bean.name = "第" + (i + 1) + "条数据";
            dataList.add(bean);
        }
        //计算viewpager一共显示几页
        int pageSize = dataList.size() % item_grid_num == 0
                ? dataList.size() / item_grid_num
                : dataList.size() / item_grid_num + 1;
        for (int i = 0; i < pageSize; i++) {
            GridView gridView = new GridView(this);
            GridViewAdapter adapter = new GridViewAdapter(dataList, i);
            gridView.setNumColumns(number_columns);
            gridView.setAdapter(adapter);
            gridList.add(gridView);
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Toast.makeText(HabActivity.this,"111",Toast.LENGTH_SHORT).show();
                }
            });
        }
        mAdapter.add(gridList);
    }
}
