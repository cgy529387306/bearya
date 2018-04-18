package com.bearya.robot.household.http.retrofit;

/**
 * Http协议状态码
 */
public enum HttpStatusCodeEnum {

    /** 链接资源未找到 (404) */
    URL_NOT_FOUND(404),
    /** 会话失效 (201) */
    SESSION_INVALID(201),
    /** 会话验证失败 (402) */
    SESSION_ERROR(402),
    /** 请求处理成功 (200) */
    SUCCESS_OK(200),
    /** 未授权 */
    CLIENT_ERROR_FORBIDDEN(403),
    /** 其他状态码*/
    OTHER(0);

    private int value;

    public int getValue() {
        return value;
    }

    HttpStatusCodeEnum(int value) {
        this.value = value;
    }

    public static HttpStatusCodeEnum valueOf(int value) {
        HttpStatusCodeEnum[] values = HttpStatusCodeEnum.values();
        for (HttpStatusCodeEnum httpStatusCodeEnum : values) {
            if (value == httpStatusCodeEnum.getValue()) {
                return httpStatusCodeEnum;
            }
        }
        return HttpStatusCodeEnum.OTHER;
    }

}
