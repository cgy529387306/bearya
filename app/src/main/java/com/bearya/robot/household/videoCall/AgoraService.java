package com.bearya.robot.household.videoCall;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.bearya.robot.household.MyApplication;
import com.bearya.robot.household.entity.UserInfo;
import com.bearya.robot.household.threadpool.ThreadPoolManager;
import com.bearya.robot.household.utils.CommonUtils;
import com.bearya.robot.household.utils.LogUtils;
import com.bearya.robot.household.utils.UserInfoManager;
import com.bearya.robot.household.videoCall.beans.AgoraEventDispatch;
import com.bearya.robot.household.videoCall.beans.AgoraRunTime;
import com.bearya.robot.household.videoCall.beans.ITransfer;
import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;

import io.agora.AgoraAPI;
import io.agora.AgoraAPIOnlySignal;
import io.agora.IAgoraAPI;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.bearya.robot.household.videoCall.RxConstants.RxEventTag.TAG_AGORA_SERVICE;

/**
 * 信令呼叫系统
 * Created by leo on 17/6/29.
 */

public class AgoraService extends Service {
    private final static int MSG_INIT_VIDEO_CALL = 998;
    private final String TAG = "AgoraService_videoCall";
    private AgoraAPIOnlySignal agoraSignal;
    private MyBroadcastReceiver receiver;
    private String mLocalName="";
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_INIT_VIDEO_CALL) {
                initVideoCall();
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //final String localName = intent.getExtras().getString("myUid");
        LogUtils.d(TAG, "onStartCommand");
        registerReceiver();
        initVideoCall();
        return START_REDELIVER_INTENT;
    }

    public void init() {
        if (agoraSignal == null) {
            try {
                agoraSignal = AgoraAPIOnlySignal.getInstance(this, AgoraCalculateHelp.appID);
                setCallBackSet();
            } catch (UnsatisfiedLinkError e) {
                agoraSignal = null;
                onDestroy();//自已关闭服务
            }
        }
    }

    public void login(String localName) {
        init();
        LogUtils.d(TAG, "login="+localName);
        // 登录
        AgoraRunTime.getInstance().setLocalName(localName);
        if (agoraSignal.isOnline() != 1) {
            AgoraRunTime.Status ss = AgoraRunTime.getInstance().getStatus();
            if (ss == AgoraRunTime.Status.init || ss == AgoraRunTime.Status.logout || ss == AgoraRunTime.Status.loginFailed) {
                AgoraRunTime.getInstance().setStatus(AgoraRunTime.Status.logining);
                agoraSignal.login2(AgoraCalculateHelp.appID,
                        localName,
                        AgoraCalculateHelp.calcToken(localName), 0, "", 60, 5);
                mLocalName = localName;
                LogUtils.d(TAG,"用户为登录，发起登录,localName:" + localName);
            } else {
                LogUtils.d(TAG, "login failed");
            }
        } else {
            if (mLocalName.equals(localName)) {
                AgoraRunTime.getInstance().setStatus(AgoraRunTime.Status.logined);
                //AgoraRunTime.getInstance().setMsg("已经登陆过");
                RxBus.get().post(RxConstants.RxEventTag.RESULT_LOGIN, "");
                LogUtils.d(TAG, "用户已登录,返回");
            } else {
                agoraSignal.logout();
                login(localName);
            }
        }
    }

    private void initVideoCall() {
        LogUtils.d(TAG, "initVideoCall");
        UserInfo userInfo = UserInfoManager.getInstance().getUserInfo();
        if (userInfo != null) {
            login(""+userInfo.getUid());
        }
    }

    private void registerReceiver() {
        if (receiver == null) {
            LogUtils.d(TAG, "registerReceiver");
            receiver = new MyBroadcastReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            registerReceiver(receiver, filter);
        }
    }

    public void sendMessageToPeer(String account, int uid, String msg, String msgID) {
        agoraSignal.messageInstantSend(account, uid, msg, msgID);
    }

    public void sendMessageByChannel(String channelID, String msg, String msgID) {
        agoraSignal.messageChannelSend(channelID, msg, msgID);
    }


    // channel && myUid
    public synchronized void joinChannel(ITransfer bean) {
        if (agoraSignal.isOnline() == 1) {
            AgoraRunTime.Status status = AgoraRunTime.getInstance().getStatus();
            if (status == AgoraRunTime.Status.logined || status == AgoraRunTime.Status.leaved ) {
                LogUtils.d(TAG,"local用户在线, 加入信道" + bean.getChannelName());
                agoraSignal.channelJoin(bean.getChannelName());
                AgoraRunTime.getInstance().setStatus(AgoraRunTime.Status.joining);
            } else {
                LogUtils.d(TAG,"自动登录中，请稍后...");
            }
        } else {
            AgoraRunTime.getInstance().reset();
            LogUtils.d(TAG,"要加入信道，但是发现没有登录," + bean.getChannelName());
            agoraSignal.login2(AgoraCalculateHelp.appID, bean.getLocalId(), AgoraCalculateHelp.calcToken(bean.getRemoteId()), 0, "", 60, 5);
        }
    }

    // 信令
    public void setCallBackSet() {
        if (agoraSignal == null) {
            return;
        }
        agoraSignal.callbackSet(new AgoraAPI.CallBack() {
            /**
             *  电话呼入,必须先登录才会收到此消息，然后加入到channel
             *  channelId 就是被接受方自己的id, remoteAccount 是对方app的帐号
             */
            @Override
            public void onInviteReceived(String channelId, String remoteAccount, int doNotUseUid, String extra) {

                AgoraRunTime.getInstance().setStatus(AgoraRunTime.Status.beCalled);
                if (RxConstants.isCalling) {
                    Toast.makeText(AgoraService.this, "您有新来电!", Toast.LENGTH_SHORT).show();
                    return;
                }

                LogUtils.d(TAG,"有新来电！");
                try {
                    JSONObject obj = JSONObject.parseObject(extra);
                    boolean isVideo = obj.getBoolean("isVideoCall");
                    String remoteName = obj.getString("remoteName");
                    LogUtils.d(TAG,"新来电："+remoteName);
                    Intent intent = null;
                    if (isVideo){
                        intent = new Intent(AgoraService.this, VideoChatViewActivity.class);
                    }else {
                        intent = new Intent(AgoraService.this, VoiceChatViewActivity.class);
                    }
                    intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("beInvited", true);
                    intent.putExtra("isVideo", isVideo);
                    intent.putExtra("remoteName", remoteName);
                    intent.putExtra("remotePic", obj.getString("localPic"));
                    intent.putExtra("localId", Integer.parseInt(channelId));
                    intent.putExtra("remoteId", Integer.parseInt(remoteAccount));// 强转为fromAccount
                    intent.putExtra("extra", extra);
                    startActivity(intent);
                } catch (Exception e) {
                    LogUtils.d(TAG,"新来电：error!!!");
                }
            }

            @Override
            public void onLoginSuccess(int uid, int fd) {
                AgoraRunTime.getInstance().setStatus(AgoraRunTime.Status.logined);
               // AgoraRunTime.getInstance().setMsg("登录成功!");
                RxBus.get().post(RxConstants.RxEventTag.RESULT_LOGIN, "");
                LogUtils.d(TAG,"local用户登录成功, " + AgoraRunTime.getInstance().getLocalName());
            }

            @Override
            public void onLoginFailed(int ecode) {
                //LogUtils.d(TAG,"onLoginFailed.... "+ecode);
                AgoraRunTime.getInstance().setStatus(AgoraRunTime.Status.loginFailed);
               // AgoraRunTime.getInstance().setMsg("登录失败!");
                RxBus.get().post(RxConstants.RxEventTag.RESULT_LOGIN, "");
                LogUtils.d(TAG,"local用户登录失败, code:" + ecode + ", localName:" + AgoraRunTime.getInstance().getLocalName());
            }

            @Override
            public void onLogout(int ecode) {
                AgoraRunTime.getInstance().setStatus(AgoraRunTime.Status.logout);
                RxBus.get().post(RxConstants.RxEventTag.EVENT_MSG_SHOW, "");

                LogUtils.d(TAG,"local用户退出成功, " + AgoraRunTime.getInstance().getLocalName());
            }

            @Override
            public void onLog(String txt) {
//                Logger.d("onLog " + txt);
            }

            @Override
            public void onChannelJoined(String chanID) {
                AgoraRunTime.getInstance().setStatus(AgoraRunTime.Status.joineded);
                RxBus.get().post(RxConstants.RxEventTag.RESULT_JOIN_CHANNEL, "");//

                LogUtils.d(TAG, "onChannelJoined, channelId:" + chanID + ", localName:" + AgoraRunTime.getInstance().getLocalName());
            }

            @Override
            public void onChannelUserJoined(String account, int uid) {
                AgoraRunTime.getInstance().setJoinedName(account);
                RxBus.get().post(RxConstants.RxEventTag.EVENT_MSG_SHOW, "");
            }

            @Override
            public void onChannelJoinFailed(String chanID, int ecode) {
                AgoraRunTime.getInstance().setStatus(AgoraRunTime.Status.joinFailed);
                AgoraRunTime.getInstance().setChannel("");
                RxBus.get().post(RxConstants.RxEventTag.RESULT_JOIN_CHANNEL, "");//
            }

            @Override
            public void onChannelUserLeaved(String account, int uid) {
                AgoraRunTime.getInstance().setLeaveName(account);
                RxBus.get().post(RxConstants.RxEventTag.EVENT_MSG_SHOW, "");
            }

            @Override
            public void onChannelUserList(String[] accounts, int[] uids) {
                for (int i = 0; i < accounts.length; i++) {
                    long uid = uids[i] & 0xffffffffl;
//                    System.out.println(accounts[i] + ":" + (long) (uid & 0xffffffffl));
//                    Logger.w("onChannelUserList, uid:" + uid);
                }
            }


            @Override
            public void onInviteRefusedByPeer(String channelID, String account, int uid, String extra) {
                if (AgoraRunTime.getInstance().getStatus() != AgoraRunTime.Status.refuse) {
                    AgoraRunTime.getInstance().setStatus(AgoraRunTime.Status.refuse);
                    RxBus.get().post(RxConstants.RxEventTag.RESULT_INVITE_USER, "");
                    endInvite();
                }
            }

            @Override
            public void onInviteReceivedByPeer(String channleID, String account, int uid) {
                //AgoraRunTime.getInstance().setMsg(account + " 已经收到我的呼叫.");
                RxBus.get().post(RxConstants.RxEventTag.EVENT_MSG_SHOW, "");
            }

            @Override
            public void onInviteFailed(String channelID, String account, int uid, int ecode, String extra) {
                AgoraRunTime.getInstance().setStatus(AgoraRunTime.Status.callFailed);
                RxBus.get().post(RxConstants.RxEventTag.RESULT_INVITE_USER, extra);
                endInvite();
            }

            @Override
            public void onInviteMsg(String channelID, String account, int uid, String msgType, String msgData, String extra) {
                                    //AgoraRunTime.getInstance().setMsg(msgData);
                RxBus.get().post(RxConstants.RxEventTag.EVENT_MSG_SHOW, "");
            }

            @Override
            public void onInviteEndByPeer(String channelID, String account, int uid, String extra) {
                if (AgoraRunTime.Status.beHangup != AgoraRunTime.getInstance().getStatus()) {
                    AgoraRunTime.getInstance().setStatus(AgoraRunTime.Status.beHangup);
                    RxBus.get().post(RxConstants.RxEventTag.RESULT_INVITE_USER, "");
                    endInvite();
                }
            }

            @Override
            public void onInviteEndByMyself(String channelID, String account, int uid) {
                if (AgoraRunTime.Status.hanguped != AgoraRunTime.getInstance().getStatus()) {
                    AgoraRunTime.getInstance().setStatus(AgoraRunTime.Status.hanguped);
                    RxBus.get().post(RxConstants.RxEventTag.RESULT_INVITE_USER, "");
                    endInvite();
                }
            }

            @Override
            public void onInviteAcceptedByPeer(String channelID, String account, int uid, String extra) {
                AgoraRunTime.getInstance().setStatus(AgoraRunTime.Status.answer);
                RxBus.get().post(RxConstants.RxEventTag.RESULT_INVITE_USER, "");
            }

            @Override
            public void onMessageChannelReceive(String channelID, String account, int uid, String msg) {
                //AgoraRunTime.getInstance().setMsg(msg);
                RxBus.get().post(RxConstants.RxEventTag.RESULT_CHANNEL_MESSAGE, "");
            }

            @Override
            public void onMessageInstantReceive(String account, int uid, String msg) {
            }

            @Override
            public void setCB(IAgoraAPI.ICallBack cb) {
                super.setCB(cb);
            }

            @Override
            public IAgoraAPI.ICallBack getCB() {
                return super.getCB();
            }

            @Override
            public void onChannelLeaved(String channelID, int ecode) {
                super.onChannelLeaved(channelID, ecode);
                AgoraRunTime.getInstance().setStatus(AgoraRunTime.Status.leaved);
                AgoraRunTime.getInstance().setChannel(null);
                RxBus.get().post(RxConstants.RxEventTag.RESULT_END_INVITE, "");

                LogUtils.d(TAG,"onChannelLeaved, channelID:" + channelID + ", ecode:" + ecode + ",name:" + AgoraRunTime.getInstance().getLocalName());
            }

            @Override
            public void onChannelQueryUserNumResult(String channelID, int ecode, int num) {
                super.onChannelQueryUserNumResult(channelID, ecode, num);
            }

            @Override
            public void onChannelAttrUpdated(String channelID, String name, String value, String type) {
                super.onChannelAttrUpdated(channelID, name, value, type);
            }


            @Override
            public void onMessageSendError(String messageID, int ecode) {
                super.onMessageSendError(messageID, ecode);
            }

            @Override
            public void onMessageSendSuccess(String messageID) {
                super.onMessageSendSuccess(messageID);
            }

            @Override
            public void onMessageAppReceived(String msg) {
                LogUtils.d(TAG,"onMessageAppReceived");
                super.onMessageAppReceived(msg);
            }

            @Override
            public void onInvokeRet(String name, String reason, String resp) {
                super.onInvokeRet(name, reason, resp);
            }

            @Override
            public void onMsg(String from, String t, String msg) {
                super.onMsg(from, t, msg);
            }

            @Override
            public void onUserAttrResult(String account, String name, String value) {
                super.onUserAttrResult(account, name, value);
            }

            @Override
            public void onUserAttrAllResult(String account, String value) {
                super.onUserAttrAllResult(account, value);
            }

            @Override
            public void onError(String name, int ecode, String desc) {
                LogUtils.d(TAG,"onError,name:" + name + ",ecode:" + ecode + ".desc:" + desc);
                super.onError(name, ecode, desc);
            }

            @Override
            public void onQueryUserStatusResult(String name, String status) {
                super.onQueryUserStatusResult(name, status);
            }

            /*@Override
            public void onDbg(String a, String b) {
                super.onDbg(a, b);
            }*/

            @Override
            public void onReconnected(int fd) {
                super.onReconnected(fd);
                //AgoraRunTime.getInstance().setMsg("已经重连");
                RxBus.get().post(RxConstants.RxEventTag.EVENT_MSG_SHOW, "");
            }

            @Override
            public void onReconnecting(int nretry) {
                if (nretry > 2) {
                    RxBus.get().post(RxConstants.RxEventTag.RESULT_END_INVITE, "");
                }
                //AgoraRunTime.getInstance().setMsg("正在进行" + nretry + "次重连");
                RxBus.get().post(RxConstants.RxEventTag.EVENT_MSG_SHOW, "");
            }
        });
    }

    // 异常结束或者呼叫结束
    private void endInvite() {
        if (AgoraRunTime.getInstance().getStatus() != AgoraRunTime.Status.leaving) {
            AgoraRunTime.getInstance().setStatus(AgoraRunTime.Status.leaving);
        }
        agoraSignal.channelLeave(AgoraRunTime.getInstance().getChannel());
    }

    public void inviteUser(ITransfer bean) {
        AgoraRunTime.Status cur = AgoraRunTime.getInstance().getStatus();
        if (cur != AgoraRunTime.Status.calling
                && cur == AgoraRunTime.Status.joineded) {
            LogUtils.d(TAG,"发起用户邀请, 要打给:" + bean.getRemoteId() + ", getChannelName:" + bean.getChannelName() + ",localId:" + bean.getLocalId());
            AgoraRunTime.getInstance().setStatus(AgoraRunTime.Status.calling);
            agoraSignal.channelInviteUser2(bean.getChannelName(), bean.getRemoteId(), bean.toString());
        } else {
            // 状态不正确，不能呼叫
            LogUtils.d(TAG,"状态错误！！！");
        }
    }

    @Subscribe(
            thread = EventThread.IO,
            tags = {
                    @Tag(TAG_AGORA_SERVICE)
            }
    )
    public void receivedMsgFromVideo(AgoraEventDispatch agoraReceived) {
        ITransfer bean = agoraReceived.getAttach();
        if (agoraReceived.getEventId() == RxConstants.EVENT_CHECKLOGIN_AND_JOIN_CHANNEL) {
            // 主动拨打电话
            joinChannel(bean);
        } else if (agoraReceived.getEventId() == RxConstants.EVENT_INVITE_USER) {
            inviteUser(bean);
        } else if (agoraReceived.getEventId() == RxConstants.EVENT_CHANNEL_INVITE_END) {
            // 用于当收到呼叫时，调用本接口拒绝收到的呼叫。主动结束呼叫
            if (AgoraRunTime.getInstance().getStatus() != AgoraRunTime.Status.hanguping) {
                AgoraRunTime.getInstance().setStatus(AgoraRunTime.Status.hanguping);
                agoraSignal.channelInviteEnd(bean.getChannelName(), bean.getRemoteId(), 0);
            }
            RxBus.get().post(RxConstants.RxEventTag.RESULT_END_INVITE, "");
        } else if (agoraReceived.getEventId() == RxConstants.EVENT_CHANNEL_INVITE_ACCEPT) {
            //String channelID,String account,int myUid(废弃字段) 发送同意加入
            if (AgoraRunTime.getInstance().getStatus() != AgoraRunTime.Status.joining) {
                AgoraRunTime.getInstance().setStatus(AgoraRunTime.Status.joining);
                agoraSignal.channelInviteAccept(bean.getChannelName(), bean.getRemoteId(), 0);
                agoraSignal.channelJoin(bean.getChannelName());
            }

        } else if (agoraReceived.getEventId() == RxConstants.EVENT_CHANNEL_INVITE_REFUSE) {
            //String channelID,String account,int myUid
            AgoraRunTime.getInstance().setStatus(AgoraRunTime.Status.hanguping);
            agoraSignal.channelInviteRefuse(bean.getChannelName(), bean.getRemoteId(), 0, "");
            // 未接听前拒绝呼叫方没有回调
            endInvite();
            RxBus.get().post(RxConstants.RxEventTag.RESULT_END_INVITE, "");
        } else if (agoraReceived.getEventId() == RxConstants.EVENT_CHANNEL_LOGOUT) {
            agoraSignal.logout();
        } else if (agoraReceived.getEventId() == RxConstants.EVENT_SEND_MESSAGE_PEER) {
            LogUtils.d(TAG,agoraReceived.getEventId()+"");
        } else if (agoraReceived.getEventId() == RxConstants.EVENT_SEND_MESSAGE_CHANNEL) {
            LogUtils.d(TAG,agoraReceived.getEventId()+"");
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        RxBus.get().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
        if (agoraSignal != null) {
            agoraSignal.logout();
        }
        RxBus.get().unregister(this);
    }

    public class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, final Intent intent) {
            ThreadPoolManager.getLongPool().execute(new Runnable() {
                @Override
                public void run() {
                    switch (intent.getAction()) {
                        case "android.net.conn.CONNECTIVITY_CHANGE":
                            LogUtils.d(TAG, "收到网络变化广播");
                            boolean isConnect = CommonUtils.isNetAvailable(MyApplication.getContext());
                            if (isConnect) {
                                initVideoCall();
                            }
                            break;
                        default:
                            break;
                    }
                }
            });

        }
    }
}
