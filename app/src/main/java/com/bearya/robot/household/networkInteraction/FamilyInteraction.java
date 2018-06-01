package com.bearya.robot.household.networkInteraction;

import android.content.Context;

import com.bearya.robot.household.entity.MsgMonitor;
import com.wilddog.wilddogcore.WilddogApp;
import com.wilddog.wilddogcore.WilddogOptions;

/**
 * Created by Qiujz on 2018/2/9.
 */

public class FamilyInteraction {

    private WilddogInteraction interaction = new WilddogInteraction();

    public static void initInteraction(Context context) {
        WilddogOptions options = new WilddogOptions.Builder().setSyncUrl(WilddogInteraction.WILDDOG_BASE_URL).build();
        WilddogApp.initializeApp(context, options);
    }

    public boolean init(String type, String serial) {
        return interaction.init(type, serial);
    }

    public void sendTTS(String tts) {
        interaction.sendTTS(tts);
    }

    public void sendAction(String id) {
        interaction.sendAction(id);
    }

    public void sendMove(String id) {
        interaction.sendMove(id);
    }

    public void sendExpression(String id) {
        interaction.sendExpression(id);
    }

    public void sendDance(String id) {
        interaction.sendDance(id);
    }

    public void sendMonitor(MsgMonitor msgMonitor) {
        interaction.sendMonitor(msgMonitor);
    }

    public void setValueEventListener(BYValueEventListener listener) {
        interaction.setValueEventListener(listener);
    }

    public void close() {
        interaction.close();
    }
}
