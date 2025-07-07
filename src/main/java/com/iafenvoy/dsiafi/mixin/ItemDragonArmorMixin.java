package com.iafenvoy.dsiafi.mixin;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import com.iafenvoy.dsiafi.DragonArmorHelper;
import com.iafenvoy.dsiafi.config.ArmorPointConfig;
import com.iafenvoy.iceandfire.data.DragonArmorPart;
import com.iafenvoy.iceandfire.item.ItemDragonArmor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Arrays;

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
        ItemDragonArmor self = (ItemDragonArmor) (Object) this;
        Item transformed = DragonArmorHelper.transform(self);
        int armor = ArmorPointConfig.get(self);
        ItemAttributeModifiers modifiers = transformed.getDefaultAttributeModifiers(stack.transmuteCopy(transformed));
        if (armor >= 0) {
            EquipmentSlot slot = this.getEquipmentSlot();
            ResourceLocation resourcelocation = ResourceLocation.withDefaultNamespace("armor." + Arrays.stream(ArmorItem.Type.values()).filter(x -> x.getSlot() == slot).findAny().orElse(ArmorItem.Type.BODY).getSerializedName());
            return modifiers.withModifierAdded(Attributes.ARMOR, new AttributeModifier(resourcelocation, armor, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.bySlot(slot));
        }
        return modifiers;
    }

    @Override
    public boolean isEnchantable(@NotNull ItemStack stack) {
        if (!stack.has(DataComponents.ENCHANTMENTS))
            stack.set(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        return true;
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        Item transformed = DragonArmorHelper.transform((ItemDragonArmor) (Object) this);
        return transformed.getEnchantmentValue(stack.transmuteCopy(transformed));
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
