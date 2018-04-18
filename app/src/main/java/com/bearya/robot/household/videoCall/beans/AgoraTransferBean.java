package com.bearya.robot.household.videoCall.beans;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 与信令系统的数据传递实例
 * Created by leo on 17/7/4.
 */

public class AgoraTransferBean implements ITransfer {
    private String channleID = "";
    private String remoteId = "";
    private String remoteName = "";
    private String localPic = "";

    private int myUid;
    private String extra = "";
    private boolean beInvitedTojoin;
    private String attExtra = "";
    private boolean isVideoCall;
    // 是否登录后自动加入信道
    private boolean isJoinChannelAfterLogined;
    // 是否通信已经加入信道

    private boolean isRtcEngineJoinChannel;

    public AgoraTransferBean(String channelID, String remoteId, int localId, String extra, boolean beInvitedTojoin, String attExtra, int callingStatus, String localPic) {
        this.channleID = channelID;
        this.remoteId = remoteId;
        this.myUid = localId;
        this.extra = extra;
        this.beInvitedTojoin = beInvitedTojoin;
        this.attExtra = attExtra;
        this.localPic = localPic;
    }

    public AgoraTransferBean() {
    }

    public String getChannleID() {
        return channleID;
    }

    public void setChannleID(String channleID) {
        this.channleID = channleID;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public String getAttExtra() {
        return attExtra;
    }

    public void setAttExtra(String attExtra) {
        this.attExtra = attExtra;
    }

    public boolean isBeInvitedTojoin() {
        return beInvitedTojoin;
    }

    public void setBeInvitedTojoin(boolean beInvitedTojoin) {
        this.beInvitedTojoin = beInvitedTojoin;
    }

    @Override
    public String getRemoteId() {
        return remoteId;
    }

    @Override
    public String getLocalId() {
        return myUid + "";
    }

    public void setRemoteId(String remoteId) {
        this.remoteId = remoteId;
    }

    public int getMyUid() {
        return myUid;
    }

    public void setMyUid(int myUid) {
        this.myUid = myUid;
    }

    @Override
    public boolean isVideoCall() {
        return isVideoCall;
    }

    public void setVideoCall(boolean videoCall) {
        isVideoCall = videoCall;
    }

    public String getRemoteName() {
        return remoteName;
    }

    public void setRemoteName(String remoteName) {
        this.remoteName = remoteName;
    }

    @Override
    public String toString() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("channleID", channleID);
            obj.put("remoteId", remoteId);
            obj.put("remoteName", remoteName);

            obj.put("extra", extra);
            obj.put("beInvitedTojoin", beInvitedTojoin);
            obj.put("isVideoCall", isVideoCall);
            obj.put("localPic", localPic);
            obj.put("attExtra", attExtra);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return obj.toString();
    }

    @Override
    public String getChannelName() {
        return channleID;
    }

    @Override
    public String getLocalName() {
        return myUid + "";
    }


    public boolean isJoinChannelAfterLogined() {
        return isJoinChannelAfterLogined;
    }

    public void setJoinChannelAfterLogined(boolean joinChannelAfterLogined) {
        isJoinChannelAfterLogined = joinChannelAfterLogined;
    }

    public boolean isRtcEngineJoinChannel() {
        return isRtcEngineJoinChannel;
    }

    public void setRtcEngineJoinChannel(boolean rtcEngineJoinChannel) {
        isRtcEngineJoinChannel = rtcEngineJoinChannel;
    }

    public String getLocalPic() {
        return localPic;
    }

    public void setLocalPic(String localPic) {
        this.localPic = localPic;
    }
}
