package com.bearya.robot.household.entity;

import com.bearya.robot.household.utils.MsgIDs;

/**
 * @author qjz
 * @since 12/05/17
 */
public class MsgDance extends MsgBase{
    public class Dance {
        public String action;
    }

    public MsgDance() {
    }

    public MsgDance(String speechId) {
        this.msgId = MsgIDs.MSG_DANCE_ID;
        Dance dance = new Dance();
        dance.action = speechId;
        setData(dance);
    }
}
