package com.bearya.robot.household.activity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;

import com.bearya.robot.household.R;
import com.bearya.robot.household.utils.NavigationHelper;
import com.bearya.robot.household.views.BaseActivity;
import com.bearya.robot.household.views.ClearableEditText;

import java.util.Calendar;


public class DeviceSettingActivity extends BaseActivity implements View.OnClickListener{

    private TextView tv_rabitName; //机器人名字
    private TextView tv_birth; //生日
    private TextView tv_whoseDad;// 他爸爸是谁
    private TextView tv_whoseMom;// 他妈妈是谁
    private final int EDIT_RABITNAME = 1;
    private final int EDIT_BIRTH = 2;
    private final int EDIT_WHOSEDAD = 3;
    private final int EDIT_WHOSEMOM = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.string.device_setting,R.layout.activity_device_setting);
        initView();
        initListener();
    }

    private void initView() {
        tv_rabitName = (TextView) findViewById(R.id.tv_rabitName);
        tv_birth = (TextView) findViewById(R.id.tv_birth);
        tv_whoseDad = (TextView) findViewById(R.id.tv_whoseDad);
        tv_whoseMom = (TextView) findViewById(R.id.tv_whoseMom);
    }
    private void initListener(){
        tv_rabitName.setOnClickListener(this);
        tv_birth.setOnClickListener(this);
        tv_whoseDad.setOnClickListener(this);
        tv_whoseMom.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        Bundle bundle = new Bundle();
        if (id == R.id.tv_rabitName){
            bundle.putString("edit", "机器人名字");
            NavigationHelper.startActivityForResult(DeviceSettingActivity.this,EditActivity.class,bundle,EDIT_RABITNAME);
        }else if (id == R.id.tv_birth){
            bundle.putString("edit", "生日");
            NavigationHelper.startActivityForResult(DeviceSettingActivity.this,EditActivity.class,bundle,EDIT_BIRTH);
        }else if (id == R.id.tv_whoseDad){
            bundle.putString("edit", "他爸爸是谁");
            NavigationHelper.startActivityForResult(DeviceSettingActivity.this,EditActivity.class,bundle,EDIT_WHOSEDAD);
        }else if (id == R.id.tv_whoseMom){
            bundle.putString("edit","他妈妈是谁");
            NavigationHelper.startActivityForResult(DeviceSettingActivity.this,EditActivity.class,bundle,EDIT_WHOSEMOM);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 0){
            if (requestCode == EDIT_RABITNAME){
                tv_rabitName.setText(data.getStringExtra("content"));
            }else if (requestCode == EDIT_BIRTH){
                tv_birth.setText(data.getStringExtra("content"));
            }else if (requestCode == EDIT_WHOSEDAD){
                tv_whoseDad.setText(data.getStringExtra("content"));
            }else if (requestCode == EDIT_WHOSEMOM){
                tv_whoseMom.setText(data.getStringExtra("content"));
            }

        }
    }
}
