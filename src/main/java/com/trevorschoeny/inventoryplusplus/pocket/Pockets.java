package com.trevorschoeny.inventoryplusplus.pocket;

import com.trevorschoeny.menukit.core.StorageAttachment;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Pocket Cycler constants + the server-persistent content storage.
 *
 * <h3>Structure model (the §0045 fixed-graft + client-gated composition)</h3>
 *
 * The graft kit grafts a <b>fixed</b> set of slots at menu construction; it
 * has no dynamic per-slot structure. So we graft the <b>maximum</b> — 3 pocket
 * slots above every one of the 9 hotbar slots (27 total) — as real,
 * server-synced, persistent slots, and let a <b>client-side per-world count</b>
 * ({@link PocketState}) decide how many of each hotbar slot's 3 pockets are
 * revealed + interactive. Hidden pockets are inert (§0021): invisible and
 * non-interactive, but their content still persists server-side. "Attaching"
 * a pocket is just raising the count from 0; resizing is changing it (1–3).
 *
 * <p>Each (hotbar, depth) pocket is its own single-slot graft with its own
 * panel + reveal predicate — that's what gives per-depth reveal granularity
 * the panel-level reveal API otherwise wouldn't (one panel reveals all-or-
 * nothing).
 */
public final class Pockets {

    private Pockets() {}

    public static final String MOD_ID = "inventoryplusplus";

    /** Hotbar slots (0–8). */
    public static final int HOTBAR_SLOTS = 9;
    /** Max pockets stacked above one hotbar slot. */
    public static final int MAX_PER_SLOT = 3;
    /** Total grafted pocket slots (9 × 3). */
    public static final int TOTAL = HOTBAR_SLOTS * MAX_PER_SLOT;

    // ─── Screen-relative layout (added to leftPos/topPos at render time) ──
    /** Vanilla hotbar slot row y. */
    public static final int HOTBAR_Y = 142;
    /**
     * X of the pocket column (and hotbar-slot hover). 7, not 8: a grafted
     * slot's 18px frame must sit 1px left of the vanilla item x so the pocket
     * frame lines up with the vanilla hotbar slot frame below it (Trev
     * 2026-06-02 — "both panels a px too far right").
     */
    public static final int HOTBAR0_X = 7;
    /** Vanilla slot pitch. */
    public static final int SLOT = 18;
    /** Gap between the hotbar slot and the bottom of the pocket column. */
    public static final int HOTBAR_GAP = 8;
    /** Height of the +/− button row drawn above the topmost pocket. */
    public static final int BUTTON_ROW_H = 9;

    /**
     * Player-attached pocket content — 27 slots, survives logout/restart
     * (§0034). Declared at common init. The graft slices this per (hotbar,
     * depth).
     */
    public static StorageAttachment<Player, NonNullList<ItemStack>> POCKETS;

    /** Common-init registration. Call from the main entrypoint. */
    public static void register() {
        POCKETS = StorageAttachment.playerAttached(MOD_ID, "pockets", TOTAL);
    }

    /** Flat index into the 27-slot backing for (hotbar, depth). */
    public static int flatIndex(int hotbar, int depth) {
        return hotbar * MAX_PER_SLOT + depth;
    }

    /** Unique graft group/panel id for (hotbar, depth). */
    public static String groupId(int hotbar, int depth) {
        return "pocket_" + hotbar + "_" + depth;
    }

    /** Screen-relative x of the pocket column for a hotbar slot. */
    public static int pocketX(int hotbar) {
        return HOTBAR0_X + hotbar * SLOT;
    }

    /**
     * Screen-relative y of a pocket at {@code depth} (0 = closest to the
     * hotbar). Pockets stack upward from just above the hotbar.
     */
    public static int pocketY(int depth) {
        return HOTBAR_Y - HOTBAR_GAP - (depth + 1) * SLOT;
    }
}
