package com.iafenvoy.dsiafi;

import com.iafenvoy.iceandfire.registry.IafAttributes;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;

@EventBusSubscriber
public class CommonEvents {
    @SubscribeEvent
    public static void attachForgeAttributes(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, IafAttributes.DRAGON_FORGE_SPEED, 0.025);
    }
}
