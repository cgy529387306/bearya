package com.bearya.robot.household.activity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bearya.robot.household.R;
import com.bearya.robot.household.api.FamilyApiWrapper;
import com.bearya.robot.household.entity.BabyInfo;
import com.bearya.robot.household.http.retrofit.HttpRetrofitClient;
import com.bearya.robot.household.utils.NavigationHelper;
import com.bearya.robot.household.views.BaseActivity;
import com.bearya.robot.household.views.ClearableEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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
    private final int DATE_DIALOG = 1;
    private int mYear, mMonth, mDay;
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
        final Calendar ca = Calendar.getInstance();
        mYear = ca.get(Calendar.YEAR);
        mMonth = ca.get(Calendar.MONTH);
        mDay = ca.get(Calendar.DAY_OF_MONTH);
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
            showDialog(DATE_DIALOG);
        }else if (id == R.id.tv_next){
            doAddInfo();
        }
    }

    private void doAddInfo() {
        String name = edtName.getText().toString().trim();
        String birth = edtBirth.getText().toString().trim();
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
        Subscription subscribe = FamilyApiWrapper.getInstance().create(name,relationship,!TextUtils.isEmpty(birth)?Integer.parseInt(dataOne(birth)):0,gender,avatar,"1",0)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<BabyInfo>() {

                    @Override
                    public void onCompleted() {
                        closeLoadingView();
                    }

                    @Override
                    public void onError(Throwable e) {
                        closeLoadingView();
                        if (e instanceof HttpRetrofitClient.APIException){
                            String message = ((HttpRetrofitClient.APIException)e).getMessage();
                            if (!TextUtils.isEmpty(message)){
                                showToast(message);
                            }
                        }
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
        }else if (type == 0){
            relationship = "mother";
        }else if (type == 0){
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
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG:
                return new DatePickerDialog(this, mdateListener, mYear, mMonth, mDay);
        }
        return null;
    }

    /**
     * 设置日期 利用StringBuffer追加
     */
    public void display() {
        edtBirth.setText(new StringBuffer().append(mYear).append("-").append(mMonth + 1).append("-").append(mDay).append(" "));
    }

    private DatePickerDialog.OnDateSetListener mdateListener = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            mYear = year;
            mMonth = monthOfYear;
            mDay = dayOfMonth;
            display();
        }
    };
    public static String dataOne(String time) {
        SimpleDateFormat sdr = new SimpleDateFormat("yyyy-MM-dd",
                Locale.CHINA);
        Date date;
        String times = null;
        try {
            date = sdr.parse(time);
            long l = date.getTime();
            String stf = String.valueOf(l);
            times = stf.substring(0, 10);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return times;
    }
}
