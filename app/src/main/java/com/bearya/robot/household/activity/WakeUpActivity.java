package com.bearya.robot.household.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bearya.robot.household.R;
import com.bearya.robot.household.api.FamilyApiWrapper;
import com.bearya.robot.household.entity.WakeupInfo;
import com.bearya.robot.household.utils.CommonUtils;
import com.bearya.robot.household.views.BaseActivity;
import com.bearya.robot.household.views.ClearableEditText;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;


public class WakeUpActivity extends BaseActivity implements View.OnClickListener{
    private CompositeSubscription subscription;
    private ClearableEditText edtName; //编辑框
    private TextView tvEvaluate; // 评估
    private ImageView ivStar1,ivStar2,ivStar3,ivStar4,ivStar5;
    private String edit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView("机器人名字",R.layout.activity_wakeup,"生成");
        getIntentData();
        initView();
        initListener();
        initData();
    }

    private void initData() {
        String name = edtName.getText().toString().trim();
        if (!TextUtils.isEmpty(name) && CommonUtils.isChinese(name)){
            getRank();
        }
    }

    @Override
    protected void onRightTip() {
        super.onRightTip();
        String name = edtName.getText().toString().trim();
        if (TextUtils.isEmpty(name)){
            showToast("请输入唤醒词");
            return;
        }
        if (!CommonUtils.isChinese(name)){
            showToast("必须为中文");
            return;
        }
        Intent intent = new Intent();
        intent.putExtra("content",name);
        setResult(RESULT_OK,intent);
        finish();
    }

    private void getIntentData() {
        edit = getIntent().getStringExtra("edit");
    }

    private void initView() {
        subscription = new CompositeSubscription();
        edtName = (ClearableEditText) findViewById(R.id.edt_name);
        tvEvaluate = (TextView) findViewById(R.id.tv_evaluate);
        ivStar1 = (ImageView) findViewById(R.id.iv_star_1);
        ivStar2 = (ImageView) findViewById(R.id.iv_star_2);
        ivStar3 = (ImageView) findViewById(R.id.iv_star_3);
        ivStar4 = (ImageView) findViewById(R.id.iv_star_4);
        ivStar5 = (ImageView) findViewById(R.id.iv_star_5);
        tvEvaluate = (TextView) findViewById(R.id.tv_evaluate);
        edtName.setText(edit==null?"":edit);
        edtName.setSelection(edit==null?0:edit.length());
    }

    private void initListener() {
        tvEvaluate.setOnClickListener(this);
        edtName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String name = edtName.getText().toString().trim();
                if (TextUtils.isEmpty(name)){
                    fillRank(0);
                }
            }
        });
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
            getRank();
        }
    }

    private void fillRank(int rank){
        ivStar1.setImageResource(rank > 0 ? R.mipmap.star_check : R.mipmap.star_uncheck);
        ivStar2.setImageResource(rank > 1 ? R.mipmap.star_check : R.mipmap.star_uncheck);
        ivStar3.setImageResource(rank > 2 ? R.mipmap.star_check : R.mipmap.star_uncheck);
        ivStar4.setImageResource(rank > 3 ? R.mipmap.star_check : R.mipmap.star_uncheck);
        ivStar5.setImageResource(rank > 4 ? R.mipmap.star_check : R.mipmap.star_uncheck);
    }

    public void getRank() {
        String name = edtName.getText().toString().trim();
        if (TextUtils.isEmpty(name)){
            showToast("请输入唤醒词");
            return;
        }
        if (!CommonUtils.isChinese(name)){
            showToast("必须为中文");
            return;
        }
        showLoadingView();
        Subscription subscribe = FamilyApiWrapper.getInstance().wakeupTest(name)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<WakeupInfo>() {

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
                    public void onNext(WakeupInfo result) {
                        closeLoadingView();
                        if (result!=null){
                            fillRank(result.getRank());
                        }
                    }
                });
        subscription.add(subscribe);
    }

}
