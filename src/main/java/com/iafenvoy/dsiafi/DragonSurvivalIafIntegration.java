package com.iafenvoy.dsiafi;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(DragonSurvivalIafIntegration.MOD_ID)
public class DragonSurvivalIafIntegration {
    public static final String MOD_ID = "dragon_survival_iaf_integration";
    private static final Logger LOGGER = LogUtils.getLogger();

    public DragonSurvivalIafIntegration(IEventBus bus, ModContainer container) {
    }
}
