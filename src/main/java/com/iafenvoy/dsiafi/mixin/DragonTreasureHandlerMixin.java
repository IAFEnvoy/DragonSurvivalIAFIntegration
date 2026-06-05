package com.iafenvoy.dsiafi.mixin;

import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonTreasureHandler;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.TreasureRestData;
import com.iafenvoy.iceandfire.item.block.PileBlock;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
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
        // is standing on a PileBlock, skip the cancellation
        if (!resting && event.getEntity().getBlockStateOn().getBlock() instanceof PileBlock) {
            return;
        }
        original.call(instance, resting);
    }
}
