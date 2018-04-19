package com.bearya.robot.household.activity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bearya.robot.household.R;
import com.bearya.robot.household.views.BaseActivity;
import com.bearya.robot.household.views.ClearableEditText;

import java.util.Calendar;


public class EditActivity extends BaseActivity implements View.OnClickListener{

    private ClearableEditText edt_content; //编辑框
    private TextView tv_confirm; // 确定
    private String content = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        setContentView(!TextUtils.isEmpty(content) ? content:"" ,R.layout.activity_edit);
        initView();
        initListener();
    }

    private void initView() {
        edt_content = (ClearableEditText) findViewById(R.id.edt_content);
        tv_confirm = (TextView) findViewById(R.id.tv_confirm);
    }

    private void initData() {
        content = getIntent().getStringExtra("edit");
    }

    private void initListener() {
        tv_confirm.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tv_confirm){
            String content = edt_content.getText().toString();
            if (TextUtils.isEmpty(content)){
                Toast.makeText(EditActivity.this,"内容不能为空",Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent();
            intent.putExtra("content",content);
            setResult(0,intent);
            finish();
        }
    }

}
