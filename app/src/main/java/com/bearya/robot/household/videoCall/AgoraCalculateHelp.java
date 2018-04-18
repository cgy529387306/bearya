package com.bearya.robot.household.videoCall;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

/**
 * Created by leo on 17/6/29.
 */

public class AgoraCalculateHelp {
    public static final String appID = "283093f77e634ddf8175a93db60fdfc7";
    public static final String certificate = "277d135966534bb9ad4f0b309e8f74ca";

    /*public static final String appID = "3d0b4e654c0a4ef08b97fa1757e33deb";
    public static final String certificate = "380c89086a28460f917405cbee47f100";*/


    public static String hexlify(byte[] data) {
        char[] DIGITS_LOWER = {'0', '1', '2', '3', '4', '5',
                '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

        /**
         * 用于建立十六进制字符的输出的大写字符数组
         */
        char[] DIGITS_UPPER = {'0', '1', '2', '3', '4', '5',
                '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

        char[] toDigits = DIGITS_LOWER;
        int l = data.length;
        char[] out = new char[l << 1];
        // two characters form the hex value.
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = toDigits[(0xF0 & data[i]) >>> 4];
            out[j++] = toDigits[0x0F & data[i]];
        }
        return String.valueOf(out);

    }

    public static String md5hex(byte[] s) {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(s);
            return hexlify(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }

    // 由 App ID 和 App Certificate 生成的 Signaling Key
    public static String calcToken(String account) {
        // Token = 1:appID:expiredTime:sign
        // Token = 1:appID:expiredTime:md5(account + vendorID + certificate + expiredTime)
        long expiredTimeWrong = new Date().getTime() / 1000 + 3600;
        long expiredTime = System.currentTimeMillis() / 1000 + 3600;

        String sign = md5hex((account + appID + certificate + expiredTime).getBytes());
        return "1:" + appID + ":" + expiredTime + ":" + sign;

    }


}
