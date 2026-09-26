package com.trevorschoeny.inventorymax.pocket;

import com.trevorschoeny.inventoryplus.api.InventoryPlusApi;
import com.trevorschoeny.inventoryplus.api.CyclerDirection;
import com.trevorschoeny.inventorymax.config.IMConfig;
import com.trevorschoeny.inventorymax.config.IMKeybinds;
import com.trevorschoeny.inventorymax.operations.InventoryMaxOperations;
import com.trevorschoeny.inventorymax.operations.ServedOperation;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

/**
 * Pocket keybind input.
 *
 * <ul>
 *   <li><b>In-world</b> (no screen) — the cycle keybinds rotate the selected
 *       hotbar slot's pocket cycle. Polled via {@code consumeClick()} in a
 *       client tick (keybinds only fire when no screen is open).</li>
 *   <li><b>In the inventory screen</b> — the cycle keybinds rotate the hovered
 *       pocket column. Handled via {@code ScreenKeyboardEvents} (key presses
 *       go to the screen, not the keybind poll, while a screen is open).</li>
 * </ul>
 *
 * <p>There's no attach keybind — pockets reveal on hover and attach/resize via
 * the on-screen +/− buttons.</li>
 *
 * <p>Rotation is server-authoritative: this sends a {@link PocketRotateC2S}
 * and kicks the local HUD animation; the server does the move and syncs back.
 */
public final class PocketInput {

    private PocketInput() {}

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(PocketInput::onClientTick);

        // In-screen handling for the inventory screen.
        ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
            if (!(screen instanceof InventoryScreen)) return;
            ScreenKeyboardEvents.afterKeyPress(screen).register((innerScreen, event) -> {
                if (!IMConfig.pocketCyclerEnabled()) return;
                if (IMKeybinds.CYCLE_FORWARD.matches(event)) {
                    cycle(PocketHoverState.revealedHotbar(), true);
                } else if (IMKeybinds.CYCLE_BACKWARD.matches(event)) {
                    cycle(PocketHoverState.revealedHotbar(), false);
                }
            });
        });
    }

    private static void onClientTick(Minecraft mc) {
        boolean inWorld = mc.gui.screen() == null && mc.player != null;
        int selected = (mc.player != null) ? mc.player.getInventory().getSelectedSlot() : -1;

        boolean fwd = false, back = false;
        while (IMKeybinds.CYCLE_FORWARD.consumeClick()) fwd = true;
        while (IMKeybinds.CYCLE_BACKWARD.consumeClick()) back = true;
        if (!inWorld) return;
        if (fwd) cycle(selected, true);
        if (back) cycle(selected, false);
    }

    /** Send a rotation request for {@code hotbar} and kick the HUD animation. */
    private static void cycle(int hotbar, boolean forward) {
        if (!IMConfig.pocketCyclerEnabled()) return;
        if (hotbar < 0 || hotbar >= Pockets.HOTBAR_SLOTS) return;
        int count = PocketState.count(hotbar);
        if (count < 1) return; // no cycle (need ≥1 pocket + hotbar)
        // Asked before sending: Inventory Plus's locks live on this client, and a
        // dedicated server has no Inventory Plus to ask. The server asks again.
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (!PocketRing.rotationAllowed(mc.player.inventoryMenu, mc.player, hotbar, count,
                InventoryMaxOperations.POCKET_CYCLE, InventoryMaxOperations.POCKET_CYCLE)) return;
        // Tell Auto-Restock this is a deliberate hand-item change — otherwise a
        // wrap to an empty pocket reads as "held item ran out" and it switches
        // the selected slot to a backup stack.
        InventoryPlusApi.suppressRestockFor(hotbar);
        ClientPlayNetworking.send(new PocketRotateC2S(hotbar, count, forward, ServedOperation.POCKET_CYCLE));
        // Predict the rotation locally so the HUD animation is drawn against the
        // post-rotation arrangement (the rotation is server-authoritative and
        // arrives a round-trip later — without this the animation desyncs).
        PocketCyclerHudSource.predictRotation(hotbar, forward);
        InventoryPlusApi.playCycleAnimation(PocketCyclerHudSource.INSTANCE,
                hotbar, forward ? CyclerDirection.FORWARD : CyclerDirection.BACKWARD);
    }
}
