package com.bearya.robot.household.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.bearya.robot.household.R;
import com.bearya.robot.household.utils.NavigationHelper;
import com.bearya.robot.household.views.BaseActivity;


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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.string.device_setting,R.layout.activity_device_setting);
        initView();
        initListener();
    }

    private void initView() {
        tvRabitName = (TextView) findViewById(R.id.tv_rabitName);
        tvBirth = (TextView) findViewById(R.id.tv_birth);
        tvWhoseDad = (TextView) findViewById(R.id.tv_whoseDad);
        tvWhoseMom = (TextView) findViewById(R.id.tv_whoseMom);
    }
    private void initListener(){
        tvRabitName.setOnClickListener(this);
        tvBirth.setOnClickListener(this);
        tvWhoseDad.setOnClickListener(this);
        tvWhoseMom.setOnClickListener(this);
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
            bundle.putString("type", "0");
            NavigationHelper.startActivityForResult(DeviceSettingActivity.this,EditActivity.class,bundle,EDIT_WHOSEDAD);
        }else if (id == R.id.tv_whoseMom){
            bundle.putString("type","1");
            NavigationHelper.startActivityForResult(DeviceSettingActivity.this,EditActivity.class,bundle,EDIT_WHOSEMOM);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 0){
            if (requestCode == EDIT_RABITNAME){
                tvRabitName.setText(data.getStringExtra("content"));
            }else if (requestCode == EDIT_BIRTH){
                tvBirth.setText(data.getStringExtra("content"));
            }else if (requestCode == EDIT_WHOSEDAD){
                tvWhoseDad.setText(data.getStringExtra("content"));
            }else if (requestCode == EDIT_WHOSEMOM){
                tvWhoseMom.setText(data.getStringExtra("content"));
            }

        }
    }
}
