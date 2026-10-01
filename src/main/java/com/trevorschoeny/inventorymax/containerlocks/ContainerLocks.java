package com.trevorschoeny.inventorymax.containerlocks;


import com.mojang.serialization.Codec;
import com.trevlar.menukit.containers.api.state.SlotState;
import com.trevlar.menukit.containers.api.state.SlotStateChannel;
import com.trevlar.menukit.api.window.BehaviorKey;
import com.trevlar.menukit.api.window.BehaviorKeys;
import com.trevlar.menukit.api.window.SlotOperations;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;

import java.util.Set;


/**
 * Container Locks — the companion (IM) half of Inventory Plus's Locked Slots:
 * shared, server-persistent locks on placed storage containers.
 *
 * <p>This class is <b>universal and free of any IP reference</b>, so it holds
 * the channel and answers enforcement on both sides — including a dedicated
 * server where IP (client-only) is absent. The client-side UI recognition lives
 * in {@link ContainerLockProvider} (registered on the client only).
 *
 * <h3>What rides MenuKit</h3>
 *
 * A single {@code SHARED} per-slot channel (§0049): one lock value per slot,
 * synced live to every viewer, writable by any of them; on shulkers it also
 * travels the break→place cycle for free (§0048). We register the channel and
 * write nothing else (§0019).
 *
 * <h3>Scope — placed simple storage containers</h3>
 *
 * {@link #handles} matches, by real (server-side) type: single chest + trapped
 * ({@link ChestBlockEntity}), barrel, shulker, hopper, dispenser + dropper
 * ({@link DispenserBlockEntity}), and the {@link CompoundContainer} backing a
 * double chest (§0050 resolves each half to its own placed-chest key). It
 * excludes functional containers (furnace/brewing/…), the player inventory, and
 * the ender chest (IP's client-side lock).
 */
public final class ContainerLocks {

    private ContainerLocks() {}

    /** SHARED boolean lock channel — one value per placed-container slot. */
    public static SlotStateChannel<Boolean> CHANNEL;

    /**
     * The vanilla operations a lock refuses: the {@code menukit:} part of
     * Inventory Plus's Slot lock default ({@code Reach.SLOT_LOCK_DENIES}), so a
     * placed-container lock means what a player-inventory lock means. The
     * server keeps these locks as plain booleans with no group, so it holds the
     * default; Inventory Plus's own operations (sort, restock) are judged on
     * the locking player's client by its own veto. World pickup
     * ({@code INVENTORY_INSERT}) is left out: it only fills the player's own
     * inventory, never a placed container.
     */
    private static final Set<BehaviorKey<?>> LOCK_DENIES = Set.of(
            BehaviorKeys.SHIFT_CLICK_IN, BehaviorKeys.SHIFT_CLICK_OUT, BehaviorKeys.COLLECT,
            BehaviorKeys.DROP, BehaviorKeys.DROP_STACK,
            BehaviorKeys.DRAG_FILL, BehaviorKeys.HOTBAR_SWAP, BehaviorKeys.OFFHAND_SWAP);

    /**
     * Registers the channel and the veto that enforces it. Universal; call once
     * at common init before any container menu opens.
     */
    public static void register() {
        CHANNEL = SlotState.register(
                Identifier.fromNamespaceAndPath("inventoryplus", "container_lock"),
                Codec.BOOL,
                StreamCodec.<RegistryFriendlyByteBuf, Boolean>of(
                        (buf, v) -> buf.writeBoolean(v),
                        buf -> buf.readBoolean()),
                false,
                SlotStateChannel.Visibility.SHARED,
                // A viewer of a placed simple-storage container may lock its
                // slots; the server has already checked the menu, the slot and
                // stillValid (MenuKit 6.0.0, §0067). Without a rule every
                // client write to a SHARED channel reverts.
                (player, slot) -> handles(slot.container()));
        // One veto for every seam MenuKit asks it at (shift-click merge and
        // destination, collect, clicks, swaps, drops), replacing the three
        // lock mixins. On the client the open container is a SimpleContainer,
        // which handles() refuses, so this binds where the real block entity is.
        SlotOperations.veto((ref, operation) -> {
            boolean denied = LOCK_DENIES.contains(operation) && isLocked(ref.container(), ref.containerSlot());
            // ponytail: [reach-probe] (Designer brief 2026-09-30): this store is
            // judged on the server; Inventory Plus judges its own on the client.
            // A line here with none from Inventory Plus is the two disagreeing.
            if (denied) com.trevorschoeny.inventorymax.InventoryMax.LOGGER.info("[reach-probe] IM DENY thread={} op={} containerSlot={} container={}",
                    Thread.currentThread().getName(), operation.id(), ref.containerSlot(),
                    ref.container().getClass().getSimpleName());
            return denied;
        });
    }

    // ── Server-side recognition (real container types) ──────────────────────

    /**
     * True if {@code container} is a placed simple-storage container this feature
     * locks, by its real server-side type. The subclass checks cover trapped
     * chest (a {@link ChestBlockEntity}) and dropper (a {@link DispenserBlockEntity});
     * the {@link CompoundContainer} case is the double chest, which §0050 resolves
     * per slot to the owning half. Used by the server enforcement + automation
     * paths; the client UI uses {@link ContainerLockProvider} instead.
     */
    public static boolean handles(Container container) {
        return container instanceof ChestBlockEntity
                || container instanceof BarrelBlockEntity
                || container instanceof ShulkerBoxBlockEntity
                || container instanceof HopperBlockEntity
                || container instanceof DispenserBlockEntity
                || container instanceof CompoundContainer;
    }

    // ── Lock reads ──────────────────────────────────────────────────────────

    /**
     * Lock check for a slot. Routes through the menu-free read
     * ({@link #isLocked(Container, int)}), which derives the server from the
     * container itself (§0050), so it answers on the server thread too.
     */
    public static boolean isLocked(Slot slot) {
        return isLocked(slot.container, slot.getContainerSlot());
    }

    /**
     * Menu-free read (§0050) — for automation (hopper / dropper / dispenser),
     * which touches a container by index with no Slot and no open menu. Returns
     * the SHARED value; server-side; a double chest resolves to the owning half
     * automatically.
     */
    public static boolean isLocked(Container container, int slotIndex) {
        return handles(container) && CHANNEL.get(container, slotIndex);
    }
}
