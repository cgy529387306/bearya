package com.bearya.robot.household.entity;

import com.bearya.robot.household.utils.MsgIDs;

/**
 * @author qjz
 * @since 12/05/17
 */
public class MoveAction extends MsgBase{
    public class Action {
        public String action;
    }

    public MoveAction() {
    }

    public MoveAction(String speechId) {
        this.msgId = MsgIDs.MSG_MOVE_ID;
        Action action = new Action();
        action.action = speechId;
        setData(action);
    }
}
