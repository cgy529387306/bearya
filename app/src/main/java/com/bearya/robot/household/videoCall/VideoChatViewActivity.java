package com.bearya.robot.household.videoCall;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bearya.robot.household.R;
import com.bearya.robot.household.media.DynamicKeyUtil;
import com.bearya.robot.household.utils.CommonUtils;
import com.bearya.robot.household.videoCall.beans.AgoraEventDispatch;
import com.bearya.robot.household.videoCall.beans.AgoraRunTime;
import com.bearya.robot.household.videoCall.beans.AgoraTransferBean;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.victor.loading.rotate.RotateLoading;

import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;

import static io.agora.rtc.Constants.ERR_LEAVE_CHANNEL_REJECTED;

public class VideoChatViewActivity extends Activity {
    private RotateLoading headerProgressLoading;
    private TextView avTitleTextView;
    private TextView avSubtitle;
    private ImageView remoteUserIv;
    private Chronometer avTimeChronometer;
    private FrameLayout localContainer;
    private View callInLayout, callInviteLayout;
    private TextView tvMute,tvSwitch;
    private int localId;// 本地的用户id
    private int remoteId;
    private String remoteName;
    private String remotePic;
    private String localPic;
    private String channelId;
    private boolean isVideo;
    private boolean isInvitedFromRemote;
    private RtcEngine mRtcEngine;
    private AgoraTransferBean bean;

    private static final String LOG_TAG = VideoChatViewActivity.class.getSimpleName();
    private static final int PERMISSION_REQ_ID_RECORD_AUDIO = 22;
    private static final int PERMISSION_REQ_ID_CAMERA = PERMISSION_REQ_ID_RECORD_AUDIO + 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_chat_view);
        localContainer = (FrameLayout) findViewById(R.id.local_video_view_container);
        headerProgressLoading = (RotateLoading) findViewById(R.id.audio_header_progress);
        callInLayout = findViewById(R.id.callin_layout);
        callInviteLayout = findViewById(R.id.call_invite_layout);
        tvMute = (TextView) findViewById(R.id.tv_mute);
        tvSwitch = (TextView) findViewById(R.id.tv_switch);
        remoteUserIv = (ImageView) findViewById(R.id.iv_avatar);
        avTitleTextView = (TextView) findViewById(R.id.av_title_textview);
        avSubtitle = (TextView) findViewById(R.id.av_subtitle);
        avTimeChronometer = (Chronometer) findViewById(R.id.av_time_textview);

        tvMute = (TextView) findViewById(R.id.tv_mute);

        remoteId = getIntent().getExtras().getInt("remoteId");
        localId = getIntent().getExtras().getInt("localId");
        localPic = getIntent().getExtras().getString("localPic");
        remotePic = getIntent().getExtras().getString("remotePic");
        remoteName = getIntent().getExtras().getString("remoteName");
        isVideo = getIntent().getExtras().getBoolean("isVideo", true);
        Glide.with(this).load(remotePic).error(R.mipmap.ic_robot_avatar).into(new SimpleTarget<GlideDrawable>() {
            @Override
            public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                remoteUserIv.setImageDrawable(resource);
            }
        });
        remoteName = TextUtils.isEmpty(remoteName) ? "" : remoteName;
        isInvitedFromRemote = getIntent().getExtras().getBoolean("beInvited", false);

        if (!isInvitedFromRemote) {
            // 拨号对方作为channel
            channelId = remoteId + "";
            avTitleTextView.setText("正在呼叫" + remoteName);
            avSubtitle.setVisibility(View.GONE);
            callInLayout.setVisibility(View.GONE);
            callInviteLayout.setVisibility(View.VISIBLE);
            headerProgressLoading.start();
        } else {
            // 外部呼叫
            channelId = localId + "";
            avTitleTextView.setText(TextUtils.isEmpty(remoteName)?"小贝":remoteName);
            avSubtitle.setVisibility(View.VISIBLE);
            callInLayout.setVisibility(View.VISIBLE);
            localContainer.setVisibility(View.GONE);
            callInviteLayout.setVisibility(View.GONE);
            headerProgressLoading.setVisibility(View.GONE);
        }

        bean = new AgoraTransferBean(channelId, remoteId + "", localId, "", false, "", 0, localPic);
        bean.setVideoCall(isVideo);


        if (isVideo) {
            localContainer.setVisibility(View.VISIBLE);
        } else {
            localContainer.setVisibility(View.GONE);
        }

        if (TextUtils.isEmpty(channelId)) {
            Toast.makeText(this, "视频通话无法接通！", Toast.LENGTH_SHORT).show();
            finish();
        }
        RxBus.get().register(this);
        if (!CommonUtils.isServiceRunning(this, AgoraService.class)) {
            bean.setJoinChannelAfterLogined(true);
            startService(new Intent(this, AgoraService.class).putExtra("myUid", "" + localId));
        }
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID_RECORD_AUDIO) && checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA)) {
            initSignal();
        } else {
            // 需要允许访问录音和视频,否则
        }
        RxConstants.isCalling = true;
    }


    private void initSignal() {
        if (mRtcEngine == null) {
            try {
                mRtcEngine = RtcEngine.create(this, AgoraCalculateHelp.appID, mRtcEventHandler);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        mRtcEngine.enableVideo();
        SurfaceView surfaceView = RtcEngine.CreateRendererView(this);
        surfaceView.setZOrderMediaOverlay(true);
        localContainer.addView(surfaceView);
        mRtcEngine.setupLocalVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_ADAPTIVE, 0));
        mRtcEngine.startPreview();

        if (!isInvitedFromRemote) {
            // 主动拨打
            RxBus.get().post(RxConstants.RxEventTag.TAG_AGORA_SERVICE,
                    new AgoraEventDispatch(RxConstants.EVENT_CHECKLOGIN_AND_JOIN_CHANNEL, bean
                    ));
        } else {
            // 外部呼入
            bean.setBeInvitedTojoin(true);
            localContainer.setVisibility(View.GONE);
        }
    }


    public void onLocalAudioMuteClicked(View view) {
        //静音
        TextView iv = (TextView) view;
        if (iv.isSelected()) {
            iv.setSelected(false);
        } else {
            iv.setSelected(true);
        }
        mRtcEngine.muteLocalAudioStream(iv.isSelected());
    }

    public void onSwitchCameraClicked(View view) {
        TextView iv = (TextView) view;
        if (iv.isSelected()) {
            iv.setSelected(false);
        } else {
            iv.setSelected(true);
        }
        if (bean.isRtcEngineJoinChannel()) {
            mRtcEngine.switchCamera();
        }
    }

    /**
     * 接听电话  channleID, account, uid
     */
    public void onAnswerVideoClicked(View view) {
        avSubtitle.setVisibility(View.GONE);
        callInLayout.setVisibility(View.GONE);
        callInviteLayout.setVisibility(View.VISIBLE);
        tvMute.setVisibility(View.VISIBLE);
        tvSwitch.setVisibility(View.VISIBLE);
        RxBus.get().post(RxConstants.RxEventTag.TAG_AGORA_SERVICE, new AgoraEventDispatch(RxConstants.EVENT_CHANNEL_INVITE_ACCEPT, bean));
    }

    /**
     * 挂断电话
     */
    public void onHangupVideoClicked(View view) {
        showLongToast("正在挂断...");
        mRtcEngine.muteAllRemoteAudioStreams(true);
        RxBus.get().post(RxConstants.RxEventTag.TAG_AGORA_SERVICE, new AgoraEventDispatch(isInvitedFromRemote ? RxConstants.EVENT_CHANNEL_INVITE_REFUSE : RxConstants.EVENT_CHANNEL_INVITE_END, bean));
    }

    /**
     * 建立远程视频连接
     *
     * @param uid
     */
    private void setupRemoteVideo(int uid) {
        FrameLayout container = (FrameLayout) findViewById(R.id.remote_video_view_container);
        if (container.getChildCount() >= 1) {
            return;
        }
        SurfaceView surfaceView = RtcEngine.CreateRendererView(getBaseContext());
        container.addView(surfaceView);
        mRtcEngine.setupRemoteVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_ADAPTIVE, uid));
        surfaceView.setTag(uid); // for mark purpose
        avTitleTextView.setVisibility(View.GONE);
        remoteUserIv.setVisibility(View.GONE);
        tvMute.setVisibility(View.VISIBLE);
        tvSwitch.setVisibility(View.VISIBLE);
        avTimeChronometer.setVisibility(View.VISIBLE);
        avTimeChronometer.setBase(SystemClock.elapsedRealtime());
        avTimeChronometer.start();
    }

    private void onRemoteUserLeft() {
        FrameLayout container = (FrameLayout) findViewById(R.id.remote_video_view_container);
        container.removeAllViews();
        showLongToast("对方已挂断");
        finish();
    }

    private void onRemoteUserVideoMuted(int uid, boolean muted) {
        FrameLayout container = (FrameLayout) findViewById(R.id.remote_video_view_container);

        SurfaceView surfaceView = (SurfaceView) container.getChildAt(0);
        if (surfaceView != null) {
            Object tag = surfaceView.getTag();
            if (tag != null && (Integer) tag == uid) {
                surfaceView.setVisibility(muted ? View.GONE : View.VISIBLE);
            }
        }
    }

    @Subscribe(
            thread = EventThread.IO,
            tags = {
                    @Tag(RxConstants.RxEventTag.RESULT_INVITE_USER)
            }
    )

    //@DebugLog
    public void inviteUserResult(final String ss) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AgoraRunTime.Status status = AgoraRunTime.getInstance().getStatus();
                if (status == AgoraRunTime.Status.answer) {
                    showLongToast("对方已经接通");
                    setViewToAnsweringState();
                    // 提交后台
//                    BackSubmitDataService.requestDeviceInfo(API.videoStart, "", null);
                    return;
                }
                if (status == AgoraRunTime.Status.callFailed) {
                    showLongToast("呼叫失败!" + ss);
                } else if (status == AgoraRunTime.Status.refuse) {
                    showLongToast("对方拒绝");
                } else if (status == AgoraRunTime.Status.hanguped) {
                    // 通话中主动挂断
                    showLongToast("主动呼叫挂断");
                } else if (status == AgoraRunTime.Status.beHangup) {
                    // 通话中被挂断
                    showLongToast("电话已经被挂断");
                }
                localContainer.setVisibility(View.GONE);

                if (mRtcEngine != null) {
                    mRtcEngine.stopPreview();
                }
                if (mRtcEngine != null) {
                    mRtcEngine.leaveChannel();
                }
            }
        });
    }

    @Subscribe(
            thread = EventThread.IO,
            tags = {
                    @Tag(RxConstants.RxEventTag.RESULT_LOGIN)
            }
    )
    //@DebugLog
    public void loginAgoraResult(String ss) {
        if (AgoraRunTime.getInstance().getStatus() == AgoraRunTime.Status.logined) {
            //showLongToast("登录成功, localId:" + localId);
            if (bean.isJoinChannelAfterLogined()) {
                bean.setJoinChannelAfterLogined(false);
                RxBus.get().post(RxConstants.RxEventTag.TAG_AGORA_SERVICE,
                        new AgoraEventDispatch(RxConstants.EVENT_CHECKLOGIN_AND_JOIN_CHANNEL, bean
                        ));
            }
        } else {
            if (isInvitedFromRemote) {
                showLongToast("登录失败！");
                finish();
            }
        }
    }

    @Subscribe(
            thread = EventThread.IO,
            tags = {
                    @Tag(RxConstants.RxEventTag.RESULT_END_INVITE)
            }
    )
    public void endInvite(String ss) {
//        showLongToast("结束呼叫" + (TextUtils.isEmpty(ss) ? "" : "[" + ss + "]"));
        finish();
    }

    @Subscribe(
            thread = EventThread.IO,
            tags = {
                    @Tag(RxConstants.RxEventTag.EVENT_MSG_SHOW)
            }
    )
    //@DebugLog
    public void signalMessage(String ss) {
        showLongToast(AgoraRunTime.getInstance().getMsg());
    }


    @Subscribe(
            thread = EventThread.IO,
            tags = {
                    @Tag(RxConstants.RxEventTag.RESULT_JOIN_CHANNEL)
            }
    )

    //@DebugLog
    public void joinChannelResult(String ss) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                bean.setVideoCall(isVideo);

                AgoraRunTime.Status status = AgoraRunTime.getInstance().getStatus();
                if (status == AgoraRunTime.Status.joinFailed) {
                    showLongToast("连接失败，稍后再试");
                    finish();
                    return;
                }

                /**
                 * 信道加入成功，进行初始化和拨号
                 */
                String key = null;
                try {
                    key = DynamicKeyUtil.generateFinalDynamicKey(channelId, remoteId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                bean.setRtcEngineJoinChannel(mRtcEngine.joinChannel(key, channelId, "", remoteId) == 0);
                if (isVideo) {
                    localContainer.setVisibility(View.VISIBLE);
                } else {
                    mRtcEngine.disableVideo();
                    localContainer.setVisibility(View.GONE);
                    mRtcEngine.setEnableSpeakerphone(true);
                }

                if (!bean.isBeInvitedTojoin()) {
                    // 主动发起邀请
                    bean.setRemoteId(remoteId + "");
                    RxBus.get().post(RxConstants.RxEventTag.TAG_AGORA_SERVICE, new AgoraEventDispatch(RxConstants.EVENT_INVITE_USER, bean));
                } else {
                    //showLongToast("收到" + bean.getRemoteName() + "来电, " + bean.getChannleID() + "，接入中");
                }

                mRtcEngine.enableAudioVolumeIndication(1000, 3);
                mRtcEngine.setParameters("{\"rtc.log_filter\":32783}");//0x800f, log to console
                mRtcEngine.setLogFilter(32783);
            }
        });
    }

    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() { // Tutorial Step 1
        @Override
        public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) { // Tutorial Step 5
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setupRemoteVideo(uid);
                }
            });
        }

        @Override
        public void onRejoinChannelSuccess(String channel, int uid, int elapsed) {
            super.onRejoinChannelSuccess(channel, uid, elapsed);
        }

        @Override
        public void onUserOffline(int uid, int reason) { // Tutorial Step 7
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onRemoteUserLeft();
                }
            });
        }

        @Override
        public void onUserMuteVideo(final int uid, final boolean muted) { // Tutorial Step 10
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onRemoteUserVideoMuted(uid, muted);
                }
            });
        }

        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            super.onJoinChannelSuccess(channel, uid, elapsed);
        }

        @Override
        public void onWarning(int warn) {
            super.onWarning(warn);
        }

        @Override
        public void onError(int err) {
            super.onError(err);
            if (err == ERR_LEAVE_CHANNEL_REJECTED) {
                // 离开频道失败。一般是因为用户已离开某频道，再次调用退出频道的API，例如leaveChannel，会返回此错误。
            }
            //showLongToast("errorCode[" + err + "]");
        }

        @Override
        public void onUserJoined(int uid, int elapsed) {
            super.onUserJoined(uid, elapsed);
        }

        @Override
        public void onConnectionLost() {
            super.onConnectionLost();
            showLongToast("连接断开！");
        }

        @Override
        public void onLeaveChannel(RtcStats stats) {
            super.onLeaveChannel(stats);
            avTimeChronometer.stop();
            mRtcEngine.stopPreview();
//            showLongToast("退出视频！" + channelId);
            finish();
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mRtcEngine.muteAllRemoteAudioStreams(true);
        RxBus.get().post(RxConstants.RxEventTag.TAG_AGORA_SERVICE, new AgoraEventDispatch(isInvitedFromRemote ? RxConstants.EVENT_CHANNEL_INVITE_REFUSE : RxConstants.EVENT_CHANNEL_INVITE_END, bean));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRtcEngine.destroy();
        RxConstants.isCalling = false;
        RxBus.get().unregister(this);
    }


    public boolean checkSelfPermission(String permission, int requestCode) {
        Log.i(LOG_TAG, "checkSelfPermission " + permission + " " + requestCode);
        if (ContextCompat.checkSelfPermission(this,
                permission)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{permission},
                    requestCode);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        Log.i(LOG_TAG, "onRequestPermissionsResult " + grantResults[0] + " " + requestCode);

        switch (requestCode) {
            case PERMISSION_REQ_ID_RECORD_AUDIO: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA);
                } else {
                    showLongToast("您需要授权语音权限才可以通话!");
                    finish();
                }
                break;
            }
            case PERMISSION_REQ_ID_CAMERA: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initSignal();
                } else {
                    showLongToast("您需要授权摄像头权限才可以通话!");
                    finish();
                }
                break;
            }
        }
    }

    private void setViewToAnsweringState() {
        avTimeChronometer.setVisibility(View.VISIBLE);
        avTimeChronometer.setBase(SystemClock.elapsedRealtime());
        avTimeChronometer.start();
        mRtcEngine.enableAudio();
        mRtcEngine.muteLocalAudioStream(false);
        headerProgressLoading.stop();
    }


    public final void showLongToast(final String msg) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!TextUtils.isEmpty(msg)) {
                    Toast.makeText(VideoChatViewActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
