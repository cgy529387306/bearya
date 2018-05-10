package com.bearya.robot.household.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import com.bearya.robot.household.R;
import com.bearya.robot.household.adapter.DeviceListAdapter;
import com.bearya.robot.household.api.FamilyApiWrapper;
import com.bearya.robot.household.entity.DeviceListData;
import com.bearya.robot.household.entity.ItemClickCallBack;
import com.bearya.robot.household.entity.MachineInfo;
import com.bearya.robot.household.utils.LogUtils;
import com.bearya.robot.household.utils.NavigationHelper;
import com.bearya.robot.household.views.BYCheckDialog;
import com.bearya.robot.household.views.BaseActivity;
import com.bearya.robot.household.views.DialogCallback;
import com.bearya.robot.household.views.SwipeItemLayout;
import com.wuxiaolong.pullloadmorerecyclerview.PullLoadMoreRecyclerView;

import java.util.ArrayList;
import java.util.List;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class DeviceListActivity extends BaseActivity implements View.OnClickListener{
    private final static int BIND_DEVICE = 9003;
    private final static int EDIT_DEVICE = 9004;
    private List<MachineInfo> machineInfoList = new ArrayList<>();
    private DeviceListAdapter deviceListAdapter;
    private PullLoadMoreRecyclerView deviceList;
    private BYCheckDialog checkDialog;
    private CompositeSubscription subscription;
    private FrameLayout emptyView;
    private int currentPage = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.string.menu_manage,R.mipmap.icon_add,R.layout.activity_device_list);
        initView();
        initData();
        showLoadingView();
        getDeviceList();
    }

    public void initView() {
        emptyView = (FrameLayout) findViewById(R.id.emptyView);
        deviceList = (PullLoadMoreRecyclerView) findViewById(R.id.rv_bind_machine);
        deviceList.setLinearLayout();
        deviceList.setPullRefreshEnable(true);
        deviceList.setPushRefreshEnable(true);
        deviceList.getRecyclerView().addOnItemTouchListener(new SwipeItemLayout.OnSwipeItemTouchListener(this));
        deviceList.setOnPullLoadMoreListener(new PullLoadMoreRecyclerView.PullLoadMoreListener() {
            @Override
            public void onRefresh() {
                refresh();
            }

            @Override
            public void onLoadMore() {
                currentPage++;
                getDeviceList();
            }
        });
    }

    private void refresh(){
        deviceList.setPushRefreshEnable(true);
        currentPage = 1;
        machineInfoList.clear();
        deviceListAdapter.clearDevicesListener();
        getDeviceList();
    }

    @Override
    protected void onRightMenu() {
        super.onRightMenu();
        Intent bindIntent = new Intent(DeviceListActivity.this, BindActivity.class);
        startActivityForResult(bindIntent, BIND_DEVICE);
    }

    public void initData() {
        subscription = new CompositeSubscription();
        deviceListAdapter = new DeviceListAdapter(R.layout.device_list_item, machineInfoList);
        deviceListAdapter.setItemClickCallBack(new ItemClickCallBack() {
            @Override
            public void onDeleteClick(final MachineInfo machineInfo) {
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
            public void onClick(MachineInfo machineInfo){
                Bundle bundle = new Bundle();
                bundle.putString("sn",machineInfo.sn);
                NavigationHelper.startActivityForResult(DeviceListActivity.this,DeviceSettingActivity.class,bundle, EDIT_DEVICE);
            }
        });
        deviceList.setAdapter(deviceListAdapter);
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
            refresh();
            setResult(RESULT_OK);
            Bundle bundle = new Bundle();
            bundle.putBoolean("isFirst",true);
            if (data!=null){
                String sn = data.getStringExtra("sn");
                bundle.putString("sn",sn);
            }
            NavigationHelper.startActivity(DeviceListActivity.this,DeviceSettingActivity.class,bundle,false);
        }else if (requestCode == EDIT_DEVICE && resultCode == Activity.RESULT_OK) {
            refresh();
        }
    }

    public void getDeviceList() {
        Subscription subscribe = FamilyApiWrapper.getInstance().getDeviceList(String.valueOf(currentPage),"20")
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
                        deviceList.setPullLoadMoreCompleted();
                        if (bindDeviceList != null && bindDeviceList.list != null && bindDeviceList.list.size() > 0) {
                            emptyView.setVisibility(View.GONE);
                            if (!bindDeviceList.isHasNext()){
                                deviceList.setPushRefreshEnable(false);
                            }
                            machineInfoList.addAll(bindDeviceList.list);
                            deviceListAdapter.setNewData(machineInfoList);
                        }else{
                           if (currentPage == 1){
                               emptyView.setVisibility(View.VISIBLE);
                           }
                        }
                    }
                });
        subscription.add(subscribe);
    }

    public void unbindDevice(MachineInfo machineInfo) {
        showLoadingView();
        Subscription subscribe = FamilyApiWrapper.getInstance().unBindDevice(machineInfo.sn)
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
                        refresh();
                    }
                });
        subscription.add(subscribe);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (deviceListAdapter != null) {
            deviceListAdapter.clearDevicesListener();
        }
    }
}
