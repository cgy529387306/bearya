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
    private final int EDIT_WHOSEDAD = 3;
    private final int EDIT_WHOSEMOM = 4;
    private CompositeSubscription subscription;
    private DeviceInfo deviceInfo;
    private Date startDate;
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.string.device_setting,R.layout.activity_device_setting,"保存");
        initView();
        initListener();
        getDeviceDetail();
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

    private void initData(DeviceInfo info) {
        deviceInfo = info;
        if (deviceInfo!=null){
            String birth = DateHelper.timeStamp2Date(deviceInfo.getBirthday(),DATE_FORMAT);
            startDate = DateHelper.string2Date(birth,DATE_FORMAT);
            tvRabitName.setText(deviceInfo.getName());
            tvBirth.setText(birth);
            tvWhoseDad.setText(TextUtils.isEmpty(deviceInfo.getFather_name()) ? "未设置" : deviceInfo.getFather_name());
            tvWhoseMom.setText(TextUtils.isEmpty(deviceInfo.getMother_name()) ? "未设置" : deviceInfo.getMother_name());
        }
    }

    private void initListener(){
        tvRabitName.setOnClickListener(this);
        tvBirth.setOnClickListener(this);
        tvWhoseDad.setOnClickListener(this);
        tvWhoseMom.setOnClickListener(this);
    }


    private void save() {
        if (deviceInfo!=null){
            showLoadingView();
            Subscription subscribe = FamilyApiWrapper.getInstance().modify(deviceInfo.getSn(),deviceInfo.getWakeup(),deviceInfo.getName(),deviceInfo.getGender(),deviceInfo.getBirthday(),deviceInfo.getMother_name(),deviceInfo.getFather_name())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<DeviceInfo>() {

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
                        public void onNext(DeviceInfo result) {
                            closeLoadingView();
                            onBack();
                        }
                    });
            subscription.add(subscribe);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        Bundle bundle = new Bundle();
        if (id == R.id.tv_rabitName){
            bundle.putString("edit",deviceInfo==null?"":deviceInfo.getWakeup());
            NavigationHelper.startActivityForResult(DeviceSettingActivity.this,WakeUpActivity.class,bundle,EDIT_RABITNAME);
        }else if (id == R.id.tv_birth){
            showTimePicker();
        }else if (id == R.id.tv_whoseDad){
            bundle.putInt("type",0);
            bundle.putString("edit", deviceInfo==null?"":deviceInfo.getFather_name());
            NavigationHelper.startActivityForResult(DeviceSettingActivity.this,EditActivity.class,bundle,EDIT_WHOSEDAD);
        }else if (id == R.id.tv_whoseMom){
            bundle.putInt("type",1);
            bundle.putString("edit", deviceInfo==null?"":deviceInfo.getMother_name());
            NavigationHelper.startActivityForResult(DeviceSettingActivity.this,EditActivity.class,bundle,EDIT_WHOSEMOM);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            String content = data.getStringExtra("content");
            if (requestCode == EDIT_RABITNAME){
                deviceInfo.setName(content);
                tvRabitName.setText(deviceInfo.getName());
            }else if (requestCode == EDIT_WHOSEDAD){
                deviceInfo.setFather_name(content);
                tvWhoseDad.setText(deviceInfo.getFather_name());
            }else if (requestCode == EDIT_WHOSEMOM){
                deviceInfo.setMother_name(content);
                tvWhoseMom.setText(deviceInfo.getMother_name());
            }
        }
    }

    public void getDeviceDetail() {
        subscription = new CompositeSubscription();
        String sn = getIntent().getStringExtra("sn");
        showLoadingView();
        Subscription subscribe = FamilyApiWrapper.getInstance().getDeviceDetail(sn)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<DeviceInfo>() {

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
                    public void onNext(DeviceInfo result) {
                        closeLoadingView();
                        initData(result);
                    }
                });
        subscription.add(subscribe);
    }

    private void showTimePicker() {
        Date date = startDate==null?new Date():startDate;
        DatePickDialog dialog = new DatePickDialog(this);
        dialog.setYearLimt(100);
        dialog.setTitle(getString(R.string.select_time));
        dialog.setType(DateType.TYPE_YMD);
        dialog.setStartDate(date);
        dialog.setMessageFormat(DATE_FORMAT);
        dialog.setOnChangeLisener(null);
        dialog.setOnSureLisener(new OnSureLisener() {
            @Override
            public void onSure(Date date) {
                startDate = date;
                tvBirth.setText(DateHelper.date2String(startDate,DATE_FORMAT));
                deviceInfo.setBirthday(DateHelper.date2TimeStamp(startDate));
            }
        });
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(subscription != null) {
            subscription.unsubscribe();
        }
    }
}
