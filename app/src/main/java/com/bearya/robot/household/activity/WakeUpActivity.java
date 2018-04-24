package com.bearya.robot.household.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bearya.robot.household.R;
import com.bearya.robot.household.views.BaseActivity;
import com.bearya.robot.household.views.ClearableEditText;


public class WakeUpActivity extends BaseActivity implements View.OnClickListener{

    private ClearableEditText edtName; //编辑框
    private TextView tvEvaluate; // 评估
    private ImageView ivStar1,ivStar2,ivStar3,ivStar4,ivStar5;
    private String content;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView("机器人名字",R.layout.activity_wakeup,"生成");
        getIntentData();
        initView();
        initListener();
    }

    private void getIntentData() {
        content = getIntent().getStringExtra("edit");
    }

    private void initView() {
        edtName = (ClearableEditText) findViewById(R.id.edt_name);
        tvEvaluate = (TextView) findViewById(R.id.tv_evaluate);
        ivStar1 = (ImageView) findViewById(R.id.iv_star_1);
        ivStar2 = (ImageView) findViewById(R.id.iv_star_2);
        ivStar3 = (ImageView) findViewById(R.id.iv_star_3);
        ivStar4 = (ImageView) findViewById(R.id.iv_star_4);
        ivStar5 = (ImageView) findViewById(R.id.iv_star_5);
        tvEvaluate = (TextView) findViewById(R.id.tv_evaluate);
        edtName.setText(content==null?"":content);
        edtName.setSelection(content==null?0:content.length());
    }

    private void initListener() {
        tvEvaluate.setOnClickListener(this);
        ivStar1.setOnClickListener(this);
        ivStar2.setOnClickListener(this);
        ivStar3.setOnClickListener(this);
        ivStar4.setOnClickListener(this);
        ivStar5.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tv_evaluate){
            String content = edtName.getText().toString();
            if (TextUtils.isEmpty(content)){
                Toast.makeText(WakeUpActivity.this,"内容不能为空",Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent();
            intent.putExtra("content",content);
            setResult(RESULT_OK,intent);
            finish();
        }else if (id == R.id.iv_star_1){
            starEvaluate(0,0,0,0,0);
        }else if (id == R.id.iv_star_2){
            starEvaluate(0,1,0,0,0);
        }else if (id == R.id.iv_star_3){
            starEvaluate(0,1,2,0,0);
        }else if (id == R.id.iv_star_4){
            starEvaluate(0,1,2,3,0);
        }else if (id == R.id.iv_star_5){
            starEvaluate(0,1,2,3,4);
        }
    }

    private void starEvaluate(int x,int y, int z,int i,int j){
        ivStar1.setImageResource(x == 0 ? R.mipmap.star_check : R.mipmap.star_uncheck);
        ivStar2.setImageResource(y == 1 ? R.mipmap.star_check : R.mipmap.star_uncheck);
        ivStar3.setImageResource(z == 2 ? R.mipmap.star_check : R.mipmap.star_uncheck);
        ivStar4.setImageResource(i == 3 ? R.mipmap.star_check : R.mipmap.star_uncheck);
        ivStar5.setImageResource(j == 4 ? R.mipmap.star_check : R.mipmap.star_uncheck);

    }

}
