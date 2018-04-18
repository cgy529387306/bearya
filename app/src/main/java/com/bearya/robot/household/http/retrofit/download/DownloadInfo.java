package com.bearya.robot.household.http.retrofit.download;

/**
 * apk下载请求数据基础类
 * Created by lianweidong on 2017/10/23.
 */

public class DownloadInfo {
    /*存储位置*/
    private String savePath;
    /*文件总长度*/
    private long filesize;
    /*下载长度*/
    private long readSize;
    /*下载唯一的HttpService*/
    private HttpDownService service;
    /*回调监听*/
    private HttpDownOnNextListener listener;
    /*超时设置*/
    private  int connectonTime=6;
    /*state状态数据库保存*/
    private int stateInte;
    /*url*/
    private String download_url;

    public String from_version;
    public String to_version;

    public DownloadInfo(String url, HttpDownOnNextListener listener) {
        setUrl(url);
        setListener(listener);
    }

    public DownloadInfo(String url) {
        setUrl(url);
    }

    public DownloadInfo() {
    }


    public DownloadState getState() {
        switch (getStateInte()){
            case 0:
                return DownloadState.START;
            case 1:
                return DownloadState.DOWN;
            case 2:
                return DownloadState.PAUSE;
            case 3:
                return DownloadState.STOP;
            case 4:
                return DownloadState.ERROR;
            case 5:
            default:
                return DownloadState.FINISH;
        }
    }

    public void setState(DownloadState state) {
        setStateInte(state.getState());
    }


    public int getStateInte() {
        return stateInte;
    }

    public void setStateInte(int stateInte) {
        this.stateInte = stateInte;
    }

    public HttpDownOnNextListener getListener() {
        return listener;
    }

    public void setListener(HttpDownOnNextListener listener) {
        this.listener = listener;
    }

    public HttpDownService getService() {
        return service;
    }

    public void setService(HttpDownService service) {
        this.service = service;
    }

    public String getUrl() {
        return download_url;
    }

    public void setUrl(String url) {
        this.download_url = url;
    }

    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }


    public long getFileSize() {
        return filesize;
    }

    public void setFileSize(long countLength) {
        this.filesize = countLength;
    }


    public long getReadSize() {
        return readSize;
    }

    public void setReadSize(long readSize) {
        this.readSize = readSize;
    }

    public int getConnectonTime() {
        return this.connectonTime;
    }

    public void setConnectonTime(int connectonTime) {
        this.connectonTime = connectonTime;
    }

    public String getFrom_version() {
        return from_version;
    }

    public void setFrom_version(String from_version) {
        this.from_version = from_version;
    }

    public String getTo_version() {
        return to_version;
    }

    public void setTo_version(String to_version) {
        this.to_version = to_version;
    }
}
