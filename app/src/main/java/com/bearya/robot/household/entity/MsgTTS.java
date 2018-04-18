package com.bearya.robot.household.entity;

import com.bearya.robot.household.utils.MsgIDs;

/**
 * @author qjz
 * @since 12/05/17
 */
public class MsgTTS extends MsgBase{
    public class TTS {
        public String tts;
    }

    public MsgTTS() {
    }

    public MsgTTS(String tts) {
        this.msgId = MsgIDs.MSG_TTS_ID;
        TTS ttsData = new TTS();
        ttsData.tts = tts;
        setData(ttsData);
    }
}
