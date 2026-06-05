package com.iafenvoy.dsiafi.mixin;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.network.status.SyncResting;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.TreasureRestData;
import com.iafenvoy.iceandfire.item.block.PileBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Pseudo
@Mixin(PileBlock.class)
public class PileBlockMixin extends Block {

    @Shadow
    @Final
    public static IntegerProperty LAYERS;

    public PileBlockMixin(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isBed(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull LivingEntity sleeper) {
        return DragonStateProvider.isDragon(sleeper);
    }

    @Override
    public @NotNull Optional<ServerPlayer.RespawnPosAngle> getRespawnPosition(@NotNull BlockState state, @NotNull EntityType<?> type, @NotNull LevelReader levelReader, @NotNull BlockPos pos, float orientation) {
        if (levelReader instanceof Level) {
            Optional<Vec3> standUpPosition = RespawnAnchorBlock.findStandUpPosition(type, levelReader, pos);
            if (standUpPosition.isPresent()) {
                return Optional.of(new ServerPlayer.RespawnPosAngle(standUpPosition.get(), orientation));
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean isPossibleToRespawnInThis(@NotNull BlockState ignored) {
        return true;
    }

    @Inject(method = "useWithoutItem", at = @At("HEAD"), cancellable = true)
    private void handleDragonSleep(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, Player player, @NotNull BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack heldItem = player.getInventory().getSelected();

        // If holding the same pile item and not at max layers, let original stacking logic run
        if (!heldItem.isEmpty() && heldItem.getItem() == this.asItem() && state.getValue(LAYERS) < 8) {
            return;
        }

        // Dragon sleep: only if standing on the block (same as TreasureBlock behavior)
        if (DragonStateProvider.isDragon(player) && player.getBlockStateOn().getBlock() == state.getBlock()) {
            TreasureRestData treasureData = TreasureRestData.getData(player);
            treasureData.setResting(true);

            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));
                serverPlayer.serverLevel().updateSleepingPlayerList();
                PacketDistributor.sendToPlayersTrackingEntityAndSelf(serverPlayer, new SyncResting(serverPlayer.getId(), treasureData.isResting()));

                BlockPos respawnPos = serverPlayer.getRespawnPosition();
                if (respawnPos == null || serverPlayer.getRespawnDimension() != world.dimension() || (!respawnPos.equals(pos) && respawnPos.distSqr(pos) > 40)) {
                    serverPlayer.setRespawnPosition(world.dimension(), pos, 0, false, true);
                }
            }

            cir.setReturnValue(InteractionResult.sidedSuccess(world.isClientSide()));
        }
    }
}
