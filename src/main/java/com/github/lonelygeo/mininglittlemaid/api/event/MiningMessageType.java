package com.github.lonelygeo.mininglittlemaid.api.event;

/**
 * 采矿消息场景枚举，用于 {@link MiningMessageEvent}
 */
public enum MiningMessageType {
    ORE_ABOVE,
    ORE_BELOW,
    ORE_UNREACHABLE,
    INVENTORY_FULL,
    NO_TORCH,
    COMBAT_DETECTED
}
