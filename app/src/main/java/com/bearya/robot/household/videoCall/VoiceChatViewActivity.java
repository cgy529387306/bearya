package com.bearya.robot.household.videoCall;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
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

import static io.agora.rtc.Constants.ERR_LEAVE_CHANNEL_REJECTED;

public class VoiceChatViewActivity extends AppCompatActivity {

    private static final String LOG_TAG = VoiceChatViewActivity.class.getSimpleName();

    private static final int PERMISSION_REQ_ID_RECORD_AUDIO = 22;
    private ImageView remoteUserIv;
    private Chronometer avTimeChronometer;
    private TextView avTitleTextView;
    private RtcEngine mRtcEngine;// Tutorial Step 1
    private int localId;// 本地的用户id
    private int remoteId;
    private String remoteName;
    private String remotePic;
    private String localPic;
    private String channelId;
    private boolean isInvitedFromRemote;
    private AgoraTransferBean bean;
    private boolean isVideo;
    private View callInLayout, callInviteLayout;
    private RotateLoading headerProgressLoading;
    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() { // Tutorial Step 1

        @Override
        public void onRejoinChannelSuccess(String channel, int uid, int elapsed) {
            super.onRejoinChannelSuccess(channel, uid, elapsed);
        }

        @Override
        public void onUserOffline(final int uid, final int reason) { // Tutorial Step 7
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onRemoteUserLeft(uid,reason);
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
            finish();
        }


        @Override
        public void onUserMuteAudio(final int uid, final boolean muted) { // Tutorial Step 6
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onRemoteUserVoiceMuted(uid, muted);
                }
            });
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_chat_view);
        remoteUserIv = (ImageView) findViewById(R.id.iv_avatar);
        avTitleTextView = (TextView) findViewById(R.id.av_title_textview);
        avTimeChronometer = (Chronometer) findViewById(R.id.av_time_textview);
        callInLayout = findViewById(R.id.callin_layout);
        callInviteLayout = findViewById(R.id.call_invite_layout);
        headerProgressLoading = (RotateLoading) findViewById(R.id.audio_header_progress);

        remoteId = getIntent().getExtras().getInt("remoteId");
        localId = getIntent().getExtras().getInt("localId");
        localPic = getIntent().getExtras().getString("localPic");
        remotePic = getIntent().getExtras().getString("remotePic");
        remoteName = getIntent().getExtras().getString("remoteName");
        isVideo = getIntent().getExtras().getBoolean("isVideo", false);
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
            headerProgressLoading.start();
        } else {
            channelId = localId + "";
            avTitleTextView.setText(remoteName+"电话呼入");
            headerProgressLoading.setVisibility(View.GONE);
        }

        if (TextUtils.isEmpty(channelId)) {
            Toast.makeText(this, "语音通话无法接通！", Toast.LENGTH_SHORT).show();
            finish();
        }

        bean = new AgoraTransferBean(channelId, remoteId + "", localId, "", false, "", 0, localPic);
        bean.setVideoCall(isVideo);
        setRemote(isInvitedFromRemote);
        RxBus.get().register(this);
        if (!CommonUtils.isServiceRunning(this, AgoraService.class)) {
            bean.setJoinChannelAfterLogined(true);
            startService(new Intent(this, AgoraService.class).putExtra("myUid", "" + localId));
        }
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID_RECORD_AUDIO)) {
            initSignal();
        }
        RxConstants.isCalling = true;
    }

    private void setRemote(boolean isRemote){
        callInLayout.setVisibility(isRemote?View.VISIBLE:View.GONE);
        callInviteLayout.setVisibility(isRemote?View.GONE:View.VISIBLE);
    }

    // Tutorial Step 1
    private void initSignal() {
        initRtcEngine();
        if (!isInvitedFromRemote) {
            // 主动拨打
            RxBus.get().post(RxConstants.RxEventTag.TAG_AGORA_SERVICE,
                    new AgoraEventDispatch(RxConstants.EVENT_CHECKLOGIN_AND_JOIN_CHANNEL, bean
                    ));
        } else {
            // 外部呼入
            bean.setBeInvitedTojoin(true);
        }
    }

    private void initRtcEngine(){
        if (mRtcEngine == null) {
            try {
                mRtcEngine = RtcEngine.create(this, AgoraCalculateHelp.appID, mRtcEventHandler);
            } catch (Exception e) {
                e.printStackTrace();
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
                if (mRtcEngine != null) {
                    mRtcEngine.leaveChannel();
                }
            }
        });
    }

    private void setViewToAnsweringState() {
        avTimeChronometer.setBase(SystemClock.elapsedRealtime());
        avTimeChronometer.start();
        mRtcEngine.enableAudio();
        mRtcEngine.muteLocalAudioStream(false);
        headerProgressLoading.stop();
        avTitleTextView.setText("和" + remoteName + "语音通话中");
    }

    @Subscribe(
            thread = EventThread.IO,
            tags = {
                    @Tag(RxConstants.RxEventTag.RESULT_LOGIN)
            }
    )
    public void loginAgoraResult(String ss) {
        if (AgoraRunTime.getInstance().getStatus() == AgoraRunTime.Status.logined) {
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
//        showLongToast(AgoraRunTime.getInstance().getMsg());
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
                initRtcEngine();
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
                if (!isVideo) {
                    mRtcEngine.disableVideo();
                    mRtcEngine.setEnableSpeakerphone(true);
                }
                if (!bean.isBeInvitedTojoin()) {
                    // 主动发起邀请
                    bean.setRemoteId(remoteId + "");
                    RxBus.get().post(RxConstants.RxEventTag.TAG_AGORA_SERVICE, new AgoraEventDispatch(RxConstants.EVENT_INVITE_USER, bean));
                }else{
                    avTimeChronometer.setBase(SystemClock.elapsedRealtime());
                    avTimeChronometer.start();
                }
                mRtcEngine.enableAudioVolumeIndication(1000, 3);
                mRtcEngine.setParameters("{\"rtc.log_filter\":32783}");//0x800f, log to console
                mRtcEngine.setLogFilter(32783);
            }
        });
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
                    initSignal();
                } else {
                    showLongToast("您需要授权语音权限才可以通话!");
                    finish();
                }
                break;
            }
        }
    }

    public final void showLongToast(final String msg) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }

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

    // Tutorial Step 7
    public void onLocalAudioMuteClicked(View view) {
        ImageView iv = (ImageView) view;
        if (iv.isSelected()) {
            iv.setSelected(false);
            iv.clearColorFilter();
        } else {
            iv.setSelected(true);
            iv.setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
        }

        mRtcEngine.muteLocalAudioStream(iv.isSelected());
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

    // Tutorial Step 5
    public void onSwitchSpeakerphoneClicked(View view) {
        ImageView iv = (ImageView) view;
        if (iv.isSelected()) {
            iv.setSelected(false);
            iv.clearColorFilter();
        } else {
            iv.setSelected(true);
            iv.setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
        }

        mRtcEngine.setEnableSpeakerphone(view.isSelected());
    }

    // Tutorial Step 3
    public void onEncCallClicked(View view) {
        finish();
    }



    // Tutorial Step 3
    private void leaveChannel() {
        mRtcEngine.leaveChannel();
    }

    // Tutorial Step 4
    private void onRemoteUserLeft(int uid, int reason) {
        showLongToast("对方已挂断");
        finish();
    }

    // Tutorial Step 6
    private void onRemoteUserVoiceMuted(int uid, boolean muted) {
        showLongToast("对方已静音");
    }

    /**
     * 接听电话  channleID, account, uid
     */
    public void onAnswerVideoClicked(View view) {
        setRemote(false);
        avTitleTextView.setText("语音通话中");
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
}
