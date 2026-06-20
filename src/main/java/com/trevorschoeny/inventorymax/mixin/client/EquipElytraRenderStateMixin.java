package com.trevorschoeny.inventorymax.mixin.client;

import com.trevorschoeny.inventorymax.equipment.EquipElytraHolder;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds the per-frame equipment-elytra slot to {@code HumanoidRenderState} (and
 * thus {@code AvatarRenderState}, which extends it) so it travels from extraction
 * to {@link com.trevorschoeny.inventorymax.mixin.client.EquipElytraWingsMixin
 * the wings layer}. See {@link EquipElytraHolder}.
 */
@Mixin(HumanoidRenderState.class)
public class EquipElytraRenderStateMixin implements EquipElytraHolder {

    @Unique
    private ItemStack inventoryMax$equipElytra = ItemStack.EMPTY;

    @Override
    public ItemStack inventoryMax$getEquipElytra() {
        return this.inventoryMax$equipElytra;
    }

    @Override
    public void inventoryMax$setEquipElytra(ItemStack stack) {
        this.inventoryMax$equipElytra = stack;
    }
}
