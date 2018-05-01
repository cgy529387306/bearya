package com.bearya.robot.household.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bearya.robot.household.MyApplication;
import com.bearya.robot.household.R;
import com.bearya.robot.household.adapter.MachineAdapter;
import com.bearya.robot.household.adapter.MenuAdapter;
import com.bearya.robot.household.api.FamilyApiWrapper;
import com.bearya.robot.household.carouselList.CarouselLayoutManager;
import com.bearya.robot.household.carouselList.CarouselZoomPostLayoutListener;
import com.bearya.robot.household.carouselList.CenterScrollListener;
import com.bearya.robot.household.carouselList.DefaultChildSelectionListener;
import com.bearya.robot.household.entity.DeviceListData;
import com.bearya.robot.household.entity.GlideCircleTransform;
import com.bearya.robot.household.entity.ItemCallback;
import com.bearya.robot.household.entity.MachineInfo;
import com.bearya.robot.household.entity.MenuInfo;
import com.bearya.robot.household.entity.UserInfo;
import com.bearya.robot.household.services.UpdateAppService;
import com.bearya.robot.household.utils.CommonUtils;
import com.bearya.robot.household.utils.LogUtils;
import com.bearya.robot.household.utils.NavigationHelper;
import com.bearya.robot.household.utils.UserInfoManager;
import com.bearya.robot.household.videoCall.AgoraService;
import com.bearya.robot.household.videoCall.RxConstants;
import com.bearya.robot.household.views.BaseActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.thread.EventThread;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class MainActivity extends BaseActivity implements View.OnClickListener {
    private final static int BIND_DEVICE = 906;
    private final static int DEVICE_LIST = 905;
    private final static int HIDE_MENU = 906;
    private ImageView setting;
    private TextView version;
    private DrawerLayout drawerLayout;
    private RelativeLayout leftMenu;
    private TextView mDate;
    private RecyclerView menus;
    private RecyclerView machines;
    private ImageView userIcon;
    private TextView userName;
    private TextView bindHit;
    private MenuAdapter menuAdapter;
    private MachineAdapter machineAdapter;
    private List<MenuInfo> menuInfoList = new ArrayList<>();
    private DeviceListData machineInfoList = new DeviceListData();
    private int[] hints = {R.string.main_device_hint_1, R.string.main_device_hint_2, R.string.main_device_hint_3, R.string.main_device_hint_4,
            R.string.main_device_hint_5, R.string.main_device_hint_6, R.string.main_device_hint_7, R.string.main_device_hint_8};
    private CompositeSubscription sc;
    private UserInfo userInfo;
    private Random mRandom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportExit(true);
        initView();
        initData();
        initUserInfo();
        getDeviceList();
    }

    private void initView() {
        drawerLayout = (DrawerLayout) findViewById(R.id.dl_content_main_menu);
        leftMenu = (RelativeLayout) findViewById(R.id.ll_left_menu);
        userIcon = (ImageView) findViewById(R.id.im_head_portrait);
        userName = (TextView) findViewById(R.id.tv_name);
        machines = (RecyclerView) findViewById(R.id.rv_bind_machine);
        menus = (RecyclerView) findViewById(R.id.rv_setting_menu);
        setting = (ImageView) findViewById(R.id.im_setting);
        bindHit = (TextView) findViewById(R.id.tv_add_machine_hint);
        mDate = (TextView) findViewById(R.id.tv_add_machine_date);
        version = (TextView) findViewById(R.id.tv_app_version);
        setting.setOnClickListener(this);
        findViewById(R.id.im_control).setOnClickListener(this);
    }

    private void initData() {
        version.setText(String.format("V%s", CommonUtils.getVersionName(MyApplication.getContext())));
        sc = new CompositeSubscription();
        machineInfoList.list = new ArrayList<>();
        menuInfoList.add(new MenuInfo(R.mipmap.menu_manage, R.string.menu_manage, true,  new ItemCallback() {
            @Override
            public void itemClick() {
                drawerLayout.closeDrawer(leftMenu);
                Intent intent = new Intent(MainActivity.this, DeviceListActivity.class);
                DeviceListData deviceList = new DeviceListData();
                deviceList.list = new ArrayList<>();
                if (!(machineInfoList.list.size() == 1 && machineInfoList.list.get(0).uid == 0)){
                    deviceList.list.addAll(machineInfoList.list);
                }
                intent.putExtra("devices", deviceList);
                startActivityForResult(intent, DEVICE_LIST);
            }
        }));
        menuInfoList.add(new MenuInfo(R.mipmap.menu_setting, R.string.menu_setting, false, new ItemCallback() {
            @Override
            public void itemClick() {
                drawerLayout.closeDrawer(leftMenu);
                NavigationHelper.startActivity(MainActivity.this,SettingActivity.class,null,false);
            }
        }));
        menuAdapter = new MenuAdapter(R.layout.menu_item_view, menuInfoList);
        menuAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                menuInfoList.get(i).itemCallback.itemClick();
            }
        });
        menus.setLayoutManager(new GridLayoutManager(this, 1, OrientationHelper.VERTICAL, false));
        menus.setAdapter(menuAdapter);
        //machineInfoList.devices.add(new MachineInfo(0,"","XB","",0));//无设备时
        machineAdapter = new MachineAdapter(R.layout.machine_item_view, machineInfoList.list);
        initRecyclerView(machines, new CarouselLayoutManager(CarouselLayoutManager.HORIZONTAL, false), machineAdapter);
        //machines.setLayoutManager(new GridLayoutManager(this, 1, OrientationHelper.HORIZONTAL, false));
        //machines.setAdapter(machineAdapter);
    }

    private void initRecyclerView(final RecyclerView recyclerView, final CarouselLayoutManager layoutManager, final MachineAdapter adapter) {
        // enable zoom effect. this line can be customized
        layoutManager.setPostLayoutListener(new CarouselZoomPostLayoutListener(136, 0.5f));
        layoutManager.setMaxVisibleItems(1);

        recyclerView.setLayoutManager(layoutManager);
        // we expect only fixed sized item for now
        recyclerView.setHasFixedSize(true);
        // sample adapter with random data
        recyclerView.setAdapter(adapter);
        // enable center post scrolling
        recyclerView.addOnScrollListener(new CenterScrollListener());
        // enable center post touching on item and item click listener
        DefaultChildSelectionListener.initCenterItemListener(new DefaultChildSelectionListener.OnCenterItemClickListener() {
            @Override
            public void onCenterItemClicked(@NonNull final RecyclerView recyclerView, @NonNull final CarouselLayoutManager carouselLayoutManager, @NonNull final View v) {
                final int position = recyclerView.getChildLayoutPosition(v);
                if (machineInfoList.list.get(position).uid >0) {
                    Intent controlIntent = new Intent(MainActivity.this, ControlActivity.class);
                    controlIntent.putExtra("deviceInfo", machineInfoList.list.get(position));
                    startActivity(controlIntent);
                } else {
                    Intent bindIntent = new Intent(MainActivity.this, BindActivity.class);
                    startActivityForResult(bindIntent, BIND_DEVICE);
                }
            }
        }, recyclerView, layoutManager);

        layoutManager.addOnItemSelectionListener(new CarouselLayoutManager.OnCenterItemSelectionListener() {

            @Override
            public void onCenterItemChanged(final int adapterPosition) {
                if (CarouselLayoutManager.INVALID_POSITION != adapterPosition) {
                    Log.d("NOWCENTER", "adapterPosition = " + adapterPosition);
                } else {
                    Log.d("NOWCENTER", " else adapterPosition = " + adapterPosition);
                }
            }
        });
    }

    public void initUserInfo() {
        userInfo = UserInfoManager.getInstance().getUserInfo();
        if (userInfo != null) {
            LogUtils.d(Tag, "userInfo:"+userInfo.getNickname()+" userInfo.uid:"+userInfo.getUid());
            Glide.with(this)
                    .load(userInfo.getAvatar())
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .transform(new GlideCircleTransform(this))
                    .error(R.mipmap.ic_avatar_default)
                    .into(userIcon);
            userName.setText(TextUtils.isEmpty(userInfo.getNickname())?"贝芽科技":userInfo.getNickname());
            startVideoCallService();
        } else {
            Toast.makeText(this, R.string.login_error_more, Toast.LENGTH_SHORT).show();
            UserInfoManager.getInstance().loginOut();
            launcherLogin();
        }
    }

    public void startVersionCheckService() {
        startService(new Intent(this, UpdateAppService.class));
    }

    public void startVideoCallService() {
        startService(new Intent(this, AgoraService.class));
    }

    public void stopVideoCallService() {
        stopService(new Intent(this, AgoraService.class));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.im_setting:
                if (drawerLayout.isDrawerOpen(leftMenu)) {
                    drawerLayout.closeDrawer(leftMenu);
                } else {
                    drawerLayout.openDrawer(leftMenu);
                }
                break;
            case R.id.im_control:
                NavigationHelper.startActivity(MainActivity.this,UserInfoActivity.class,null,false);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAppVersion(this);
        getDeviceList();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (drawerLayout.isDrawerOpen(leftMenu)) {
            drawerLayout.closeDrawer(leftMenu);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && (requestCode == BIND_DEVICE || requestCode == DEVICE_LIST)) {
            getDeviceList();
        }
    }

    public void setMachineHint() {
        if (mRandom == null) {
            mRandom = new Random();
        }
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy/MM/dd ");
        String date = sDateFormat.format(new java.util.Date());
        String hintString = getString(hints[mRandom.nextInt(hints.length)]);
        bindHit.setText(hintString);
        mDate.setText(date);
        bindHit.setVisibility(View.VISIBLE);
        mDate.setVisibility(View.VISIBLE);
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
                        if (machineInfoList.list.size() <= 0) {
                            machineInfoList.list.add(new MachineInfo(0,"","XB","",0));//无设备时
                        }
                        machineAdapter.setNewData(machineInfoList.list);
                        setMachineHint();
                    }

                    @Override
                    public void onError(Throwable e) {
                        closeLoadingView();
                        LogUtils.d(BaseActivity.Tag, "getDeviceList onError");
                        showErrorMessage(e);
                        if (machineInfoList.list.size() <= 0) {
                            machineInfoList.list.add(new MachineInfo(0,"","XB","",0));//无设备时
                            machineAdapter.setNewData(machineInfoList.list);
                            setMachineHint();
                        }
                    }

                    @Override
                    public void onNext(DeviceListData bindDeviceList) {
                        closeLoadingView();
                        LogUtils.d(BaseActivity.Tag, "getDeviceList onNext");
                        machineInfoList.list.clear();
                        machineAdapter.clearDevicesListener();
                        if (bindDeviceList != null && bindDeviceList.list != null){
                            if (bindDeviceList.list.size() > 0) {
                                machineInfoList.list.addAll(bindDeviceList.list);
                            }
                        }
                    }
                });
        sc.add(subscribe);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(sc != null) {
            sc.unsubscribe();
        }
        if (machineAdapter != null) {
            machineAdapter.clearDevicesListener();
        }
        stopVideoCallService();
    }
}
