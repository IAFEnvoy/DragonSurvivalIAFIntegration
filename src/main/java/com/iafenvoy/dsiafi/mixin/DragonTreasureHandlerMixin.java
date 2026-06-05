package com.iafenvoy.dsiafi.mixin;

import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonTreasureHandler;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.TreasureRestData;
import com.iafenvoy.iceandfire.item.block.PileBlock;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(DragonTreasureHandler.class)
public class DragonTreasureHandlerMixin {

    @WrapOperation(
            method = "update",
            at = @At(value = "INVOKE", target = "Lby/dragonsurvivalteam/dragonsurvival/registry/attachments/TreasureRestData;setResting(Z)V")
    )
    private static void wrapCancelRest(
            TreasureRestData instance,
            boolean resting,
            Operation<Void> original,
            @Local(argsOnly = true) PlayerTickEvent.Post event
    ) {
        // If the handler is trying to cancel resting (resting=false) but the player
        // is on a PileBlock, skip the cancellation.
        // Check both getBlockStateOn (block below feet) and block at player position
        // because 1-layer PileBlock has empty collision so getBlockStateOn may return
        // the block beneath the pile instead.
        if (!resting) {
            Player player = event.getEntity();
            BlockState onBlock = player.getBlockStateOn();
            BlockState atFeet = player.level().getBlockState(player.blockPosition());
            if (onBlock.getBlock() instanceof PileBlock || atFeet.getBlock() instanceof PileBlock) {
                return;
            }
        }
        original.call(instance, resting);
    }
}
