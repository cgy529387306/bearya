package com.bearya.robot.household.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bearya.robot.household.R;
import com.bearya.robot.household.api.FamilyApiWrapper;
import com.bearya.robot.household.entity.BabyInfo;
import com.bearya.robot.household.http.retrofit.HttpRetrofitClient;
import com.bearya.robot.household.utils.DateHelper;
import com.bearya.robot.household.utils.NavigationHelper;
import com.bearya.robot.household.views.BaseActivity;
import com.bearya.robot.household.views.ClearableEditText;
import com.codbking.widget.DatePickDialog;
import com.codbking.widget.OnSureLisener;
import com.codbking.widget.bean.DateType;

import java.util.Date;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;


public class UserInfoActivity extends BaseActivity implements View.OnClickListener{
    private CompositeSubscription subscription;
    private ImageView imvHead; //头像
    private ImageView imvDad; //选择爸爸
    private ImageView imvMom; //选择妈妈
    private ImageView imvOther; // 选择其他
    private TextView tvBoy; //男
    private TextView tvGirl; //女
    private ClearableEditText edtBirth;//出生日期
    private TextView tvNext;
    private ClearableEditText edtName;
    private int gender = 1;
    private String relationship = "father";
    private String avatar = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.string.babyInfo,R.layout.activity_user_info);
        initView();
        initData();
        initListener();
    }

    private void initView() {
        imvHead = (ImageView) findViewById(R.id.imv_head);
        imvDad = (ImageView) findViewById(R.id.imv_dad);
        imvMom = (ImageView) findViewById(R.id.imv_mom);
        imvOther = (ImageView) findViewById(R.id.imv_other);
        tvBoy = (TextView) findViewById(R.id.tv_boy);
        tvGirl = (TextView) findViewById(R.id.tv_girl);
        edtBirth = (ClearableEditText) findViewById(R.id.edt_birth);
        tvNext = (TextView) findViewById(R.id.tv_next);
        edtName = (ClearableEditText) findViewById(R.id.edt_name);
    }
    private void initData() {
        subscription = new CompositeSubscription();
    }

    private void initListener(){
        imvDad.setOnClickListener(this);
        imvMom.setOnClickListener(this);
        imvOther.setOnClickListener(this);
        tvBoy.setOnClickListener(this);
        tvGirl.setOnClickListener(this);
        edtBirth.setOnClickListener(this);
        tvNext.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.imv_dad){
            checkID(0);
        }else if (id == R.id.imv_mom){
            checkID(1);
        }else if (id == R.id.imv_other){
            checkID(2);
        }else if (id == R.id.tv_boy){
            checkSex(0);
        }else if (id == R.id.tv_girl){
            checkSex(1);
        }else if (id == R.id.edt_birth){
            showTimePicker();
        }else if (id == R.id.tv_next){
//            doAddInfo();
            NavigationHelper.startActivity(this,HabitActivity.class,null,false);
        }
    }

    private void showTimePicker(){
        DatePickDialog dialog = new DatePickDialog(this);
        dialog.setYearLimt(100);
        dialog.setTitle(getString(R.string.select_time));
        dialog.setType(DateType.TYPE_YMD);
        dialog.setMessageFormat("yyyy-MM-dd");
        dialog.setOnChangeLisener(null);
        dialog.setOnSureLisener(new OnSureLisener() {
            @Override
            public void onSure(Date date) {
                String dateStr = DateHelper.date2String("yyyy-MM-dd");
                edtBirth.setText(dateStr);
            }
        });
        dialog.show();
    }

    private void doAddInfo() {
        String name = edtName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            showToast(getString(R.string.input_baby_name));
            return;
        }
//        Bundle bundle = new Bundle();
//        bundle.putString("name",name);
//        bundle.putString("birth",birth);
//        bundle.putString("relationship",relationship);
//        bundle.putString("gender",String.valueOf(gender));
//        bundle.putString("avatar",!TextUtils.isEmpty(avatar) ? avatar:"");
        showLoadingView();
        String birthDay = edtBirth.getText().toString();
        String stamp = DateHelper.date2Stamp(birthDay);
        Subscription subscribe = FamilyApiWrapper.getInstance().create(name,relationship,stamp,gender,avatar,"1",0)
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
                        showToast(getString(R.string.create_success));
                        finish();
                    }
                });
        subscription.add(subscribe);
    }

    private void checkID(int type){
        imvDad.setImageResource(type == 0 ? R.mipmap.icon_check:R.mipmap.icon_uncheck);
        imvMom.setImageResource(type == 1 ? R.mipmap.icon_check:R.mipmap.icon_uncheck);
        imvOther.setImageResource(type == 2 ? R.mipmap.icon_check:R.mipmap.icon_uncheck);
        if (type == 0){
            relationship = "father";
        }else if (type == 1){
            relationship = "mother";
        }else if (type == 2){
            relationship = "other";
        }
    }
    private void checkSex(int type){
        tvBoy.setBackgroundColor(type == 0 ? getResources().getColor(R.color.colorSex):getResources().getColor(R.color.colorViewBg));
        tvBoy.setTextColor(type == 0 ? getResources().getColor(R.color.colorWhite):getResources().getColor(R.color.colorHint));
        tvGirl.setBackgroundColor(type == 1 ? getResources().getColor(R.color.colorSex):getResources().getColor(R.color.colorViewBg));
        tvGirl.setTextColor(type == 1 ? getResources().getColor(R.color.colorWhite):getResources().getColor(R.color.colorHint));
        gender = type;
    }


}
