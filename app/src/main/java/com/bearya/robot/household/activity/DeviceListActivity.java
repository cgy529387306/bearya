package com.bearya.robot.household.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;

import com.bearya.robot.household.R;
import com.bearya.robot.household.adapter.DeviceListAdapter;
import com.bearya.robot.household.api.FamilyApiWrapper;
import com.bearya.robot.household.entity.DeviceInfo;
import com.bearya.robot.household.entity.DeviceListData;
import com.bearya.robot.household.entity.ItemClickCallBack;
import com.bearya.robot.household.entity.MachineInfo;
import com.bearya.robot.household.utils.CommonUtils;
import com.bearya.robot.household.utils.LogUtils;
import com.bearya.robot.household.utils.NavigationHelper;
import com.bearya.robot.household.views.BYCheckDialog;
import com.bearya.robot.household.views.BaseActivity;
import com.bearya.robot.household.views.DialogCallback;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class DeviceListActivity extends BaseActivity implements View.OnClickListener{
    private final static int BIND_DEVICE = 9003;
    private DeviceListData machineInfoList = new DeviceListData();
    private DeviceListAdapter deviceListAdapter;
    private RecyclerView deviceList;
    private BYCheckDialog checkDialog;
    private CompositeSubscription sc;
    private FrameLayout emptyView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.string.menu_manage,R.mipmap.icon_add,R.layout.activity_device_list);
        initView();
        initData();
    }

    public void initView() {
        emptyView = (FrameLayout) findViewById(R.id.emptyView);
        deviceList = (RecyclerView) findViewById(R.id.rv_bind_machine);
        deviceList.setLayoutManager(new GridLayoutManager(this, 1, OrientationHelper.VERTICAL, false));
    }

    @Override
    protected void onRightMenu() {
        super.onRightMenu();
        Intent bindIntent = new Intent(DeviceListActivity.this, BindActivity.class);
        startActivityForResult(bindIntent, BIND_DEVICE);
    }

    public void initData() {
        sc = new CompositeSubscription();
        if (getIntent().hasExtra("devices")) {
            machineInfoList = getIntent().getParcelableExtra("devices");
        }
        deviceListAdapter = new DeviceListAdapter(R.layout.device_list_item, machineInfoList.devices);
        deviceListAdapter.setItemClickCallBack(new ItemClickCallBack() {
            @Override
            public void onLongClick(View view) throws Exception {
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

            @Override
            public void onClick(View view) throws Exception {
                final MachineInfo machineInfo = machineInfoList.devices.get((Integer) view.getTag());
                getDeviceDetail(machineInfo);
            }
        });
        deviceList.setAdapter(deviceListAdapter);
        emptyView.setVisibility(CommonUtils.isEmpty(machineInfoList.devices)?View.VISIBLE:View.GONE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
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
                .subscribe(new Subscriber<DeviceListData>() {

                    @Override
                    public void onCompleted() {
                        closeLoadingView();
                        LogUtils.d(BaseActivity.Tag, "getDeviceList onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        closeLoadingView();
                        showErrorMessage(e);
                        LogUtils.d(BaseActivity.Tag, "getDeviceList onError");
                    }

                    @Override
                    public void onNext(DeviceListData bindDeviceList) {
                        closeLoadingView();
                        machineInfoList.devices.clear();
                        deviceListAdapter.clearDevicesListener();
                        if (bindDeviceList != null && bindDeviceList.devices != null && bindDeviceList.devices.size() > 0) {
                            machineInfoList.devices.addAll(bindDeviceList.devices);
                            deviceListAdapter.setNewData(machineInfoList.devices);
                            emptyView.setVisibility(View.GONE);
                        }else{
                            emptyView.setVisibility(View.VISIBLE);
                        }
                    }
                });
        sc.add(subscribe);
    }

    public void unbindDevice(MachineInfo machineInfo) {
        showLoadingView();
        Subscription subscribe = FamilyApiWrapper.getInstance().unBindDevice(machineInfo.serial_num)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Object>() {

                    @Override
                    public void onCompleted() {
                        closeLoadingView();
                        LogUtils.d(BaseActivity.Tag, "unBindDevice onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        closeLoadingView();
                        showErrorMessage(e);
                        LogUtils.d(BaseActivity.Tag, "unBindDevice onError");
                    }

                    @Override
                    public void onNext(Object result) {
                        closeLoadingView();
                        setResult(RESULT_OK);
                        getDeviceList();
                    }
                });
        sc.add(subscribe);
    }

    public void getDeviceDetail(MachineInfo machineInfo) {
        showLoadingView();
        Subscription subscribe = FamilyApiWrapper.getInstance().getDeviceDetail(machineInfo.serial_num)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<DeviceInfo>() {

                    @Override
                    public void onCompleted() {
                        closeLoadingView();
                        LogUtils.d(BaseActivity.Tag, "getDeviceDetail onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        closeLoadingView();
                        showErrorMessage(e);
                        LogUtils.d(BaseActivity.Tag, "getDeviceDetail onError");
                    }

                    @Override
                    public void onNext(DeviceInfo result) {
                        closeLoadingView();
                        Bundle bundle = new Bundle();
                        bundle.putString("name",result.getName());
                        bundle.putString("dtype",result.getDtype());
                        bundle.putString("father_name",result.getFather_name());
                        bundle.putString("mother_name",result.getMother_name());
                        bundle.putString("sn",result.getSn());
                        bundle.putString("birth",String.valueOf(result.getBirthday()));
                        bundle.putString("gender",String.valueOf(result.getGender()));
                        bundle.putString("wakeup",!TextUtils.isEmpty(result.getWakeup()) ? result.getWakeup() : "");
                        NavigationHelper.startActivity(DeviceListActivity.this,DeviceSettingActivity.class,bundle,false);
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
