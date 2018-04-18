package com.bearya.robot.household.update.zip;

/**
 * Created by yexifeng on 17/8/31.
 */

public class ZipInfo {
    private String filePath;
    private int versionCode;

    public ZipInfo(int versionCode, String filePath) {
        this.versionCode = versionCode;
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }
}
