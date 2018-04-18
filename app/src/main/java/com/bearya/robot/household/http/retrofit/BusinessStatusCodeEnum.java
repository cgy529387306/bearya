package com.bearya.robot.household.http.retrofit;

/**
 * 后台定义的业务状态码
 */
public enum BusinessStatusCodeEnum {

    /** 服务器返回系统错误 (505) */
    SERVER_ERROR(505),
    /** 请求处理成功 (200) */
    SUCCESS_OK(1),
    /** 其他状态码*/
    OTHER(0),
    /** TOKEN 失效 (200) */
    TOKEN_INVALID(-1);

    private int value;

    public int getValue() {
        return value;
    }

    BusinessStatusCodeEnum(int value) {
        this.value = value;
    }

    public static BusinessStatusCodeEnum valueOf(int value) {
        BusinessStatusCodeEnum[] values = BusinessStatusCodeEnum.values();
        for (BusinessStatusCodeEnum businessStatusCodesc : values) {
            if (value == businessStatusCodesc.getValue()) {
                return businessStatusCodesc;
            }
        }
        return BusinessStatusCodeEnum.OTHER;
    }

}
