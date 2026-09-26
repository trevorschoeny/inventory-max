package com.trevorschoeny.inventorymax.operations;

import com.trevlar.menukit.window.BehaviorKey;
import com.trevlar.menukit.window.TriBool;

/**
 * The operation a server-side pocket move serves, as it travels in
 * {@code PocketRotateC2S} and {@code PocketQuickMoveC2S}.
 *
 * <h3>Why a pocket move names an operation at all</h3>
 *
 * <p>A pocket rotation and a pocket quick-move happen on the server with no
 * clicks, so MenuKit's click tags never see them. Restock and Auto Tool Switch
 * reach a pocket through those same moves, and what the player sees is a
 * restock or a tool switch, not a rotation. So the packet says which, and the
 * server asks {@code SlotOperations.allows} for that operation on each slot it
 * touches ({@code plans/slot-operations.md}). A fixed set of three, not an
 * arbitrary id: the client can only claim one of the moves Inventory Max
 * actually performs.
 *
 * <h3>Why the restock and tool-switch keys are mirrored here</h3>
 *
 * <p>Those two operations are Inventory Plus's, published in
 * {@code InventoryPlusOperations}. But Inventory Plus is a client-only mod
 * ({@code environment: client}), and this enum runs on the server, where its
 * classes must not be loaded. Inventory Max's common code already keeps every
 * Inventory Plus type off the server path for exactly this reason.
 *
 * <p>So the server builds the keys itself from MenuKit's recipe
 * ({@link InventoryMaxOperations#recipe}). {@link BehaviorKey} is a record, so a
 * key built from the same recipe with the same id is <em>equal</em> to Inventory
 * Plus's, not merely similar: MenuKit resolves and vetoes them as one key.
 * {@link #verifyMirrors} runs at client init, where both mods are loaded, and
 * throws if Inventory Plus ever changes a recipe parameter, so the two cannot
 * drift silently.
 *
 * <p>Wire order is the ordinal: add new operations at the end.
 */
public enum ServedOperation {

    /** The player cycling the pocket, or anything that did not say otherwise. */
    POCKET_CYCLE(InventoryMaxOperations.POCKET_CYCLE, InventoryMaxOperations.POCKET_CYCLE),

    /** A restock reaching a pocket: take on the slots emptied, put on those filled. */
    RESTOCK(InventoryMaxOperations.recipe("inventoryplus", "restock_take"),
            InventoryMaxOperations.recipe("inventoryplus", "restock_put")),

    /** Auto Tool Switch bringing a tool down from a pocket. */
    AUTO_TOOL_SWITCH(InventoryMaxOperations.recipe("inventoryplus", "auto_tool_switch"),
            InventoryMaxOperations.recipe("inventoryplus", "auto_tool_switch"));

    private final BehaviorKey<TriBool> take;
    private final BehaviorKey<TriBool> put;

    ServedOperation(BehaviorKey<TriBool> take, BehaviorKey<TriBool> put) {
        this.take = take;
        this.put = put;
    }

    /** The operation judged on slots this move empties. */
    public BehaviorKey<TriBool> take() { return take; }

    /** The operation judged on slots this move fills. */
    public BehaviorKey<TriBool> put() { return put; }

    public int toWire() { return ordinal(); }

    /** Reads the wire value back; an unknown value is judged as the pocket's own cycle. */
    public static ServedOperation fromWire(int ordinal) {
        ServedOperation[] all = values();
        return ordinal >= 0 && ordinal < all.length ? all[ordinal] : POCKET_CYCLE;
    }

    /**
     * The served operation matching a take/put pair, for a caller that passed
     * MenuKit keys through {@code HotbarCyclable}. A pair Inventory Max does not
     * know is sent as the pocket's own cycle, which is what the move physically
     * is; the client has already asked about the real pair before sending.
     */
    public static ServedOperation servingFor(BehaviorKey<TriBool> take, BehaviorKey<TriBool> put) {
        for (ServedOperation op : values()) {
            if (op.take.equals(take) && op.put.equals(put)) return op;
        }
        return POCKET_CYCLE;
    }

    /**
     * Client init: fails loudly if the server-side mirrors are not equal to
     * Inventory Plus's published keys. Takes them as parameters so this common
     * class never names an Inventory Plus type.
     */
    public static void verifyMirrors(BehaviorKey<TriBool> restockTake, BehaviorKey<TriBool> restockPut,
                                     BehaviorKey<TriBool> autoToolSwitch) {
        check(RESTOCK.take, restockTake);
        check(RESTOCK.put, restockPut);
        check(AUTO_TOOL_SWITCH.take, autoToolSwitch);
    }

    private static void check(BehaviorKey<TriBool> mirror, BehaviorKey<TriBool> published) {
        if (!mirror.equals(published)) {
            throw new IllegalStateException("Inventory Max's server-side copy of " + published.id()
                    + " no longer matches Inventory Plus's published key (" + mirror + " vs " + published
                    + "). Rebuild the mirror in ServedOperation from the same recipe.");
        }
    }
}
