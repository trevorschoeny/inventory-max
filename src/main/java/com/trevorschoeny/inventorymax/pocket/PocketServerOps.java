package com.trevorschoeny.inventorymax.pocket;

import com.trevorschoeny.menukit.core.Storage;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Server-side pocket mutations — the authoritative side of cycling and
 * shrink-eviction. Operates directly on the player's pocket attachment +
 * inventory, then broadcasts so the client (whose pocket slots may be inert)
 * picks up the result via vanilla slot sync.
 */
public final class PocketServerOps {

    private PocketServerOps() {}

    /**
     * Rotate the ring [hotbar slot, pocket 0 … pocket count-1] by one step.
     * FORWARD brings pocket 0 into the hand and wraps the hand's item to the
     * topmost pocket; BACKWARD is the mirror.
     */
    public static void rotate(ServerPlayer sp, int hotbar, int count, boolean forward) {
        if (sp == null || hotbar < 0 || hotbar >= Pockets.HOTBAR_SLOTS) return;
        count = Math.max(0, Math.min(Pockets.MAX_PER_SLOT, count));
        if (count < 1) return; // need ≥2 ring members (1 pocket + hotbar)

        Storage pockets = Pockets.POCKETS.bind(sp);
        int m = count + 1;
        ItemStack[] items = new ItemStack[m];
        // Visual top→bottom: pocket(count-1) … pocket0, then the hotbar item.
        for (int i = 0; i < count; i++) {
            int depth = count - 1 - i;
            items[i] = pockets.getStack(Pockets.flatIndex(hotbar, depth));
        }
        items[count] = sp.getInventory().getItem(hotbar);

        ItemStack[] out = new ItemStack[m];
        for (int i = 0; i < m; i++) {
            out[i] = forward ? items[(i - 1 + m) % m] : items[(i + 1) % m];
        }

        for (int i = 0; i < count; i++) {
            int depth = count - 1 - i;
            pockets.setStack(Pockets.flatIndex(hotbar, depth), out[i]);
        }
        sp.getInventory().setItem(hotbar, out[count]);
        pockets.markDirty();
        sp.inventoryMenu.broadcastChanges();
    }

    /**
     * Empty pocket depths {@code [from, to)} of {@code hotbar} into the
     * player's inventory; drop anything that doesn't fit. Used on shrink.
     */
    public static void evict(ServerPlayer sp, int hotbar, int from, int to) {
        if (sp == null || hotbar < 0 || hotbar >= Pockets.HOTBAR_SLOTS) return;
        from = Math.max(0, from);
        to = Math.min(Pockets.MAX_PER_SLOT, to);

        Storage pockets = Pockets.POCKETS.bind(sp);
        for (int depth = from; depth < to; depth++) {
            int idx = Pockets.flatIndex(hotbar, depth);
            ItemStack stack = pockets.getStack(idx);
            if (stack.isEmpty()) continue;
            ItemStack moving = stack.copy();
            pockets.setStack(idx, ItemStack.EMPTY);
            // Vanilla add-to-inventory (mutates `moving` to the remainder).
            sp.getInventory().add(moving);
            if (!moving.isEmpty()) {
                sp.drop(moving, false); // no room → drop the remainder
            }
        }
        pockets.markDirty();
        sp.inventoryMenu.broadcastChanges();
    }
}
