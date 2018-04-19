package com.bearya.robot.household.activity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;

import com.bearya.robot.household.R;
import com.bearya.robot.household.views.BaseActivity;
import com.bearya.robot.household.views.ClearableEditText;

import java.util.Calendar;


public class UserInfoActivity extends BaseActivity implements View.OnClickListener{

    private ImageView imv_head; //头像
    private ImageView imv_dad; //选择爸爸
    private ImageView imv_mom; //选择妈妈
    private ImageView imv_other; // 选择其他
    private TextView tv_boy; //男
    private TextView tv_girl; //女
    private ClearableEditText edt_birth;//出生日期
    private final int DATE_DIALOG = 1;
    private int mYear, mMonth, mDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.string.babyInfo,R.layout.activity_user_info);
        initView();
        initListener();
    }

    private void initView() {
        imv_head = (ImageView) findViewById(R.id.imv_head);
        imv_dad = (ImageView) findViewById(R.id.imv_dad);
        imv_mom = (ImageView) findViewById(R.id.imv_mom);
        imv_other = (ImageView) findViewById(R.id.imv_other);
        tv_boy = (TextView) findViewById(R.id.tv_boy);
        tv_girl = (TextView) findViewById(R.id.tv_girl);
        edt_birth = (ClearableEditText) findViewById(R.id.edt_birth);
        final Calendar ca = Calendar.getInstance();
        mYear = ca.get(Calendar.YEAR);
        mMonth = ca.get(Calendar.MONTH);
        mDay = ca.get(Calendar.DAY_OF_MONTH);
    }
    private void initListener(){
        imv_dad.setOnClickListener(this);
        imv_mom.setOnClickListener(this);
        imv_other.setOnClickListener(this);
        tv_boy.setOnClickListener(this);
        tv_girl.setOnClickListener(this);
        edt_birth.setOnClickListener(this);
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
        }
    }

    private void checkID(int type){
        imv_dad.setImageResource(type == 0 ? R.mipmap.icon_check:R.mipmap.icon_uncheck);
        imv_mom.setImageResource(type == 1 ? R.mipmap.icon_check:R.mipmap.icon_uncheck);
        imv_other.setImageResource(type == 2 ? R.mipmap.icon_check:R.mipmap.icon_uncheck);
    }
    private void checkSex(int type){
        tv_boy.setBackgroundColor(type == 0 ? getResources().getColor(R.color.colorSex):getResources().getColor(R.color.colorViewBg));
        tv_boy.setTextColor(type == 0 ? getResources().getColor(R.color.colorWhite):getResources().getColor(R.color.colorHint));
        tv_girl.setBackgroundColor(type == 1 ? getResources().getColor(R.color.colorSex):getResources().getColor(R.color.colorViewBg));
        tv_girl.setTextColor(type == 1 ? getResources().getColor(R.color.colorWhite):getResources().getColor(R.color.colorHint));
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
        edt_birth.setText(new StringBuffer().append(mYear).append("-").append(mMonth + 1).append("-").append(mDay).append(" "));
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
}
