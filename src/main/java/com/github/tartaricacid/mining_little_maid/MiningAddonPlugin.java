package com.github.tartaricacid.mining_little_maid;

import com.github.tartaricacid.touhoulittlemaid.api.ILittleMaid;
import com.github.tartaricacid.touhoulittlemaid.api.LittleMaidExtension;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager;
import com.github.tartaricacid.mining_little_maid.task.TaskMining;

@LittleMaidExtension
public class MiningAddonPlugin implements ILittleMaid {
    @Override
    public void addMaidTask(TaskManager manager) {
        manager.add(new TaskMining());
    }
}
