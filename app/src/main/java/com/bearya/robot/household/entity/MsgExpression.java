package com.bearya.robot.household.entity;

import com.bearya.robot.household.utils.MsgIDs;

/**
 * @author qjz
 * @since 12/05/17
 */
public class MsgExpression extends MsgBase{
    public class Expression {
        public String eId;
    }

    public MsgExpression() {
    }

    public MsgExpression(String speechId) {
        this.msgId = MsgIDs.MSG_EXPRESSION_ID;
        Expression ttsData = new Expression();
        ttsData.eId = speechId;
        setData(ttsData);
    }
}
