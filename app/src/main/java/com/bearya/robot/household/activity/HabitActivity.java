package com.bearya.robot.household.activity;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bearya.robot.household.R;
import com.bearya.robot.household.adapter.ViewPagerAdapter;
import com.bearya.robot.household.api.FamilyApiWrapper;
import com.bearya.robot.household.entity.BabyInfo;
import com.bearya.robot.household.entity.HabitData;
import com.bearya.robot.household.entity.HabitInfo;
import com.bearya.robot.household.utils.CommonUtils;
import com.bearya.robot.household.utils.DateHelper;
import com.bearya.robot.household.views.BaseActivity;
import com.bearya.robot.household.views.CircleView;
import com.bearya.robot.household.views.FlowLabelLayout;
import com.bearya.robot.household.views.KeywordsFlow;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;


public class HabitActivity extends BaseActivity implements View.OnClickListener {
    private CompositeSubscription subscription;
    private String name;
    private String birth;
    private String relationship;
    private int gender;
    private String avatar;
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private ViewPager viewPager;
    private ViewPagerAdapter pagerAdapter;
    private Random random;
    private int paddingArray[] = {10,12,14,16,18,20,22,24,26,28,30};
    private List<FlowLabelLayout> labelLayoutList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.string.title_habit, R.layout.activity_habit);
        initIntent();
        initView();
        initData();
        initListener();
        getList();
    }

    private void initIntent(){
        name = getIntent().getStringExtra("name");
        birth = getIntent().getStringExtra("birth");
        relationship = getIntent().getStringExtra("relationship");
        gender = getIntent().getIntExtra("gender",0);
        avatar = getIntent().getStringExtra("avatar");
    }

    private void initView() {
        random = new Random();
        viewPager = (ViewPager) findViewById(R.id.view_pager);
    }

    private void initData() {
        pagerAdapter = new ViewPagerAdapter();
        viewPager.setAdapter(pagerAdapter);
        subscription = new CompositeSubscription();
    }

    private void initListener() {
        findViewById(R.id.tv_confirm).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tv_confirm) {
            doAddInfo();
        }
    }

    private void doAddInfo() {
        showLoadingView();
        long stamp = DateHelper.date2TimeStamp(DateHelper.string2Date(birth,DATE_FORMAT));
        Subscription subscribe = FamilyApiWrapper.getInstance().create(name, relationship, String.valueOf(stamp), gender, avatar, "1", 0)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<BabyInfo>() {

                    @Override
                    public void onCompleted() {
                        closeLoadingView();
                    }

                    @Override
                    public void onError(Throwable e) {
                        closeLoadingView();
                        showErrorMessage(e);
                    }

                    @Override
                    public void onNext(BabyInfo result) {
                        closeLoadingView();
                        showToast(getString(R.string.save_success));
                        launcherMain();
                    }
                });
        subscription.add(subscribe);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(subscription != null) {
            subscription.unsubscribe();
        }
    }

    public void getList() {
        showLoadingView();
        Subscription subscribe = FamilyApiWrapper.getInstance().getHabitList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<HabitData>() {

                    @Override
                    public void onCompleted() {
                        closeLoadingView();
                    }

                    @Override
                    public void onError(Throwable e) {
                        closeLoadingView();
                        showErrorMessage(e);
                    }

                    @Override
                    public void onNext(HabitData result) {
                        closeLoadingView();
                        if (CommonUtils.isNotEmpty(result) && CommonUtils.isNotEmpty(result.getList())){
                            initPagerData(result.getList());
                        }
                    }
                });
        subscription.add(subscribe);
    }

    private void initPagerData(List<HabitInfo> list){
        int[] colorArray = getResources().getIntArray(R.array.colors);
        for (int i=0;i<list.size();i++){
            int padding = paddingArray[random.nextInt(9)];
            int color = colorArray[random.nextInt(9)];
            HabitInfo habitInfo = list.get(i);
            habitInfo.setPadding(padding);
            habitInfo.setColor(color);
        }
        int pageSize = list.size()/6;
        List<List<HabitInfo>> subList = CommonUtils.splitList(list, pageSize);
        for (int i=0;i<subList.size();i++){
            List<HabitInfo> habitInfos = subList.get(i);
            FlowLabelLayout labelLayout = new FlowLabelLayout(HabitActivity.this);
            ViewGroup.MarginLayoutParams lp = new ViewGroup.MarginLayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(10,10,10,10);
            for (int j = 0; j < habitInfos.size(); j++) {
                final HabitInfo habitInfo = habitInfos.get(j);
                CircleView view = new CircleView(this);
                view.setPadding(habitInfo.getPadding(),habitInfo.getPadding(),habitInfo.getPadding(),habitInfo.getPadding());
                view.setText(habitInfo.getTag_name());
                view.setTextColor(habitInfo.getColor());
                view.setBorderColor(habitInfo.getColor());
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CircleView circleView = (CircleView) view;
                        habitInfo.setSelect(!habitInfo.isSelect());
                        circleView.setTextColor(habitInfo.getColor());
                        circleView.setFillColor(habitInfo.getFillColor());
                    }
                });
                labelLayout.addView(view, lp);
                labelLayout.requestLayout();
            }
            labelLayoutList.add(labelLayout);
            pagerAdapter.add(labelLayoutList);
        }



    }


}
