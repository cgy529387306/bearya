package com.bearya.robot.household.services;

import android.util.Log;
import android.view.SurfaceView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bearya.robot.household.MyApplication;
import com.bearya.robot.household.utils.LogUtils;

import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;

import static io.agora.rtc.Constants.CHANNEL_PROFILE_LIVE_BROADCASTING;

/**
 * Created by Qiujz on 2017/12/5.
 */

public class MonitorService {
    private RtcEngine mRtcEngine;// Tutorial Step 1
    private RelativeLayout sfvContainer;
    private ImageView closeMonitor;

    public MonitorService() {

    }

    public MonitorService(RelativeLayout sfvContainer, ImageView closeMonitor) {
        this.sfvContainer = sfvContainer;
        this.closeMonitor = closeMonitor;
    }

    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() { // Tutorial Step 1
        @Override
        public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) { // Tutorial Step 5
            LogUtils.d("MonitorService", " onFirstRemoteVideoDecoded ..........."+uid);
            //closeMonitor.setVisibility(View.VISIBLE);
            //setupLocalVideo(uid);
        }

        @Override
        public void onUserOffline(int uid, int reason) { // Tutorial Step 7
        }

        @Override
        public void onUserMuteVideo(final int uid, final boolean muted) { // Tutorial Step 10
        }
    };

    public void initAgoraEngineAndJoinChannel(String appId, String channel, String key, int uid) {
        LogUtils.d("MonitorService", " initAgoraEngineAndJoinChannel ..........."+" key = "+key);
        initializeAgoraEngine(appId);     // Tutorial Step 1
        setupVideoProfile();              // Tutorial Step 2
        setupLocalVideo(uid);             // Tutorial Step 3
        joinChannel(channel, key, 0);   // Tutorial Step 4
    }

    // Tutorial Step 1
    private void initializeAgoraEngine(String appId) {
        try {
            mRtcEngine = RtcEngine.create(MyApplication.getContext(), appId, mRtcEventHandler);
        } catch (Exception e) {
            LogUtils.e("MONITOR" + Log.getStackTraceString(e));
            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
    }

    // Tutorial Step 2
    private void setupVideoProfile() {
        mRtcEngine.enableVideo();
        mRtcEngine.setClientRole(Constants.CLIENT_ROLE_AUDIENCE, "");
        mRtcEngine.setChannelProfile(CHANNEL_PROFILE_LIVE_BROADCASTING);
        int i = mRtcEngine.setVideoProfile(Constants.VIDEO_PROFILE_360P, true);
        System.out.println("setupVideoProfile" + i);
    }

    // Tutorial Step 3
    public void setupLocalVideo(int uid) {
        SurfaceView surfaceView = RtcEngine.CreateRendererView(MyApplication.getContext());
        surfaceView.setZOrderOnTop(true);
        sfvContainer.addView(surfaceView);

        int i = mRtcEngine.setupRemoteVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, uid));
        mRtcEngine.startPreview();
        System.out.println("setupLocalVideo" + i);
    }

    // Tutorial Step 4
    private void joinChannel(String channel, String key, int uid) {
        try {
            int i = mRtcEngine.joinChannel(key, channel, "OpenLive", uid);// if you do not specify the uid, we will generate the uid for you
            LogUtils.d("MonitorService", " joinChannel ..........."+i);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Tutorial Step 6
    public void leaveChannel() {
        LogUtils.d("MonitorService", " leaveChannel ...........");
        if (mRtcEngine != null) {
            try {
                mRtcEngine.leaveChannel();
                mRtcEngine.destroy();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
            mRtcEngine = null;
        }
    }


}
