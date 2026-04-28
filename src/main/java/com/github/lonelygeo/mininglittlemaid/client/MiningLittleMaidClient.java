package com.github.lonelygeo.mininglittlemaid.client;

import com.github.lonelygeo.mininglittlemaid.MiningLittleMaid;
import com.github.lonelygeo.mininglittlemaid.client.gui.MiningTaskConfigGui;
import com.github.lonelygeo.mininglittlemaid.inventory.container.MiningTaskConfigContainer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = MiningLittleMaid.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class MiningLittleMaidClient {

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(MiningTaskConfigContainer.TYPE, MiningTaskConfigGui::new);
    }
}
