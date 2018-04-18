package com.bearya.robot.household.videoCall;

/**
 * RxConstants
 */
public class RxConstants {
    public static boolean isCalling = false;
    public static boolean isDebug = true;
    //public final static String WX_APP_ID = "wx15c26c87b5c49c87";

    public final static class URL {
        public static final String signin = "http://open.bearya.com/social/habit/signin";
    }

    /**
     * Event type like tags etc...
     */
    public final class RxEventTag {
        public final static String RESULT_JOIN_CHANNEL = "result_join_channel";
        public final static String TAG_AGORA_SERVICE = "ServiceMessage";
        public final static String RESULT_INVITE_USER = "result_invite_user";
        public final static String EVENT_MSG_SHOW = "msg_toast";
        public final static String RESULT_CHANNEL_MESSAGE = "channel_message";
        public final static String RESULT_LOGIN = "result_login";
        public final static String RESULT_END_INVITE = "result_end_invite";
        public final static String RESULT_WX_LOGIN = "result_wx_login";
    }

    public final static int EVENT_CHECKLOGIN_AND_JOIN_CHANNEL = 1;
    public final static int EVENT_INVITE_USER = 2;
    public final static int EVENT_SEND_MESSAGE_PEER = 3;
    public final static int EVENT_SEND_MESSAGE_CHANNEL = 4;
    public final static int EVENT_CHANNEL_INVITE_END = 5;
    public final static int EVENT_CHANNEL_INVITE_ACCEPT = 6;
    public final static int EVENT_CHANNEL_INVITE_REFUSE = 7;
    public final static int EVENT_CHANNEL_LOGOUT = 8;

    public final static int RESULT_JOIN_CHANNEL_FAILED = 3;

    public final static int RESULT_USER_JOIN_CHANNEL = 0;
    public final static int RESULT_USER_LEFT_CHANNEL = 1;

    public enum ERCODE {
        RESULT_LOGIN_NO_DATA,
        RESULT_LOGIN_DATA_PARSE,
        RESULT_USER_UNMATCH,
    }

    public static final class Error {
        public ERCODE code;
        public String message;

        public Error(ERCODE code) {

            this.code = code;
            if (this.code == ERCODE.RESULT_LOGIN_NO_DATA) {
                this.message = "网页登录结果返回空";
            } else if (this.code == ERCODE.RESULT_LOGIN_DATA_PARSE) {
                this.message = "网页登录结果解析错误";
            } else if (this.code == ERCODE.RESULT_USER_UNMATCH) {
                this.message = "用户不匹配";
            } else {
                this.message = "未知错误";
            }
        }

        @Override
        public String toString() {
            return "Error{" +
                    "code=" + code + (isDebug ?
                    ", message='" + message + '\'' : "") +
                    '}';
        }
    }
}
