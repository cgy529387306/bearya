package com.bearya.robot.household.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.bearya.robot.household.R;
import com.bearya.robot.household.api.FamilyApiWrapper;
import com.bearya.robot.household.entity.DeviceInfo;
import com.bearya.robot.household.utils.DateHelper;
import com.bearya.robot.household.utils.LogUtils;
import com.bearya.robot.household.utils.NavigationHelper;
import com.bearya.robot.household.views.BaseActivity;
import com.codbking.widget.DatePickDialog;
import com.codbking.widget.OnSureLisener;
import com.codbking.widget.bean.DateType;

import java.util.Date;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;


public class DeviceSettingActivity extends BaseActivity implements View.OnClickListener{

    private TextView tvRabitName; //机器人名字
    private TextView tvBirth; //生日
    private TextView tvWhoseDad;// 他爸爸是谁
    private TextView tvWhoseMom;// 他妈妈是谁
    private final int EDIT_RABITNAME = 1;
    private final int EDIT_BIRTH = 2;
    private final int EDIT_WHOSEDAD = 3;
    private final int EDIT_WHOSEMOM = 4;
    private int type;//0:爸爸 1：妈妈
    private CompositeSubscription sc;
    private String gender = "";
    private String sn = "";
    private String wakeup = "";
    private String name = "";
    private String birth = "";
    private String father_name = "";
    private String mother_name = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.string.device_setting,R.layout.activity_device_setting,"保存");
        initView();
        initData();
        initListener();
    }

    private void initView() {
        tvRabitName = (TextView) findViewById(R.id.tv_rabitName);
        tvBirth = (TextView) findViewById(R.id.tv_birth);
        tvWhoseDad = (TextView) findViewById(R.id.tv_whoseDad);
        tvWhoseMom = (TextView) findViewById(R.id.tv_whoseMom);
    }

    @Override
    protected void onRightTip() { //右上角点击事件
        super.onRightTip();
        save();
    }

    private void initData() {
        sc = new CompositeSubscription();
        Bundle bundle = getIntent().getExtras();
        name = bundle.getString("name");
        birth = bundle.getString("dtype");
        father_name = bundle.getString("father_name");
        mother_name = bundle.getString("mother_name");
        gender = bundle.getString("gender");
        sn = bundle.getString("sn");
        tvRabitName.setText(!TextUtils.isEmpty(name)? name: "");
        tvBirth.setText(!TextUtils.isEmpty(birth)? birth: "");
        tvWhoseDad.setText(!TextUtils.isEmpty(father_name)? father_name: "");
        tvWhoseMom.setText(!TextUtils.isEmpty(mother_name)? mother_name: "");
    }

    private void initListener(){
        tvRabitName.setOnClickListener(this);
        tvBirth.setOnClickListener(this);
        tvWhoseDad.setOnClickListener(this);
        tvWhoseMom.setOnClickListener(this);
    }


    private void save() {
        wakeup = "小乖你好";
        if (TextUtils.isEmpty(wakeup)){
            showToast(getString(R.string.goto_wakeup));
            return;
        }
        showLoadingView();
        Subscription subscribe = FamilyApiWrapper.getInstance().modify(sn,wakeup,name,gender,birth,mother_name,father_name)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<DeviceInfo>() {

                    @Override
                    public void onCompleted() {
                        closeLoadingView();
                        LogUtils.d(BaseActivity.Tag, "save onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        closeLoadingView();
                        showErrorMessage(e);
                        LogUtils.d(BaseActivity.Tag, "save onError");
                    }

                    @Override
                    public void onNext(DeviceInfo result) {
                        closeLoadingView();
                        onBack();
                    }
                });
        sc.add(subscribe);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        Bundle bundle = new Bundle();
        if (id == R.id.tv_rabitName){
            bundle.putString("name",name);
            bundle.putString("wakeup",wakeup);
            NavigationHelper.startActivityForResult(DeviceSettingActivity.this,WakeUpActivity.class,bundle,EDIT_RABITNAME);
        }else if (id == R.id.tv_birth){
            showTimePicker();
        }else if (id == R.id.tv_whoseDad){
            bundle.putString("name","爸爸");
            bundle.putString("edit", father_name);
            NavigationHelper.startActivityForResult(DeviceSettingActivity.this,EditActivity.class,bundle,EDIT_WHOSEDAD);
        }else if (id == R.id.tv_whoseMom){
            bundle.putString("name","妈妈");
            bundle.putString("edit",mother_name);
            NavigationHelper.startActivityForResult(DeviceSettingActivity.this,EditActivity.class,bundle,EDIT_WHOSEMOM);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 0){
            if (requestCode == EDIT_RABITNAME){
                wakeup = data.getStringExtra("content");
                tvRabitName.setText(data.getStringExtra("content"));
            }else if (requestCode == EDIT_WHOSEDAD){
                tvWhoseDad.setText(data.getStringExtra("content"));
            }else if (requestCode == EDIT_WHOSEMOM){
                tvWhoseMom.setText(data.getStringExtra("content"));
            }

        }
    }
    private void showTimePicker() {
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
                tvBirth.setText(dateStr);
            }
        });
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(sc != null) {
            sc.unsubscribe();
        }
    }
}
