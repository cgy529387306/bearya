package com.bearya.robot.household.videoCall.beans;

/**
 * 运行时产生的数据结构
 * Created by leo on 17/7/11.
 */

public class AgoraRunTime {

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public enum Status {
        init,// 初始状态
        logining, //正在登录
        logined,// 已经登录
        loginFailed,// 登录失败
        joining,// 正在加入信道
        joineded,// 已经加入渠道
        joinFailed, // 加入信道失败
        calling,// 主动呼叫
        beCalled,// 被动呼叫
        callFailed,// 呼叫失败

        answer,// 接听
        refuse, // 拒绝接听

        hanguping,// 正在挂断
        hanguped,// 主动挂断
        beHangup,// 被动挂断
        leaving,// 正在离开channel
        leaved, // 已离开信道
        logout // 已经退出登录
    }

    private String channel;
    private String localName;
    private String remoteName;
    private Status status = Status.init;
    private String msg;

    // 加入信道的用户
    private String joinedName;
    // 离开信道的用户
    private String leaveName;

    private static AgoraRunTime runTime;

    public static AgoraRunTime getInstance() {
        if (runTime == null) {
            runTime = new AgoraRunTime();
        }
        return runTime;
    }

    public void reset() {
        runTime = null;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }


    public String getLocalName() {
        return localName;
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }

    public String getRemoteName() {
        return remoteName;
    }

    public void setRemoteName(String remoteName) {
        this.remoteName = remoteName;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getJoinedName() {
        return joinedName;
    }

    public void setJoinedName(String joinedName) {
        this.joinedName = joinedName;
    }

    public String getLeaveName() {
        return leaveName;
    }

    public void setLeaveName(String leaveName) {
        this.leaveName = leaveName;
    }
}
