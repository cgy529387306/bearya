package com.bearya.robot.household.update.zip;

import com.bearya.robot.household.update.IProcessor;
import com.bearya.robot.household.utils.FileUtil;
import com.bearya.robot.household.utils.LogUtils;

import java.io.File;

/**
 * Created by yexifeng on 17/8/25.
 */

public class Zipper implements IProcessor {
    private ZipInfo zipInfo;

    public Zipper(ZipInfo zipInfo) {
        this.zipInfo = zipInfo;
    }


    private boolean unZip(final FileUtil.ProcessorListener listener){
        LogUtils.e("zipPath=%s"+zipInfo.getFilePath());
        FileUtil.unZipFile(zipInfo.getVersionCode(),zipInfo.getFilePath(), getResourceDir(),listener);
        return true;
    }

    private String getResourceDir(){
        String path = FileUtil.getSDPath();
        File file = new File(path);
        if(file.exists()){
            file.mkdirs();
        }
        return path;
    }

    @Override
    public boolean processor(FileUtil.ProcessorListener listener) {
        unZip(listener);
        return true;
    }

    @Override
    public boolean reboot() {
        return false;
    }
}
