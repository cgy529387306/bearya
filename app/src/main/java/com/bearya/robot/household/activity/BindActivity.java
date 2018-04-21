package com.bearya.robot.household.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.bearya.robot.household.R;
import com.bearya.robot.household.api.FamilyApiWrapper;
import com.bearya.robot.household.entity.ProductInfo;
import com.bearya.robot.household.utils.CommonUtils;
import com.bearya.robot.household.utils.LogUtils;
import com.bearya.robot.household.utils.SharedPrefUtil;
import com.bearya.robot.household.utils.UserInfoManager;
import com.bearya.robot.household.views.BaseActivity;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class BindActivity extends BaseActivity implements View.OnClickListener {
    private static final int REQUEST_SCAN_CODE = 908;
    private CompositeSubscription sc;
    private EditText machineId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.string.main_bind_machine, R.layout.activity_bind);
        sc = new CompositeSubscription();
        initView();
    }

    private void initView() {
        machineId = (EditText) findViewById(R.id.et_machine_id);
        findViewById(R.id.tv_scan_qc).setOnClickListener(this);
        findViewById(R.id.tv_bind_machine).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_scan_qc:
                startActivityForResult(new Intent(this, ScanQRActivity.class), REQUEST_SCAN_CODE);
                break;
            case R.id.tv_bind_machine:
                String serial = machineId.getText().toString();
                if (!TextUtils.isEmpty(serial)) {
                    bindDevice(serial);
                } else {
                   showToast(getString(R.string.input_device_id));
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SCAN_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                String code = data.getStringExtra("result");
                if (!TextUtils.isEmpty(code) && code.startsWith("http")) {
                    getProductCode(code);
                } else {
                    machineId.setText(code);
                    machineId.setSelection(code.length());//设置光标位置
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(sc != null) {
            sc.unsubscribe();
        }
    }

    public void bindDevice(String serial) {
        showLoadingView();
        Subscription subscribe = FamilyApiWrapper.getInstance().bindDevice(serial)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Object>() {

                    @Override
                    public void onCompleted() {
                        closeLoadingView();
                        LogUtils.d(BaseActivity.Tag, "bindDevice onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        closeLoadingView();
                        showErrorMessage(e);
                        LogUtils.d(BaseActivity.Tag, "bindDevice onError");
                    }

                    @Override
                    public void onNext(Object result) {
                        closeLoadingView();
                        setResult(RESULT_OK);
                        finish();
                    }
                });
        sc.add(subscribe);
    }

    public void getProductCode(String url) {
        showLoadingView();
        Subscription subscribe = FamilyApiWrapper.getInstance().getProductCode(CommonUtils.MD5(url))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ProductInfo>() {

                    @Override
                    public void onCompleted() {
                        closeLoadingView();
                        LogUtils.d(BaseActivity.Tag, "getProductCode onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        closeLoadingView();
                        showErrorMessage(e);
                        LogUtils.d(BaseActivity.Tag, "getProductCode onError");
                    }

                    @Override
                    public void onNext(ProductInfo result) {
                        closeLoadingView();
                        if( TextUtils.isEmpty(result.serial_num)) {
                            Toast.makeText(BindActivity.this, getString(R.string.get_product_code_failed), Toast.LENGTH_SHORT).show();
                        } else {
                            machineId.setText(result.serial_num);
                            machineId.setSelection(result.serial_num.length());//设置光标位置
                        }
                    }
                });
        sc.add(subscribe);
    }
}
