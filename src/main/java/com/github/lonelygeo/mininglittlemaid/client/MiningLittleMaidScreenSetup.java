package com.github.lonelygeo.mininglittlemaid.client;

import com.github.lonelygeo.mininglittlemaid.MiningLittleMaid;
import com.github.lonelygeo.mininglittlemaid.client.gui.MiningTaskConfigGui;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = "mininglittlemaid", value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class MiningLittleMaidScreenSetup {

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(MiningLittleMaid.MINING_TASK_CONFIG.get(), MiningTaskConfigGui::new);
    }
}
