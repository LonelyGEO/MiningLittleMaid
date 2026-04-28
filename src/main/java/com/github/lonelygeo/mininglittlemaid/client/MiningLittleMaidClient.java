package com.github.lonelygeo.mininglittlemaid.client;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = "mininglittlemaid", value = Dist.CLIENT)
public class MiningLittleMaidClient {
    public static double savedMouseX, savedMouseY;
    public static boolean hasSaved;

    @SubscribeEvent
    public static void onScreenClosing(ScreenEvent.Closing event) {
        if (event.getScreen() == null) return;
        long w = Minecraft.getInstance().getWindow().getWindow();
        double[] x = new double[1], y = new double[1];
        GLFW.glfwGetCursorPos(w, x, y);
        savedMouseX = x[0];
        savedMouseY = y[0];
        hasSaved = true;
    }
}
