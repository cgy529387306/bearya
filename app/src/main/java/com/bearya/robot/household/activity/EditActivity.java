package com.bearya.robot.household.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.bearya.robot.household.R;
import com.bearya.robot.household.views.BaseActivity;
import com.bearya.robot.household.views.ClearableEditText;


public class EditActivity extends BaseActivity{

    private ClearableEditText edtContent; //编辑框
    private String content;
    private String title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getIntentData();
        setContentView(title,R.layout.activity_edit,"生成");
        setRightTextColor(R.color.colorBlack);
        initView();
    }

    private void initView() {
        edtContent = (ClearableEditText) findViewById(R.id.edt_content);
        edtContent.setText(content==null?"":content);
        edtContent.setSelection(content==null?0:content.length());
    }

    private void getIntentData() {
        title = getIntent().getStringExtra("title");
        content = getIntent().getStringExtra("edit");
    }



    @Override
    protected void onRightTip() {
        super.onRightTip();
        String content = edtContent.getText().toString();
        if (TextUtils.isEmpty(content)){
            Toast.makeText(EditActivity.this,"内容不能为空",Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent();
        intent.putExtra("content",content);
        setResult(RESULT_OK,intent);
        finish();
    }

}
