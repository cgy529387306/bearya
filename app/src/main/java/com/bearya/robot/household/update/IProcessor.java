package com.bearya.robot.household.update;


import com.bearya.robot.household.utils.FileUtil;

/**
 * Created by yexifeng on 17/8/25.
 */

public  interface IProcessor {
    public boolean processor(FileUtil.ProcessorListener listener);
    public boolean reboot();

}