package com.bearya.robot.household.threadpool;

/**
 * 线程池管理类
 * created by lianweidong on 2017／7／15
 */
public class ThreadPoolManager {

    private static ThreadPoolProxy mLongPool;                        // 耗时操作的池子
    private static ThreadPoolProxy mCanPool;                        // 耗时操作的池子
    private static final Object mLongLock = new Object();
    private static final Object mCanLock = new Object();

    /**
     * 获得耗时操作的池子
     *
     * @return
     */
    public static ThreadPoolProxy getLongPool() {
        if (mLongPool == null) {
            synchronized (mLongLock) {
                if (mLongPool == null) {
                    mLongPool = new ThreadPoolProxy(5, 5, 0L);
                }
            }
        }
        return mLongPool;
    }

    /**
     * 获得耗时操作的池子
     *
     * @return
     */
    public static ThreadPoolProxy getCanPool() {
        if (mCanPool == null) {
            synchronized (mCanLock) {
                if (mCanPool == null) {
                    mCanPool = new ThreadPoolProxy(5, 5, 0L);
                }
            }
        }
        return mCanPool;
    }
}
