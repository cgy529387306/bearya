package com.bearya.robot.household.entity;

/**
 * Created by xifengye on 2017/1/22.
 */
public class MsgBase<T> {
    public int msgId;
    public String uuid;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    private T data;

    public MsgBase() {
    }

    public MsgBase(int msgId) {
        this.msgId = msgId;
    }

    public int getMsgId() {
        return msgId;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void createUuid(){
        uuid = String.valueOf(System.currentTimeMillis());
    }

}
