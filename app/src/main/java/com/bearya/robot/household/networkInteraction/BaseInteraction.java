package com.bearya.robot.household.networkInteraction;

import com.bearya.robot.household.entity.MsgMonitor;

/**
 * Created by Qiujz on 2018/2/9.
 */

public abstract class BaseInteraction {
    public abstract boolean init(String type, String serial);
    public abstract void sendTTS(String tts);
    public abstract void sendAction(String id);
    public abstract void sendExpression(String id);
    public abstract void sendDance(String id);
    public abstract void sendMonitor(MsgMonitor msgMonitor);
    public abstract void setValueEventListener(BYValueEventListener listener);
    public abstract void close();
}
