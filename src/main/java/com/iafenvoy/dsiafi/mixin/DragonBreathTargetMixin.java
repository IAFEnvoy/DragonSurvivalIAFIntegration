package com.iafenvoy.dsiafi.mixin;

import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.DragonBreathTarget;
import com.iafenvoy.iceandfire.item.block.entity.DragonForgeInputBlockEntity;
import com.iafenvoy.iceandfire.registry.IafAttributes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(DragonBreathTarget.class)
public class DragonBreathTargetMixin {
    @Inject(method = "apply", at = @At("HEAD"))
    private void handleDragonForgeTick(ServerPlayer dragon, DragonAbilityInstance ability, CallbackInfo ci) {
        HitResult result = dragon.pick(10, 0, false);
        if (result instanceof BlockHitResult blockHitResult && dragon.serverLevel().getBlockEntity(blockHitResult.getBlockPos()) instanceof DragonForgeInputBlockEntity dragonForgeInput)
            dragonForgeInput.onHitWithFlame(dragon.getAttributeValue(IafAttributes.DRAGON_FORGE_SPEED));
    }
}
