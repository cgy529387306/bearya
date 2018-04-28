package com.bearya.robot.household.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bearya.robot.household.R;
import com.bearya.robot.household.adapter.DanceListAdapter;
import com.bearya.robot.household.adapter.ExpressionListAdapter;
import com.bearya.robot.household.adapter.VideoListAdapter;
import com.bearya.robot.household.api.FamilyApiWrapper;
import com.bearya.robot.household.entity.ItemInfo;
import com.bearya.robot.household.entity.KeyInfo;
import com.bearya.robot.household.entity.MachineInfo;
import com.bearya.robot.household.entity.MsgMonitor;
import com.bearya.robot.household.entity.UserInfo;
import com.bearya.robot.household.networkInteraction.BYValueEventListener;
import com.bearya.robot.household.networkInteraction.FamilyInteraction;
import com.bearya.robot.household.services.MonitorService;
import com.bearya.robot.household.utils.CommonUtils;
import com.bearya.robot.household.utils.LogUtils;
import com.bearya.robot.household.utils.UserInfoManager;
import com.bearya.robot.household.videoCall.AgoraService;
import com.bearya.robot.household.videoCall.VideoChatViewActivity;
import com.bearya.robot.household.videoCall.VoiceChatViewActivity;
import com.bearya.robot.household.views.BaseActivity;
import com.chad.library.adapter.base.BaseQuickAdapter;

import java.util.ArrayList;
import java.util.List;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class ControlActivity extends BaseActivity implements View.OnClickListener, BYValueEventListener {
    private final static int MSG_HIDE_ACTION_TEXT = 10000;
    private final static int MODE_NORMAL = 0;
    private final static int MODE_EXPRESSION = 1;
    private final static int MODE_DANCE = 2;
    private final static int MODE_VIDEO = 3;
    private final String TAG = "ControlActivity";
    private RelativeLayout rlControlView;
    private FrameLayout sfvContainer;
    private ImageView closeMonitor;
    private ProgressBar loading;
    private TextView deviceState;
    private TextView deviceAction;

    private LinearLayout llyExpressions;
    private LinearLayout llyDances;
    private LinearLayout llyVideo;
    private LinearLayout llyMessage;

    private ImageView ivExpressions;
    private ImageView ivDances;
    private ImageView ivVideo;
    private ImageView ivMessage;

    private RelativeLayout rlSendMsg;
    private EditText inputMsg;
    private ImageView sendMsg;

    private LinearLayout llBottomView;
    private RelativeLayout rlBottomControl;
    private RelativeLayout rlMoreInfo;
    private RecyclerView rvExpressions;
    private RecyclerView rvDances;
    private RecyclerView rvVideos;
    private RelativeLayout msgSendView;

    private MachineInfo deviceInfo;
    private UserInfo userInfo;
    //private SyncReference mStateRef;
    //private SyncReference mDeviceRef;
    //private ValueEventListener stateValueEventListener;
    //private ValueEventListener deviceEventListener;
    private FamilyInteraction familyInteraction;
    private MonitorService monitorService;
    private CompositeSubscription sc;
    private List<ItemInfo> expressionListInfo = new ArrayList<>();
    private List<ItemInfo> danceListInfo = new ArrayList<>();
    private List<ItemInfo> videoListInfo = new ArrayList<>();
    private ExpressionListAdapter expressionListAdapter;
    private DanceListAdapter danceListAdapter;
    private VideoListAdapter videoListAdapter;
    private String [] actions;
    private boolean isNeedInit = true;
    private int isMonitor = -1;
    private int isOnLine = -1;
    private int mMode = 0;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_HIDE_ACTION_TEXT:
                    deviceAction.setText("");
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        sc = new CompositeSubscription();
        initView();
        initData();
    }

    private void initView() {
        rlControlView =(RelativeLayout)  findViewById(R.id.rl_control_view);
        rlControlView.setOnClickListener(this);
        //监护视频窗口
        sfvContainer =(FrameLayout) findViewById(R.id.rl_remote_video_view_container);
        loading = (ProgressBar) findViewById(R.id.pb_loading);
        closeMonitor = (ImageView) findViewById(R.id.im_close_monitor);
        closeMonitor.setOnClickListener(this);
        deviceState = (TextView) findViewById(R.id.tv_device_state);
        deviceAction = (TextView) findViewById(R.id.tv_device_action);

        //上、下、左、右控制
        findViewById(R.id.im_turn_up).setOnClickListener(this);
        findViewById(R.id.im_turn_down).setOnClickListener(this);
        findViewById(R.id.im_turn_left).setOnClickListener(this);
        findViewById(R.id.im_turn_right).setOnClickListener(this);
        findViewById(R.id.iv_back).setOnClickListener(this);
        actions = getResources().getStringArray(R.array.action_names);

        llyExpressions = (LinearLayout)findViewById(R.id.action_expressions);
        llyDances = (LinearLayout)findViewById(R.id.action_dances);
        llyVideo = (LinearLayout)findViewById(R.id.action_videos);
        llyMessage = (LinearLayout)findViewById(R.id.action_messages);
        llyExpressions.setOnClickListener(this);
        llyDances.setOnClickListener(this);
        llyVideo.setOnClickListener(this);
        llyMessage.setOnClickListener(this);

        ivExpressions = (ImageView) findViewById(R.id.iv_expressions);
        ivDances = (ImageView)findViewById(R.id.iv_dance);
        ivVideo = (ImageView)findViewById(R.id.iv_monitor);
        ivMessage = (ImageView)findViewById(R.id.iv_walls);

        //信息发送相关
        llBottomView = (LinearLayout) findViewById(R.id.rl_bottom_view);
        rlBottomControl = (RelativeLayout) findViewById(R.id.rl_bottom_control);
        msgSendView = (RelativeLayout) findViewById(R.id.rl_send_tts_msg);
        rlSendMsg = (RelativeLayout) findViewById(R.id.rl_send_tts_msg);
        inputMsg = (EditText) findViewById(R.id.et_input_msg);
        sendMsg = (ImageView)findViewById(R.id.im_send_msg);
        msgSendView.setOnClickListener(this);
        sendMsg.setOnClickListener(this);

        rlMoreInfo = (RelativeLayout) findViewById(R.id.rl_more_info);
        rvExpressions = (RecyclerView) findViewById(R.id.rv_expressions);
        rvDances = (RecyclerView) findViewById(R.id.rv_dances);
        rvVideos = (RecyclerView) findViewById(R.id.rv_videos);

        inputMsg.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String e = editable.toString();
                sendMsg.setSelected(!TextUtils.isEmpty(e));
            }
        });

        rvExpressions.setLayoutManager(new GridLayoutManager(this, 1, OrientationHelper.HORIZONTAL, false));
        rvDances.setLayoutManager(new GridLayoutManager(this, 1, OrientationHelper.HORIZONTAL, false));
        rvVideos.setLayoutManager(new GridLayoutManager(this, 1, OrientationHelper.HORIZONTAL, false));
    }

    private void initData() {
        userInfo = UserInfoManager.getInstance().getUserInfo();
        Log.d(TAG, "AA initData........");
        if (getIntent().hasExtra("deviceInfo")) {
            deviceInfo = getIntent().getParcelableExtra("deviceInfo");
            Log.d(TAG, "deviceInfo.dtype = "+deviceInfo.dtype+" deviceInfo.serial_num = "+deviceInfo.serial_num);
            familyInteraction = new FamilyInteraction();
            familyInteraction.init(deviceInfo.dtype, deviceInfo.serial_num);
            familyInteraction.setValueEventListener(this);
            //addDeviceStateListener(deviceInfo.serial_num);
            //addWildDogDeviceListener(deviceInfo.serial_num);
        }
        monitorService = new MonitorService(sfvContainer, closeMonitor);

        String [] expressionNames = getResources().getStringArray(R.array.expression_names);
        String [] expressionValues = getResources().getStringArray(R.array.expression_values);
        for (int i = 0; i<expressionNames.length; i++) {
            int resID = getResources().getIdentifier("ic_expression"+i, "mipmap", getPackageName());
            expressionListInfo.add(new ItemInfo(expressionValues[i],expressionNames[i],resID));
        }
        expressionListAdapter = new ExpressionListAdapter(R.layout.expression_item_view, expressionListInfo);
        expressionListAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                if (!isDeviceOnLine()) {
                    CommonUtils.showToast(ControlActivity.this, getString(R.string.device_outline_toast));
                    return;
                }
                //MsgExpression msgExpression = new MsgExpression(expressionListInfo.get(position).id);
                setDeviceAction("表情："+expressionListInfo.get(position).name);
                if (familyInteraction != null) {
                    familyInteraction.sendExpression(expressionListInfo.get(position).id);
                }
                //mDeviceRef.child(CommonUtils.WILDDOG_CLIENT).push().setValue(msgExpression);
            }
        });
        rvExpressions.setAdapter(expressionListAdapter);

        String [] danceNames = getResources().getStringArray(R.array.dance_names);
        String [] danceValues = getResources().getStringArray(R.array.dance_values);
        for (int i = 0; i<danceNames.length; i++) {
            int resID = getResources().getIdentifier("ic_dance"+i, "mipmap", getPackageName());
            danceListInfo.add(new ItemInfo(danceValues[i],danceNames[i],resID));
        }
        danceListAdapter = new DanceListAdapter(R.layout.dance_item_view, danceListInfo);
        danceListAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                if (!isDeviceOnLine()) {
                    CommonUtils.showToast(ControlActivity.this, getString(R.string.device_outline_toast));
                    return;
                }
                setDeviceAction("舞蹈："+danceListInfo.get(position).name);
                //MsgDance msgDance = new MsgDance(danceListInfo.get(position).id);
                //mDeviceRef.child(CommonUtils.WILDDOG_CLIENT).push().setValue(msgDance);
                if (familyInteraction != null) {
                    familyInteraction.sendDance(danceListInfo.get(position).id);
                }
            }
        });
        rvDances.setAdapter(danceListAdapter);

        videoListInfo.add(new ItemInfo("0","视频",R.mipmap.img_video));
        videoListInfo.add(new ItemInfo("1","语音",R.mipmap.img_voice));
        videoListInfo.add(new ItemInfo("2","监护",R.mipmap.img_monitor));
        videoListAdapter = new VideoListAdapter(R.layout.video_item_view, videoListInfo);
        videoListAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                if (!isDeviceOnLine()) {
                    CommonUtils.showToast(ControlActivity.this, getString(R.string.device_outline_toast));
                    return;
                }
                if (!CommonUtils.isServiceRunning(ControlActivity.this, AgoraService.class)) {
                    CommonUtils.showToast(ControlActivity.this, getString(R.string.service_start_failed));
                    return;
                }
                deviceAction.setText("");
                if (Integer.valueOf(videoListInfo.get(position).id) == 0) {
                    if (isMonitor >= 0) {
                        CommonUtils.showToast(ControlActivity.this, getString(R.string.monitor_mode_toast));
                        return;
                    }
                    showOrHideBottomInfo(MODE_NORMAL);
                    LogUtils.d("VideoCall", "localId = "+userInfo.getUid() + " remoteId = "+ deviceInfo.uid);
                    Intent intent = new Intent(ControlActivity.this, VideoChatViewActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("beInvited", false);
                    intent.putExtra("isVideo", true);
                    intent.putExtra("remoteName", deviceInfo.name);
                    intent.putExtra("localId", userInfo.getUid());
                    intent.putExtra("remoteId", deviceInfo.uid);// 强转为fromAccount
                    startActivity(intent);
                }else if (Integer.valueOf(videoListInfo.get(position).id) == 1){
                    if (isMonitor >= 0) {
                        CommonUtils.showToast(ControlActivity.this, getString(R.string.monitor_mode_toast1));
                        return;
                    }
                    showOrHideBottomInfo(MODE_NORMAL);
                    LogUtils.d("VideoCall", "localId = "+userInfo.getUid() + " remoteId = "+ deviceInfo.uid);
                    Intent intent = new Intent(ControlActivity.this, VoiceChatViewActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("beInvited", false);
                    intent.putExtra("isVideo", false);
                    intent.putExtra("remoteName", deviceInfo.name);
                    intent.putExtra("localId", userInfo.getUid());
                    intent.putExtra("remoteId", deviceInfo.uid);// 强转为fromAccount
                    startActivity(intent);
                }else if (Integer.valueOf(videoListInfo.get(position).id) == 2) {
                    if (isMonitor == -1) {
                        getMonitorKey();
                    } else if (isMonitor == 1) {
                        stopMonitor();
                    }
                }
            }
        });
        rvVideos.setAdapter(videoListAdapter);

    }

    private void setDeviceAction(String action) {
        mHandler.removeMessages(MSG_HIDE_ACTION_TEXT);
        deviceAction.setText(action);
        mHandler.sendEmptyMessageDelayed(MSG_HIDE_ACTION_TEXT, 2000);
    }

    public boolean isDeviceOnLine() {
        return isOnLine==1;
    }

    public void sendMessageToDevice() {
        String inputMessage = inputMsg.getText().toString();
        if (TextUtils.isEmpty(inputMessage)) {
            //隐藏软键盘
            CommonUtils.hideSoftInput(this, inputMsg);
            //退出文本输入模式
            rlSendMsg.setVisibility(View.GONE);
            return;
        }
        if (!isDeviceOnLine()) {
            CommonUtils.showToast(ControlActivity.this, getString(R.string.device_outline_toast));
            inputMsg.setText("");
            return;
        }
        setDeviceAction("文字："+inputMessage);
        if (familyInteraction != null) {
            familyInteraction.sendTTS(inputMessage);
        }
        //MsgTTS msgTTS = new MsgTTS(inputMessage);
        //mDeviceRef.child(CommonUtils.WILDDOG_CLIENT).push().setValue(msgTTS);
        inputMsg.setText("");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.rl_control_view:
                showOrHideBottomInfo(MODE_NORMAL);
                break;
            case R.id.im_turn_up:
                sendAction(CommonUtils.actionUp);
                setDeviceAction(actions[0]);
                break;
            case R.id.im_turn_down:
                sendAction(CommonUtils.actionDown);
                setDeviceAction(actions[1]);
                break;
            case R.id.im_turn_right:
                sendAction(CommonUtils.actionRight);
                setDeviceAction(actions[2]);
                break;
            case R.id.im_turn_left:
                sendAction(CommonUtils.actionLeft);
                setDeviceAction(actions[3]);
                break;
            case R.id.action_expressions:
                showOrHideBottomInfo(MODE_EXPRESSION);
                break;
            case R.id.action_dances:
                showOrHideBottomInfo(MODE_DANCE);
                break;
            case R.id.action_videos:
                showOrHideBottomInfo(MODE_VIDEO);
                break;
            case R.id.action_messages:
                showOrHideBottomInfo(MODE_NORMAL);
                rlSendMsg.setVisibility(View.VISIBLE);
                break;
            case R.id.im_close_monitor:
                stopMonitor();
                break;
            case R.id.im_send_msg:
                sendMessageToDevice();
                break;
            default:
                break;
        }
    }

    public void sendAction(String speechId) {
        if (!isDeviceOnLine()) {
            CommonUtils.showToast(this, getString(R.string.device_outline_toast));
            return;
        }
        LogUtils.d(Tag, "turn action = "+speechId);
        if (familyInteraction != null) {
            familyInteraction.sendAction(speechId);
        }
        /*MsgAction action = new MsgAction(action);
        mDeviceRef.child(CommonUtils.WILDDOG_CLIENT).push().setValue(action);*/
    }

    public void getMonitorKey() {
        isMonitor = 0;
        showLoadingView();
        Subscription subscribe = FamilyApiWrapper.getInstance().getMonitorKey(deviceInfo.serial_num, userInfo.getUid())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<KeyInfo>() {

                    @Override
                    public void onCompleted() {
                        closeLoadingView();
                        if (isMonitor <= 0) {
                            isMonitor = -1;
                        }
                        LogUtils.d(BaseActivity.Tag, "getMonitorKey onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        closeLoadingView();
                        LogUtils.d(BaseActivity.Tag, "getMonitorKey onError");
                        isMonitor = -1;
                        showErrorMessage(e);
                    }

                    @Override
                    public void onNext(KeyInfo keyInfo) {
                        closeLoadingView();
                        synchronized (ControlActivity.this) {
                            if (keyInfo != null && isMonitor == 0) {
                                isMonitor = 1;
                                deviceState.setVisibility(View.GONE);
                                deviceAction.setVisibility(View.GONE);
                                closeMonitor.setVisibility(View.VISIBLE);
                                sfvContainer.setVisibility(View.VISIBLE);
                                MsgMonitor msgMonitor = new MsgMonitor("start", keyInfo.appid, keyInfo.channel, keyInfo.uid, keyInfo.key.device);
                                LogUtils.d("VideoCall", "keyInfo.appid = " + keyInfo.appid + " keyInfo.channel = " + keyInfo.channel + " keyInfo.uid = " + keyInfo.uid + " keyInfo.key.device = " + keyInfo.key.device);
                                //mDeviceRef.child(CommonUtils.WILDDOG_CLIENT).push().setValue(msgMonitor);
                                if (familyInteraction != null) {
                                    familyInteraction.sendMonitor(msgMonitor);
                                }
                                monitorService.initAgoraEngineAndJoinChannel(keyInfo.appid, keyInfo.channel, keyInfo.key.device, keyInfo.uid);
                            } else {
                                isMonitor = -1;
                            }
                        }
                    }
                });
        sc.add(subscribe);
    }

    public void stopMonitor() {
        synchronized(ControlActivity.this) {
            if (isMonitor == 1) {
                isMonitor = -1;
                sfvContainer.setVisibility(View.GONE);
                closeMonitor.setVisibility(View.GONE);
                deviceState.setVisibility(View.VISIBLE);
                deviceAction.setVisibility(View.VISIBLE);
                monitorService.leaveChannel();
                MsgMonitor msgMonitor = new MsgMonitor("stop", "", "", 0, "");
                //mDeviceRef.child(CommonUtils.WILDDOG_CLIENT).push().setValue(msgMonitor);
                if (familyInteraction != null) {
                    familyInteraction.sendMonitor(msgMonitor);
                }
                sfvContainer.removeAllViews();
            }
        }
    }


    /*
    * 显示隐藏底部表情等列表
    * */
    private void showOrHideBottomInfo(int mode) {
        boolean isShow = true;
        boolean isDoAnimate = true;
        CommonUtils.hideSoftInput(this, inputMsg);//隐藏软键盘
        if (mode == MODE_NORMAL) {
            if (mMode == MODE_NORMAL) {
                return;
            } else {
                mMode = MODE_NORMAL;
                isShow = false;
            }
        } else {
            if (mMode == MODE_NORMAL) {
                mMode = mode;
            } else {
                if (mMode == mode) {
                    mMode = MODE_NORMAL;
                    isShow = false;
                } else {
                    isDoAnimate = false;
                    mMode = mode;
                }
            }
        }
        if (mMode == MODE_EXPRESSION) {
            rvExpressions.scrollToPosition(0);
        } else if (mMode == MODE_DANCE) {
            rvDances.scrollToPosition(0);
        } else if (mMode == MODE_VIDEO) {
            rvVideos.scrollToPosition(0);
        }
        rvExpressions.setVisibility(mMode == MODE_EXPRESSION?View.VISIBLE:View.GONE);
        rvDances.setVisibility(mMode == MODE_DANCE?View.VISIBLE:View.GONE);
        rvVideos.setVisibility(mMode == MODE_VIDEO?View.VISIBLE:View.GONE);
        ivExpressions.setSelected(mMode == MODE_EXPRESSION);
        ivDances.setSelected(mMode == MODE_DANCE);
        ivVideo.setSelected(mMode == MODE_VIDEO);
        if (!isDoAnimate) {
            return;
        }
        ViewGroup.LayoutParams listParams = rlMoreInfo.getLayoutParams();
        Animation mTranslateAnimation = new TranslateAnimation(0, 0, isShow?0:0, isShow?-listParams.height:listParams.height);// 移动
        mTranslateAnimation.setFillEnabled(true);
        mTranslateAnimation.setFillBefore(true);
        mTranslateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ViewGroup.LayoutParams listParams = rlMoreInfo.getLayoutParams();
                ViewGroup.MarginLayoutParams viewParams = (ViewGroup.MarginLayoutParams) llBottomView.getLayoutParams();
                viewParams.bottomMargin = (mMode != MODE_NORMAL?0:(-listParams.height));
                llBottomView.setLayoutParams(viewParams);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mTranslateAnimation.setDuration(200);
        mTranslateAnimation.setRepeatCount(0);
        llBottomView.startAnimation(mTranslateAnimation);
    }

    /*
    * 显示底部控制栏
    * */
    private void showBottomControlView() {
        ViewGroup.LayoutParams listParams = rlMoreInfo.getLayoutParams();
        ViewGroup.LayoutParams viewParams = llBottomView.getLayoutParams();
        Animation mTranslateAnimation = new TranslateAnimation(0, 0, viewParams.height, listParams.height);// 移动
        mTranslateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ViewGroup.LayoutParams listParams = rlMoreInfo.getLayoutParams();
                ViewGroup.MarginLayoutParams viewParams = (ViewGroup.MarginLayoutParams) llBottomView.getLayoutParams();
                viewParams.bottomMargin = -listParams.height;
                llBottomView.setLayoutParams(viewParams);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mTranslateAnimation.setFillEnabled(true);
        mTranslateAnimation.setFillBefore(true);
        mTranslateAnimation.setDuration(100);
        mTranslateAnimation.setRepeatCount(0);
        llBottomView.startAnimation(mTranslateAnimation);
        llBottomView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopMonitor();//停止监护
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isNeedInit) {
            showBottomControlView();
            isNeedInit = false;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //mStateRef.removeEventListener(stateValueEventListener);
        //mDeviceRef.child(CommonUtils.WILDDOG_SERVER).removeEventListener(deviceEventListener);
        if (familyInteraction != null) {
            familyInteraction.close();
        }
        if (sc != null) {
            sc.unsubscribe();
        }
    }

    @Override
    public void onStateChange(boolean isOnline) {
        if (isOnline) {
            isOnLine = 1;//在线
            deviceState.setText(getString(R.string.machine_state_inline));
            deviceState.setSelected(true);
        } else {
            isOnLine = 0;//离线
            deviceState.setText(getString(R.string.machine_state_outline));
            deviceState.setSelected(false);
        }
        LogUtils.d("UserWildDog", "onDataChange isOnLine = "+isOnLine);
    }

    @Override
    public void onDataChange(String data) {

    }
}
