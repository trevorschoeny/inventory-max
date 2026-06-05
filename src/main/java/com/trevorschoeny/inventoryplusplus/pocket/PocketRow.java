package com.trevorschoeny.inventoryplusplus.pocket;

import com.trevorschoeny.menukit.core.MenuKitSlot;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

/**
 * Drives the §0047 runtime reposition of the revealed pocket slots into the
 * floating horizontal row centered above the hovered hotbar slot.
 *
 * <p>Grafted slots are laid out vertically at construction (the graft can't know
 * the per-world count or which slot is hovered); §0047 makes their position
 * mutable presentation. So each client render frame — <em>before</em> the
 * grafted slots draw and before input hit-tests them — we move the revealed
 * hotbar's first {@code count} pockets onto {@link Pockets#pocketRowX} /
 * {@link Pockets#pocketRowY}. Render and clicks both read {@code graftX/graftY},
 * so both follow the live row.
 *
 * <p>Only the revealed pockets are moved; the rest stay inert (§0021) and
 * invisible, so their stale (vertical) positions never show. Nothing to reset
 * when the hover moves on — the previously-revealed column simply goes inert.
 */
public final class PocketRow {

    private PocketRow() {}

    /** Position the revealed hotbar's pockets into the centered horizontal row. */
    public static void reposition(AbstractContainerMenu menu) {
        int rev = PocketHoverState.revealedHotbar();
        if (rev < 0) return;
        int count = PocketHoverState.count(rev);
        if (count <= 0) return; // 0 pockets → nothing above the slot to place

        int rowY = Pockets.pocketRowY();
        for (Slot slot : menu.slots) {
            if (!(slot instanceof MenuKitSlot mk)) continue;
            // Match this 1-slot graft to one of the revealed (rev, depth) pockets
            // by its group id, then place it at depth's spot in the centered row.
            for (int depth = 0; depth < count; depth++) {
                if (Pockets.groupId(rev, depth).equals(mk.getGroupId())) {
                    mk.setGraftPosition(Pockets.pocketRowX(rev, count, depth), rowY);
                    break;
                }
            }
        }
    }
}
