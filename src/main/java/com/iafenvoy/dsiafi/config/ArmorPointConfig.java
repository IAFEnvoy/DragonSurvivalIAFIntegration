package com.iafenvoy.dsiafi.config;

import com.google.gson.JsonParser;
import com.iafenvoy.dsiafi.DragonSurvivalIafIntegration;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class ArmorPointConfig {
    public static final Codec<Map<Item, Integer>> CODEC = Codec.unboundedMap(BuiltInRegistries.ITEM.byNameCodec(), Codec.INT);
    private static final String PATH = "./config/dsiafi.json";
    private static final Map<Item, Integer> DATA = new HashMap<>();

    static {
        try {
            DATA.putAll(CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(new FileReader(PATH))).getPartialOrThrow());
        } catch (Exception e) {
            DragonSurvivalIafIntegration.LOGGER.error("Failed to load dsiafi.json", e);
        }
    }

    public static int get(Item item) {
        return DATA.getOrDefault(item, -1);
    }
}
