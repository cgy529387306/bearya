package com.bearya.robot.household.videoCall;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.Bundle;
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
import android.widget.LinearLayout;
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
import com.skyfishjy.library.RippleBackground;
import com.victor.loading.rotate.RotateLoading;

import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;

import static io.agora.rtc.Constants.ERR_LEAVE_CHANNEL_REJECTED;

public class VideoChatViewActivity extends Activity {
    private TextView avTitleTextView, callAudioTextView;
    private Chronometer avTimeChronometer;
    private TextView chatMemberTextView;
    private FrameLayout localContainer;
    private ImageView switchCameraIv;
    private ImageView switchMicIv;
    private ImageView hangupIv;
    private ImageView answerIv;
    private ImageView muteIv;
    private ImageView remoteUserIv;
    private Chronometer avCenterTimeChronometer;
    private View callInLayout, callInCenterLayout, avTitleLinearLayout;
    private RotateLoading headerProgressLoading;
    private RippleBackground rippleBackground;
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

        avTitleLinearLayout = (LinearLayout) findViewById(R.id.av_title_layout);
        avTitleTextView = (TextView) findViewById(R.id.av_title_textview);
        avTimeChronometer = (Chronometer) findViewById(R.id.av_time_textview);
        // 语音呼叫的时候使用
        avCenterTimeChronometer = (Chronometer) findViewById(R.id.audio_time_textview);
        localContainer = (FrameLayout) findViewById(R.id.local_video_view_container);
        callInLayout = (View) findViewById(R.id.callin_layout);
        callInCenterLayout = (View) findViewById(R.id.callin__center_layout);
        switchCameraIv = (ImageView) findViewById(R.id.video_controll_switch_camera_iv);
        switchMicIv = (ImageView) findViewById(R.id.video_controll_switch_mic_iv);
        hangupIv = (ImageView) findViewById(R.id.video_controll_hangup_iv);
        answerIv = (ImageView) findViewById(R.id.video_controll_answer_iv);
        muteIv = (ImageView) findViewById(R.id.video_controll_mute_iv);
        chatMemberTextView = (TextView) findViewById(R.id.audio_chat_textview);
        callAudioTextView = (TextView) findViewById(R.id.callin_textview);
        remoteUserIv = (ImageView) findViewById(R.id.audio_header_imageview);
        headerProgressLoading = (RotateLoading) findViewById(R.id.audio_header_progress);
        rippleBackground = (RippleBackground) findViewById(R.id.ripple_background);

        remoteId = getIntent().getExtras().getInt("remoteId");
        localId = getIntent().getExtras().getInt("localId");
        localPic = getIntent().getExtras().getString("localPic");
        remotePic = getIntent().getExtras().getString("remotePic");
        remoteName = getIntent().getExtras().getString("remoteName");
        isVideo = getIntent().getExtras().getBoolean("isVideo", true);
        Glide.with(this).load(remotePic).error(R.mipmap.header_dad).into(new SimpleTarget<GlideDrawable>() {
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
            avTitleTextView.setText("呼叫" + remoteName + "中, 等待回应...");
            chatMemberTextView.setText(remoteName);
            callInLayout.setVisibility(View.GONE);
            avCenterTimeChronometer.setVisibility(View.GONE);
        } else {
            channelId = localId + "";
            // 外部呼叫
            callInLayout.setVisibility(View.VISIBLE);
            avTitleLinearLayout.setVisibility(View.GONE);
            localContainer.setVisibility(View.GONE);
            chatMemberTextView.setText(remoteName);
        }

        bean = new AgoraTransferBean(channelId, remoteId + "", localId, "", false, "", 0, localPic);
        bean.setVideoCall(isVideo);


        if (isVideo) {
            localContainer.setVisibility(View.VISIBLE);
        } else {
            localContainer.setVisibility(View.GONE);
        }

        if (/*TextUtils.isEmpty(remoteName) ||*/ TextUtils.isEmpty(channelId)) {
            Toast.makeText(this, "视频通话无法接通！", Toast.LENGTH_SHORT).show();
            finish();
        }
        //switchCameraIv.setVisibility(isInvitedFromRemote ? View.GONE : View.VISIBLE);
        answerIv.setVisibility(isInvitedFromRemote ? View.VISIBLE : View.GONE);
        switchMicIv.setVisibility(View.GONE);
        muteIv.setVisibility(View.GONE);

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

        //mRtcEngine.enableAudio();
        mRtcEngine.enableVideo();
        //mRtcEngine.setAudioProfile(3/*AUDIO_PROFILE_MUSIC_STANDARD_STEREO*/, 2/*AUDIO_SCENARIO_EDUCATION*/);
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
            rippleBackground.setVisibility(View.GONE);
            headerProgressLoading.start();
        } else {
            // 外部呼入
            bean.setBeInvitedTojoin(true);
            localContainer.setVisibility(View.GONE);
            rippleBackground.startRippleAnimation();
            headerProgressLoading.setVisibility(View.GONE);
//                mRtcEngine.disableVideo();
        }
    }

    public void onLocalVideoMuteClicked(boolean muteLocalVideo) {
        mRtcEngine.muteLocalVideoStream(muteLocalVideo);

        SurfaceView surfaceView = (SurfaceView) localContainer.getChildAt(0);
        surfaceView.setZOrderMediaOverlay(!muteLocalVideo);
        surfaceView.setVisibility(muteLocalVideo ? View.GONE : View.VISIBLE);
    }

    public void onLocalAudioMuteClicked(View view) {
        ImageView iv = (ImageView) view;
        if (iv.isSelected()) {
            iv.setSelected(false);
            iv.clearColorFilter();
        } else {
            iv.setSelected(true);
            iv.setColorFilter(getResources().getColor(R.color.colorItBlue), PorterDuff.Mode.MULTIPLY);
        }

        mRtcEngine.muteAllRemoteAudioStreams(iv.isSelected());
    }

    public void onLocalMicMuteClicked(View view) {
        ImageView iv = (ImageView) view;
        if (iv.isSelected()) {
            iv.setSelected(false);
            iv.clearColorFilter();
        } else {
            iv.setSelected(true);
            iv.setColorFilter(getResources().getColor(R.color.colorItBlue), PorterDuff.Mode.MULTIPLY);
        }

        mRtcEngine.muteLocalAudioStream(iv.isSelected());
    }

    public void onSwitchCameraClicked(View view) {
        if (bean.isRtcEngineJoinChannel()) {
            mRtcEngine.switchCamera();
        }
    }

    /**
     * 接听电话  channleID, account, uid
     */
    public void onAnswerVideoClicked(View view) {
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

        callInCenterLayout.setVisibility(View.GONE);
        avTitleTextView.setText("和" + remoteName + "视频通话中");
        answerIv.setVisibility(View.GONE);
        hangupIv.setVisibility(View.VISIBLE);
        //switchCameraIv.setVisibility(View.VISIBLE);
        switchMicIv.setVisibility( View.VISIBLE);
        muteIv.setVisibility(View.VISIBLE);

        headerProgressLoading.stop();
        avTitleLinearLayout.setVisibility(View.VISIBLE);
        avCenterTimeChronometer.start();
        avTimeChronometer.start();
    }

    private void onRemoteUserLeft() {
        FrameLayout container = (FrameLayout) findViewById(R.id.remote_video_view_container);
        container.removeAllViews();
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
                    avCenterTimeChronometer.setVisibility(View.GONE);
                } else {
                    mRtcEngine.disableVideo();
                    localContainer.setVisibility(View.GONE);
                    avCenterTimeChronometer.setVisibility(View.VISIBLE);
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

            avCenterTimeChronometer.stop();
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
        rippleBackground.stopRippleAnimation();
        rippleBackground.setVisibility(View.GONE);

        headerProgressLoading.setVisibility(View.GONE);
        //switchCameraIv.setVisibility(View.GONE);
        switchMicIv.setVisibility( View.GONE);
        callAudioTextView.setText("正在通话");
        answerIv.setVisibility(View.GONE);

        mRtcEngine.muteLocalAudioStream(false);
        avCenterTimeChronometer.start();
        avTimeChronometer.start();
        mRtcEngine.enableAudio();
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
