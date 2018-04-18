package com.bearya.robot.household.update;

import java.util.List;

/**
 * Created by yexifeng on 17/8/25.
 */

public interface IMutilProcessor {
    public List<IProcessor> getMutilProcessor();
    public boolean reboot();
    public boolean isEmpty();
}


