package com.github.lonelygeo.mininglittlemaid;

import com.github.tartaricacid.touhoulittlemaid.api.ILittleMaid;
import com.github.tartaricacid.touhoulittlemaid.api.LittleMaidExtension;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager;
import com.github.lonelygeo.mininglittlemaid.task.TaskMining;

@LittleMaidExtension
public class MiningAddonPlugin implements ILittleMaid {
    @Override
    public void addMaidTask(TaskManager manager) {
        manager.add(new TaskMining());
    }
}
