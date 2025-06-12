package com.iafenvoy.dsiafi.mixin;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import com.iafenvoy.dsiafi.DragonArmorHelper;
import com.iafenvoy.iceandfire.data.DragonArmorPart;
import com.iafenvoy.iceandfire.item.ItemDragonArmor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

@Pseudo
@Mixin(ItemDragonArmor.class)
public class ItemDragonArmorMixin extends Item implements Equipable {
    @Shadow
    @Final
    public DragonArmorPart dragonSlot;

    public ItemDragonArmorMixin(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canEquip(@NotNull ItemStack stack, @NotNull EquipmentSlot armorType, @NotNull LivingEntity entity) {
        return DragonStateProvider.isDragon(entity) && entity.getEquipmentSlotForItem(stack) == armorType;
    }

    @Override
    public @NotNull ItemAttributeModifiers getDefaultAttributeModifiers(@NotNull ItemStack stack) {
        Item transformed = DragonArmorHelper.transform((ItemDragonArmor) (Object) this);
        return transformed.getDefaultAttributeModifiers(stack.transmuteCopy(transformed));
    }

    @Override
    public @NotNull EquipmentSlot getEquipmentSlot() {
        return switch (this.dragonSlot) {
            case HEAD -> EquipmentSlot.HEAD;
            case NECK -> EquipmentSlot.CHEST;
            case BODY -> EquipmentSlot.LEGS;
            case TAIL -> EquipmentSlot.FEET;
        };
    }
}
