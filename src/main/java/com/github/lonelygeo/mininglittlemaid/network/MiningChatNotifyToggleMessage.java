package com.github.lonelygeo.mininglittlemaid.network;

import com.github.lonelygeo.mininglittlemaid.MiningLittleMaid;
import com.github.lonelygeo.mininglittlemaid.task.MaidMineBreakTask;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public record MiningChatNotifyToggleMessage(int maidId) implements CustomPacketPayload {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(MiningLittleMaid.MOD_ID, "chat_notify_toggle");
    public static final Type<MiningChatNotifyToggleMessage> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, MiningChatNotifyToggleMessage> STREAM_CODEC =
            StreamCodec.ofMember(MiningChatNotifyToggleMessage::encode, MiningChatNotifyToggleMessage::decode);

    public static MiningChatNotifyToggleMessage decode(RegistryFriendlyByteBuf buf) {
        return new MiningChatNotifyToggleMessage(buf.readInt());
    }

    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeInt(maidId);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MiningChatNotifyToggleMessage message, IPayloadContext context) {
        LOGGER.debug("Received chat notify toggle packet, maidId={}", message.maidId());
        context.enqueueWork(() -> {
            Entity entity = context.player().level().getEntity(message.maidId());
            if (entity != null) {
                MaidMineBreakTask.toggleForMaid(context.player().level(), message.maidId());
            }
        });
    }
}
