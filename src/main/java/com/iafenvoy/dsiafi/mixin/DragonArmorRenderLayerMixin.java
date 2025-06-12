package com.iafenvoy.dsiafi.mixin;

import by.dragonsurvivalteam.dragonsurvival.client.render.entity.dragon.DragonArmorRenderLayer;
import com.iafenvoy.dsiafi.DragonArmorHelper;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DragonArmorRenderLayer.class)
public class DragonArmorRenderLayerMixin {
    @Inject(method = "generateArmorTextureResourceLocation", at = @At(value = "INVOKE", target = "Lby/dragonsurvivalteam/dragonsurvival/client/render/entity/dragon/DragonArmorRenderLayer;toArmorResource(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/world/item/Item;)Lnet/minecraft/resources/ResourceLocation;"))
    private static void handleArmorItem(CallbackInfoReturnable<ResourceLocation> cir, @Local LocalRef<Item> item) {
        item.set(DragonArmorHelper.transform(item.get()));
    }
}
