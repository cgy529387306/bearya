package com.bearya.robot.household.videoCall.beans;

/**
 * Created by leo on 17/7/12.
 */

public interface ITransfer {
    String getChannelName();
    String getLocalName();
    String getRemoteId();
    String getLocalId();
    String getRemoteName();
    boolean isVideoCall();
}
