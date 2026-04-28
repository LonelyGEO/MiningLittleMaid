package com.github.lonelygeo.mininglittlemaid.api.event;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * 采矿消息事件，允许联动模组拦截并替换采矿场景中的原始消息。
 * 通过 {@link net.neoforged.neoforge.common.NeoForge#EVENT_BUS} 发送。
 */
public class MiningMessageEvent extends Event implements ICancellableEvent {

    private final EntityMaid maid;
    private final MiningMessageType type;
    @Nullable
    private final String oreName;
    private final Component originalBubbleText;
    private final Component originalSystemText;
    private final Map<String, Object> context;

    public MiningMessageEvent(EntityMaid maid, MiningMessageType type,
                              @Nullable String oreName,
                              Component originalBubbleText, Component originalSystemText,
                              Map<String, Object> context) {
        this.maid = maid;
        this.type = type;
        this.oreName = oreName;
        this.originalBubbleText = originalBubbleText;
        this.originalSystemText = originalSystemText;
        this.context = context != null ? context : new HashMap<>();
    }

    public EntityMaid getMaid() {
        return maid;
    }

    public MiningMessageType getType() {
        return type;
    }

    @Nullable
    public String getOreName() {
        return oreName;
    }

    public Component getOriginalBubbleText() {
        return originalBubbleText;
    }

    public Component getOriginalSystemText() {
        return originalSystemText;
    }

    public Map<String, Object> getContext() {
        return context;
    }
}
