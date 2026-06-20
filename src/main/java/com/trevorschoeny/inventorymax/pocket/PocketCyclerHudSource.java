package com.trevorschoeny.inventorymax.pocket;

import com.trevorschoeny.inventoryplus.columncycler.hud.HudMode;
import com.trevorschoeny.inventoryplus.cyclable.CycleHudRegistry;
import com.trevorschoeny.inventoryplus.cyclable.CycleHudSource;
import com.trevorschoeny.inventoryplus.cyclable.CycleView;
import com.trevorschoeny.inventorymax.config.IMConfig;
import com.trevorschoeny.menukit.core.Storage;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Pocket Cycler's contribution to the shared cycle HUD. Registered into IP's
 * {@link CycleHudRegistry} so the one Mini-hotbar HUD draws the pocket cycle
 * when the player is on a pocket-bearing hotbar slot — the HUD generalization
 * paying off (the same HUD serves Column and Pocket cyclers).
 *
 * <h3>Reading content client-side</h3>
 *
 * The pocket items are read from the player's pocket attachment on the client.
 * Even in-world (where the client's pocket <i>slots</i> are inert/hidden), the
 * server's pocket slots are always active, so vanilla slot sync keeps the
 * client-side attachment populated — so reading the attachment directly gives
 * the live content regardless of reveal state.
 *
 * <h3>Animation sync — client-side rotation prediction</h3>
 *
 * Pocket rotation is <b>server-authoritative</b>: {@link PocketInput} sends a
 * request and the rotated contents arrive a round-trip later. The shared HUD
 * animation, however, fires the instant you press the key. Without prediction
 * the animation would slide the OLD arrangement and the data would flip
 * mid-slide — a visible jump. (Column Cycler doesn't suffer this: its rotation
 * applies locally at once, so its view is already up to date when the animation
 * fires.) We mirror that here: {@link #predictRotation} stashes the
 * post-rotation view immediately, and {@link #cycleViewForHotbar} serves it
 * until the authoritative sync catches up — so the animation is always drawn
 * against the arrangement it's animating toward.
 */
public final class PocketCyclerHudSource implements CycleHudSource {

    public static final PocketCyclerHudSource INSTANCE = new PocketCyclerHudSource();

    private PocketCyclerHudSource() {}

    // ── Rotation prediction (client-side, presentation only) ──────────────
    /** How long the predicted view is served — covers the rotation's round-trip. */
    private static final long PREDICTION_WINDOW_MILLIS = 500L;
    private static int predictedHotbar = -1;
    private static CycleView predictedView = null;
    private static long predictedExpiryMillis = 0L;

    /** Register this source with the shared HUD registry. Call at client init. */
    public static void register() {
        CycleHudRegistry.register(INSTANCE);
    }

    /**
     * Stash the predicted post-rotation view for {@code hotbar} so the HUD
     * animation is drawn against the arrangement it's sliding toward, not the
     * stale pre-rotation one. Call the instant a rotation is requested (before
     * the server confirms). The prediction applies the same ring rotation
     * {@link PocketServerOps#rotate} does server-side, so it matches what syncs
     * back; it's served for a short window, then the live view takes over.
     */
    public static void predictRotation(int hotbar, boolean forward) {
        CycleView current = computeRealView(hotbar);
        if (current == null) return;
        List<ItemStack> old = current.visualOrder();
        int m = old.size();
        List<ItemStack> next = new ArrayList<>(m);
        for (int i = 0; i < m; i++) {
            int src = forward ? ((i - 1 + m) % m) : ((i + 1) % m);
            next.add(old.get(src));
        }
        predictedHotbar = hotbar;
        predictedView = CycleView.fromVisualOrder(next);
        predictedExpiryMillis = System.currentTimeMillis() + PREDICTION_WINDOW_MILLIS;
    }

    @Override
    public CycleView cycleViewForHotbar(int hotbarSlot) {
        if (!IMConfig.pocketCyclerEnabled()) return null;
        if (IMConfig.pocketHudMode() != HudMode.MINI_HOTBAR) return null;

        // Serve the predicted view while it's live, so the animation stays in
        // sync with the drawn arrangement until the server's rotation syncs back.
        if (predictedView != null && predictedHotbar == hotbarSlot
                && System.currentTimeMillis() < predictedExpiryMillis) {
            return predictedView;
        }
        return computeRealView(hotbarSlot);
    }

    /** The live (server-synced) pocket view for a hotbar slot, or null if none. */
    private static CycleView computeRealView(int hotbarSlot) {
        int count = PocketState.count(hotbarSlot);
        if (count < 1) return null; // need ≥1 pocket + hotbar = 2 ring members

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return null;

        Storage pockets = Pockets.POCKETS.bind(mc.player);
        // Visual top→bottom: pocket(count-1) … pocket0, then the hotbar item.
        List<ItemStack> visual = new ArrayList<>(count + 1);
        for (int depth = count - 1; depth >= 0; depth--) {
            visual.add(pockets.getStack(Pockets.flatIndex(hotbarSlot, depth)));
        }
        visual.add(mc.player.getInventory().getItem(hotbarSlot));
        return CycleView.fromVisualOrder(visual);
    }

    /**
     * Pocket Cycler pins to the BOTTOM of the stacked HUD when it shares a
     * hotbar slot with another cycler (Trev 2026-06-04) — the lowest stack
     * order takes the bottom (nearest-the-hotbar) strip.
     */
    @Override
    public int hudStackOrder() {
        return 0;
    }
}
