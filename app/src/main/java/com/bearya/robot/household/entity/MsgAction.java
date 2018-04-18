package com.bearya.robot.household.entity;

import com.bearya.robot.household.utils.MsgIDs;

/**
 * @author qjz
 * @since 12/05/17
 */
public class MsgAction extends MsgBase{
    public class Action {
        public String action;
    }

    public MsgAction() {
    }

    public MsgAction(String speechId) {
        this.msgId = MsgIDs.MSG_ACTION_ID;
        Action action = new Action();
        action.action = speechId;
        setData(action);
    }
}
