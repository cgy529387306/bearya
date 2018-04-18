package com.bearya.robot.household.http.retrofit;

/**
 * 后台返回数据封装类型
 * @param <T>
 */
public class HttpResult<T> {

    /** 服务器返回结果状态码 */
    private int status;

    /** 服务器返回的具体业务数据，为了统一处理，此处是指成功返回的结果模型 */
    private T data;

    /** 服务器错误信息 */
    private String text;

    public HttpResult(int status, T data, String text) {
        this.status = status;
        this.data = data;
        this.text = text;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isSuccess() {
        return BusinessStatusCodeEnum.SUCCESS_OK.getValue() == status;
    }

    public boolean isTokenInvalid() {
        return BusinessStatusCodeEnum.TOKEN_INVALID.getValue() == status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HttpResult<?> that = (HttpResult<?>) o;

        if (status != that.status) return false;
        return data != null ? data.equals(that.data) : that.data == null && (text != null ? text.equals(that.text) : that.text == null);

    }

    @Override
    public int hashCode() {
        int result = status;
        result = 31 * result + (data != null ? data.hashCode() : 0);
        result = 31 * result + (text != null ? text.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "HttpResult{" +
                "status=" + status +
                ", data=" + data +
                ", text='" + text + '\'' +
                '}';
    }
}
