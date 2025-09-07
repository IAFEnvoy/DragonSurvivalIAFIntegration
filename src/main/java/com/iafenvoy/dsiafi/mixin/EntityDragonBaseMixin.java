package com.iafenvoy.dsiafi.mixin;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import com.iafenvoy.iceandfire.config.IafCommonConfig;
import com.iafenvoy.iceandfire.entity.DragonBaseEntity;
import com.iafenvoy.iceandfire.entity.ai.DragonAITargetGoal;
import com.iafenvoy.iceandfire.entity.ai.DragonAITargetNonTamedGoal;
import com.iafenvoy.iceandfire.entity.util.dragon.DragonUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(DragonBaseEntity.class)
public abstract class EntityDragonBaseMixin extends TamableAnimal {
    @Shadow
    protected abstract boolean shouldTarget(Entity entity);

    @Shadow
    public abstract int getHunger();

    protected EntityDragonBaseMixin(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "registerGoals", at = @At("RETURN"))
    private void handleTarget(CallbackInfo ci) {
        DragonBaseEntity self = (DragonBaseEntity) (Object) this;

        this.targetSelector.removeAllGoals(g -> g instanceof DragonAITargetNonTamedGoal<?>);
        this.targetSelector.addGoal(5, new DragonAITargetNonTamedGoal<>(self, LivingEntity.class, false, (entity) -> {
            if (entity instanceof Player player)
                return !player.isCreative() && !IafCommonConfig.INSTANCE.dragon.neutralToPlayer.getValue() && !DragonStateProvider.isDragon(player);
            else if (this.getRandom().nextInt(100) <= this.getHunger()) return false;
            else
                return entity.getType() != this.getType() && DragonUtils.canHostilesTarget(entity) && DragonUtils.isAlive(entity) && this.shouldTarget(entity);
        }));

        this.targetSelector.removeAllGoals(g -> g instanceof DragonAITargetGoal<?>);
        this.targetSelector.addGoal(6, new DragonAITargetGoal<>(self, LivingEntity.class, true, (entity) -> (!(entity instanceof Player player) || (!player.isCreative() && (entity instanceof Player ? !IafCommonConfig.INSTANCE.dragon.neutralToPlayer.getValue() : !DragonStateProvider.isDragon(player))) && DragonUtils.canHostilesTarget(entity) && entity.getType() != this.getType() && this.shouldTarget(entity) && DragonUtils.isAlive(entity))));
    }
}
