package com.bearya.robot.household.videoCall.beans;

/**
 * 信令消息业务处理
 * Created by leo on 17/7/4.
 */

public class AgoraEventDispatch {
    private int eventId;
    private ITransfer attach;

    public AgoraEventDispatch(int eventId, ITransfer attach) {
        this.eventId = eventId;
        this.attach = attach;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public ITransfer getAttach() {
        return attach;
    }

    public void setAttach(AgoraTransferBean attach) {
        this.attach = attach;
    }
}
