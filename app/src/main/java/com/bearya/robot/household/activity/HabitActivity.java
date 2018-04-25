package com.bearya.robot.household.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.bearya.robot.household.R;
import com.bearya.robot.household.api.FamilyApiWrapper;
import com.bearya.robot.household.entity.BabyInfo;
import com.bearya.robot.household.entity.HabitData;
import com.bearya.robot.household.utils.CommonUtils;
import com.bearya.robot.household.utils.DateHelper;
import com.bearya.robot.household.views.BaseActivity;
import com.bearya.robot.household.views.KeywordsFlow;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;


public class HabitActivity extends BaseActivity implements View.OnClickListener {
    private CompositeSubscription subscription;
    private KeywordsFlow flowView;
    private String name;
    private String birth;
    private String relationship;
    private int gender;
    private String avatar;
    private static final String DATE_FORMAT = "yyyy-MM-dd";
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
        flowView = (KeywordsFlow) findViewById(R.id.flowView);
    }

    private void initData() {
        subscription = new CompositeSubscription();
    }

    private void initListener() {
        findViewById(R.id.tv_confirm).setOnClickListener(this);
        flowView.setOnItemClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String keyword = ((TextView) v).getText().toString();// 获得点击的标签
                showToast(keyword);
            }
        });
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
                            flowView.setDataList(result.getList());
                            flowView.setOnItemClickListener(HabitActivity.this);
                            flowView.go2Show(KeywordsFlow.ANIMATION_OUT);
                        }
                    }
                });
        subscription.add(subscribe);
    }
}
