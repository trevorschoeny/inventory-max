package com.trevorschoeny.inventorymax.equipment;

import com.trevorschoeny.menukit.core.GraftScreenContext;
import com.trevorschoeny.menukit.core.GraftScreenPresence;
import com.trevorschoeny.menukit.core.GraftSlots;
import com.trevorschoeny.menukit.core.MenuKitGraft;
import com.trevorschoeny.menukit.core.MenuKitSlot;
import com.trevorschoeny.menukit.core.SlotRendering;
import com.trevorschoeny.menukit.inject.VanillaSlotResolver;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

/**
 * Registers the equipment slots' screen presence with MenuKit's graft dispatch —
 * the library draws / hover-routes / clicks the grafted elytra + totem slots on
 * every inventory-bearing screen (creative included). We supply only what's ours:
 * the per-screen POSITION (survival above the offhand; creative beside the armor
 * column) and the empty-slot placeholder icon.
 *
 * <p>This replaces {@code CreativeEquipmentSlotMixin} and the equipment half of
 * the old {@code PocketRenderMixin}; the equip slots showing identically on
 * survival and creative with no per-screen mixin is the proof the primitive is
 * complete.
 */
public final class EquipScreenPresence {

    private EquipScreenPresence() {}

    /** Register both equip slot presences. Call once at IM client init. */
    public static void register() {
        // Distance above the offhand: totem directly above it, elytra above the totem.
        present(EquipmentSlots.ELYTRA_GROUP, 2);
        present(EquipmentSlots.TOTEM_GROUP, 1);
    }

    private static void present(String group, int slotsAboveOffhand) {
        // Equip slots belong to the player's own inventory only — survival
        // InventoryScreen + creative inventory tab — never to chests/furnaces.
        GraftScreenPresence.forPanel(EquipmentSlots.MOD_ID + ":" + group)
                .onlyScreens(InventoryScreen.class, CreativeModeInventoryScreen.class)
                .onPrepare(ctx -> position(ctx, group, slotsAboveOffhand))
                .foreground((ctx, g) -> drawIcon(ctx, g, group))
                .register();
    }

    /**
     * Place the graft directly above the real offhand slot — the <em>same</em> call
     * on every screen. MenuKit's {@link VanillaSlotResolver} returns the offhand's
     * live position, unwrapping creative's {@code SlotWrapper}, so the old
     * survival/creative coordinate split is gone: both were the one intent — "above
     * the offhand" — and it now resolves on whatever screen the dispatch fired on,
     * with no hardcoded creative coordinates.
     */
    private static void position(GraftScreenContext ctx, String group, int slotsAboveOffhand) {
        MenuKitSlot mk = find(ctx, group);
        if (mk == null) return;
        // Offhand is player-inventory container index Inventory.SLOT_OFFHAND (40); its
        // slot keeps container == Inventory on every screen, so resolve-by-index works
        // through the creative wrapper too. graftX/Y are frame coords (item − 1).
        VanillaSlotResolver.resolveSlot(ctx.menu(), Inventory.SLOT_OFFHAND).ifPresent(offhand ->
                mk.setGraftPosition(offhand.x - 1,
                        offhand.y - 1 - slotsAboveOffhand * MenuKitGraft.SLOT_PITCH));
    }

    /** Faint empty-slot placeholder over the slot (library draws the frame + item). */
    private static void drawIcon(GraftScreenContext ctx, GuiGraphics g, String group) {
        MenuKitSlot mk = find(ctx, group);
        if (mk == null || !mk.getItem().isEmpty()) return;
        Identifier sprite = EquipSlotIcons.spriteFor(group);
        if (sprite == null) return;
        EquipSlotIcons.draw(g, sprite,
                ctx.leftPos() + mk.graftX() + SlotRendering.ITEM_INSET,
                ctx.topPos() + mk.graftY() + SlotRendering.ITEM_INSET);
    }

    /**
     * The equip graft for {@code group} as it appears in this screen's <em>live</em>
     * menu — directly on the survival inventory, or unwrapped from its creative
     * {@code SlotWrapper} on the creative inventory tab via {@link GraftSlots#asGraft}.
     * Null when this screen carries no such graft (e.g. a non-inventory creative
     * tab), which makes {@link #position} and {@link #drawIcon} self-suppress there.
     */
    private static MenuKitSlot find(GraftScreenContext ctx, String group) {
        for (Slot s : ctx.menu().slots) {
            MenuKitSlot mk = GraftSlots.asGraft(s);
            if (mk != null && group.equals(mk.getGroupId())) return mk;
        }
        return null;
    }
}
