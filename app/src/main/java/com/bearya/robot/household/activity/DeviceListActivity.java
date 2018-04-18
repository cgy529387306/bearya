package com.bearya.robot.household.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;
import com.bearya.robot.household.R;
import com.bearya.robot.household.adapter.DeviceListAdapter;
import com.bearya.robot.household.api.FamilyApiWrapper;
import com.bearya.robot.household.entity.BindDeviceList;
import com.bearya.robot.household.entity.ItemClickCallBack;
import com.bearya.robot.household.entity.MachineInfo;
import com.bearya.robot.household.utils.LogUtils;
import com.bearya.robot.household.utils.SharedPrefUtil;
import com.bearya.robot.household.views.BYCheckDialog;
import com.bearya.robot.household.views.BaseActivity;
import com.bearya.robot.household.views.DialogCallback;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class DeviceListActivity extends BaseActivity implements View.OnClickListener{
    private final static int BIND_DEVICE = 9003;
    private BindDeviceList machineInfoList = new BindDeviceList();
    private DeviceListAdapter deviceListAdapter;
    private RecyclerView deviceList;
    private BYCheckDialog checkDialog;
    private CompositeSubscription sc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.string.menu_manage, R.layout.activity_device_list);
        initView();
        initData();
    }

    public void initView() {
        findViewById(R.id.im_bind_machine).setOnClickListener(this);
        deviceList = (RecyclerView) findViewById(R.id.rv_bind_machine);
        deviceList.setLayoutManager(new GridLayoutManager(this, 1, OrientationHelper.VERTICAL, false));
    }

    public void initData() {
        sc = new CompositeSubscription();
        if (getIntent().hasExtra("devices")) {
            machineInfoList = getIntent().getParcelableExtra("devices");
        }
        deviceListAdapter = new DeviceListAdapter(R.layout.device_list_item, machineInfoList.devices);
        deviceListAdapter.setItemClickCallBack(new ItemClickCallBack() {
            @Override
            public void onClick(View view) throws Exception {
                final MachineInfo machineInfo = machineInfoList.devices.get((Integer) view.getTag());
                String msg = String.format(getString(R.string.device_unbind_hint), machineInfo.name);
                if (checkDialog == null) {
                    checkDialog = new BYCheckDialog();
                    checkDialog.createDialog(DeviceListActivity.this).setConfirmCallback(new DialogCallback() {
                        @Override
                        public void callback() {
                            unbindDevice(machineInfo);
                        }
                    }).setDismisCallback(new DialogCallback() {
                        @Override
                        public void callback() {
                            checkDialog = null;
                        }
                    });
                }
                checkDialog.setMessage(msg);
                checkDialog.showDialog();
            }
        });
        deviceList.setAdapter(deviceListAdapter);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.im_bind_machine:
                Intent bindIntent = new Intent(DeviceListActivity.this, BindActivity.class);
                startActivityForResult(bindIntent, BIND_DEVICE);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BIND_DEVICE && resultCode == Activity.RESULT_OK) {
            setResult(RESULT_OK);
            getDeviceList();
        }
    }

    public void getDeviceList() {
        showLoadingView();
        Subscription subscribe = FamilyApiWrapper.getInstance().getDeviceList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<BindDeviceList>() {

                    @Override
                    public void onCompleted() {
                        closeLoadingView();
                        LogUtils.d(BaseActivity.Tag, "getDeviceList onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        closeLoadingView();
                        LogUtils.d(BaseActivity.Tag, "getDeviceList onError");
                        if (!SharedPrefUtil.getInstance(DeviceListActivity.this).getBoolean(SharedPrefUtil.KEY_LOGIN_STATE)) {
                            launcherMain();
                            return;
                        }
                    }

                    @Override
                    public void onNext(BindDeviceList bindDeviceList) {
                        closeLoadingView();
                        machineInfoList.devices.clear();
                        deviceListAdapter.clearDevicesListener();
                        if (bindDeviceList != null && bindDeviceList.devices != null) {
                            if (bindDeviceList.devices.size() > 0) {
                                machineInfoList.devices.addAll(bindDeviceList.devices);
                            }
                        }
                        deviceListAdapter.setNewData(machineInfoList.devices);
                    }
                });
        sc.add(subscribe);
    }

    public void unbindDevice(MachineInfo machineInfo) {
        showLoadingView();
        Subscription subscribe = FamilyApiWrapper.getInstance().unBindDevice(machineInfo.serial_num, "XB")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {

                    @Override
                    public void onCompleted() {
                        closeLoadingView();
                        LogUtils.d(BaseActivity.Tag, "unBindDevice onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        closeLoadingView();
                        if (!SharedPrefUtil.getInstance(DeviceListActivity.this).getBoolean(SharedPrefUtil.KEY_LOGIN_STATE)) {
                            launcherMain();
                            return;
                        }
                        Toast.makeText(DeviceListActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        LogUtils.d(BaseActivity.Tag, "unBindDevice onError");
                    }

                    @Override
                    public void onNext(String result) {
                        closeLoadingView();
                        setResult(RESULT_OK);
                        getDeviceList();
                    }
                });
        sc.add(subscribe);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (deviceListAdapter != null) {
            deviceListAdapter.clearDevicesListener();
        }
    }
}
