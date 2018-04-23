package com.bearya.robot.household.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.bearya.robot.household.R;
import com.bearya.robot.household.api.FamilyApiWrapper;
import com.bearya.robot.household.entity.UserData;
import com.bearya.robot.household.utils.NavigationHelper;
import com.bearya.robot.household.utils.ProjectHelper;
import com.bearya.robot.household.utils.UserInfoManager;
import com.bearya.robot.household.views.BaseActivity;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by cgy on 2018/4/19 0019.
 */

public class BindMobileActivity extends BaseActivity implements View.OnClickListener{
    private CompositeSubscription subscription;
    private EditText etTel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.string.title_bind_mobile,R.layout.activity_bind_mobile);
        initView();
        initData();
    }

    private void initView() {
        etTel = (EditText) findViewById(R.id.et_tel);
        findViewById(R.id.tv_confirm).setOnClickListener(this);
    }

    private void initData() {
        subscription = new CompositeSubscription();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.tv_confirm){
            doBind();
        }
    }




    public void doBind() {
        String mobile = etTel.getText().toString().trim();
        if (TextUtils.isEmpty(mobile)) {
            showToast(getString(R.string.input_correct_tel));
            return;
        }else if (!ProjectHelper.isMobiPhoneNum(mobile)) {
           showToast(getString(R.string.tel_error));
            return;
        }
        showLoadingView();
        Subscription subscribe = FamilyApiWrapper.getInstance().mobileBind(mobile)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<UserData>() {

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
                    public void onNext(UserData result) {
                        closeLoadingView();
                        if (result.getUser() != null && !TextUtils.isEmpty(result.getToken())) {
                            showToast(getString(R.string.bind_success));
                            UserInfoManager.getInstance().login(result);
                            NavigationHelper.startActivity(BindMobileActivity.this, MainActivity.class,null,true);
                        }
                    }
                });
        subscription.add(subscribe);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(subscription != null) {
            subscription.unsubscribe();
        }
    }

}
