package com.bearya.robot.household.entity;

import com.bearya.robot.household.utils.MsgIDs;

/**
 * @author qjz
 * @since 12/05/17
 */
public class MsgMonitor extends MsgBase{
    public class Monitor {
        public String state;
        public String appid;
        public String channel;
        public int uid;
        public String key;
    }

    public MsgMonitor() {
    }

    public MsgMonitor(String state, String appid, String channel, int uid, String key) {
        this.msgId = MsgIDs.MSG_MONITOR_ID;
        Monitor monitor = new Monitor();
        monitor.state = state;
        monitor.appid = appid;
        monitor.channel = channel;
        monitor.uid = uid;
        monitor.key = key;
        setData(monitor);
    }
}
