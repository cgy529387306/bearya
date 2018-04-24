package com.bearya.robot.household.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bearya.robot.household.R;
import com.bearya.robot.household.views.BaseActivity;
import com.bearya.robot.household.views.ClearableEditText;


public class EditActivity extends BaseActivity implements View.OnClickListener{

    private ClearableEditText edtContent; //编辑框
    private TextView tvConfirm; // 确定
    private String content = "";
    private String name = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        setContentView(!TextUtils.isEmpty(name) ? name:"" ,R.layout.activity_edit);
        initView();
        initListener();
    }

    private void initView() {
        edtContent = (ClearableEditText) findViewById(R.id.edt_content);
        tvConfirm = (TextView) findViewById(R.id.tv_confirm);
        edtContent.setText(content);
    }

    private void initData() {
        name = getIntent().getStringExtra("name");
        content = getIntent().getStringExtra("edit");
    }

    private void initListener() {
        tvConfirm.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tv_confirm){
            String content = edtContent.getText().toString();
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
