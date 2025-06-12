package com.iafenvoy.dsiafi.mixin;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import com.iafenvoy.iceandfire.entity.EntityDragonBase;
import com.iafenvoy.iceandfire.entity.ai.DragonAITarget;
import com.iafenvoy.iceandfire.entity.ai.DragonAITargetNonTamed;
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
@Mixin(EntityDragonBase.class)
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
        EntityDragonBase self = (EntityDragonBase) (Object) this;

        this.targetSelector.removeAllGoals(g -> g instanceof DragonAITargetNonTamed);
        this.targetSelector.addGoal(5, new DragonAITargetNonTamed<>(self, LivingEntity.class, false, (entity) -> {
            if (entity instanceof Player player) return !player.isCreative() && !DragonStateProvider.isDragon(player);
            else if (this.getRandom().nextInt(100) <= this.getHunger()) return false;
            else
                return entity.getType() != this.getType() && DragonUtils.canHostilesTarget(entity) && DragonUtils.isAlive(entity) && this.shouldTarget(entity);
        }));

        this.targetSelector.removeAllGoals(g -> g instanceof DragonAITarget);
        this.targetSelector.addGoal(6, new DragonAITarget<>(self, LivingEntity.class, true, (entity) -> (!(entity instanceof Player player) || (!player.isCreative() && !DragonStateProvider.isDragon(player))) && DragonUtils.canHostilesTarget(entity) && entity.getType() != this.getType() && this.shouldTarget(entity) && DragonUtils.isAlive(entity)));
    }
}
