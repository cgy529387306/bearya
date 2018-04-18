package com.bearya.robot.household.http.retrofit.download.DownLoadListener;


/**
 * 成功回调处理
 * Created by lianweidong on 2017/10/23.
 */
public interface DownloadProgressListener {
    /**
     * 下载进度
     * @param read
     * @param count
     * @param done
     */
    void update(long read, long count, boolean done);
}
