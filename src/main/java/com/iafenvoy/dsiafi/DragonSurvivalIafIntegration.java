package com.iafenvoy.dsiafi;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.food.Foods;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.slf4j.Logger;

@Mod(DragonSurvivalIafIntegration.MOD_ID)
public class DragonSurvivalIafIntegration {
    public static final String MOD_ID = "dragon_survival_iaf_integration";
    public static final Logger LOGGER = LogUtils.getLogger();
}
