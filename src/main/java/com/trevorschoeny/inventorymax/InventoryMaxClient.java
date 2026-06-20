package com.trevorschoeny.inventorymax;

import com.trevorschoeny.inventoryplus.lockedslots.LockedSlots;
import com.trevorschoeny.inventorymax.config.IMConfig;
import com.trevorschoeny.inventorymax.config.IMKeybinds;
import com.trevorschoeny.inventorymax.containerlocks.ContainerLockProvider;
import com.trevorschoeny.inventorymax.pocket.PocketCyclerHudSource;
import com.trevorschoeny.inventorymax.pocket.PocketInput;
import com.trevorschoeny.inventorymax.pocket.PocketState;

import net.fabricmc.api.ClientModInitializer;

/**
 * Client entrypoint for Inventory Max. Wires Pocket Cycler's client
 * surface: config, keybinds, per-world count state, input dispatch, and the
 * pocket source for IP's shared cycle HUD.
 *
 * <p>The graft + render + click mixins activate via {@code mixins.json}; the
 * shared HUD panel itself is registered by IP (this just contributes a
 * source into IP's {@code CycleHudRegistry}).
 */
public class InventoryMaxClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        IMConfig.load();
        IMKeybinds.register();
        PocketState.load();
        PocketInput.register();
        // Contribute pockets to IP's shared cycle HUD (the generalization
        // paying off — one HUD, both cyclers).
        PocketCyclerHudSource.register();

        // Plug Container Locks into IP's client-side lock seam, so IP's unified
        // lock-check / edit UI / icon / sort+move-matching skip recognize placed
        // containers. Client-only: IP's LockedSlots is a client-only class.
        LockedSlots.registerProvider(new ContainerLockProvider());

        InventoryMax.LOGGER.info("[inventorymax] Client init — Pocket Cycler + Container Locks active.");
    }
}
