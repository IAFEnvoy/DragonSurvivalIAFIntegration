# PileBlock Sleep Integration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Allow dragon players to sleep on Ice & Fire PileBlocks by adding two mixins mirroring TreasureBlock sleep behavior.

**Architecture:** Two mixins: `PileBlockMixin` overrides `isBed`/`getRespawnPosition`/`isPossibleToRespawnInThis` and injects into `useWithoutItem` to trigger the DS resting mechanic. `DragonTreasureHandlerMixin` wraps the `setResting(false)` call so PileBlocks aren't treated as "not a treasure" and the rest state persists.

**Tech Stack:** NeoForge 1.21.x, Mixin + MixinExtras, Dragon Survival API, Ice & Fire CE API

---

### Task 1: Create PileBlockMixin

**Files:**
- Create: `src/main/java/com/iafenvoy/dsiafi/mixin/PileBlockMixin.java`

- [ ] **Step 1: Write PileBlockMixin**

```java
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
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/iafenvoy/dsiafi/mixin/PileBlockMixin.java
git commit -m "feat: add PileBlockMixin for dragon sleep support"
```

---

### Task 2: Create DragonTreasureHandlerMixin

**Files:**
- Create: `src/main/java/com/iafenvoy/dsiafi/mixin/DragonTreasureHandlerMixin.java`

- [ ] **Step 1: Write DragonTreasureHandlerMixin**

This mixin wraps the `TreasureRestData.setResting(false)` call inside `DragonTreasureHandler.update()`. When the call is `setResting(false)` and the player is standing on a `PileBlock`, the call is skipped — keeping the rest state active.

```java
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
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/iafenvoy/dsiafi/mixin/DragonTreasureHandlerMixin.java
git commit -m "feat: add DragonTreasureHandlerMixin to accept PileBlock as valid rest surface"
```

---

### Task 3: Register mixins in mixins.json

**Files:**
- Modify: `src/main/resources/dragon_survival_iaf_integration.mixins.json`

- [ ] **Step 1: Add both new mixins to the mixins array**

Read the current file at `src/main/resources/dragon_survival_iaf_integration.mixins.json`:

```json
{
  "required": true,
  "minVersion": "0.8",
  "package": "com.iafenvoy.dsiafi.mixin",
  "compatibilityLevel": "JAVA_8",
  "refmap": "dragon_survival_iaf_integration.refmap.json",
  "mixins": [
    "DragonArmorRenderLayerMixin",
    "DragonBreathTargetMixin",
    "DragonBaseEntityMixin",
    "DragonArmorItemMixin"
  ],
  "client": [
  ],
  "injectors": {
    "defaultRequire": 1
  }
}
```

Add `"PileBlockMixin"` and `"DragonTreasureHandlerMixin"` to the `mixins` array:

```json
{
  "required": true,
  "minVersion": "0.8",
  "package": "com.iafenvoy.dsiafi.mixin",
  "compatibilityLevel": "JAVA_8",
  "refmap": "dragon_survival_iaf_integration.refmap.json",
  "mixins": [
    "DragonArmorRenderLayerMixin",
    "DragonBreathTargetMixin",
    "DragonBaseEntityMixin",
    "DragonArmorItemMixin",
    "PileBlockMixin",
    "DragonTreasureHandlerMixin"
  ],
  "client": [
  ],
  "injectors": {
    "defaultRequire": 1
  }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/resources/dragon_survival_iaf_integration.mixins.json
git commit -m "chore: register PileBlockMixin and DragonTreasureHandlerMixin"
```

---

### Task 4: Build verification

- [ ] **Step 1: Run the build**

```bash
./gradlew build
```

Expected: BUILD SUCCESSFUL. All mixin classes compile and the refmap is generated.

If the build fails:
- Check that `MixinExtras` is available (already used by existing `DragonArmorRenderLayerMixin` which imports `@Local`)
- Verify `PileBlock.LAYERS` is accessible via `@Shadow` — if the mixin processor can't resolve it, try referencing the property via `BlockStateProperties.LAYERS` instead (but this should work since PileBlock declares LAYERS as `public static final`)

- [ ] **Step 2: Commit if any fixes were needed**

```bash
git add -A
git commit -m "fix: build adjustments for PileBlock sleep mixins"
```
