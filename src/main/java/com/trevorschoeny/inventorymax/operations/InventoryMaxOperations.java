package com.trevorschoeny.inventorymax.operations;

import com.trevlar.menukit.window.BehaviorKey;
import com.trevlar.menukit.window.KindTag;
import com.trevlar.menukit.window.SlotOperations;
import com.trevlar.menukit.window.SlotOperations.Role;
import com.trevlar.menukit.window.Tier;
import com.trevlar.menukit.window.TriBool;

import net.minecraft.resources.Identifier;

/**
 * Inventory Max's own slot operation, Pocket Cycler
 * ({@code plans/slot-operations.md}), and the recipe it is built with.
 *
 * <p>Common code, safe on a dedicated server: it names MenuKit types only. The
 * restock and Auto Tool Switch operations a pocket move can also serve belong
 * to Inventory Plus; see {@link ServedOperation} for how the server names them.
 */
public final class InventoryMaxOperations {

    private InventoryMaxOperations() {}

    /** Pocket Cycler may rotate items through a slot. Role BOTH. */
    public static final BehaviorKey<TriBool> POCKET_CYCLE = recipe("inventorymax", "pocket_cycle");

    /**
     * Defines Pocket Cycler with its role so MenuKit lists it. Common init, so it
     * is listed from the title screen and known to a dedicated server. Idempotent.
     */
    public static void define() {
        SlotOperations.define(POCKET_CYCLE, Role.BOTH);
    }

    /**
     * MenuKit's recipe for an operation of one's own ({@code docs/recipes.md}): a
     * {@link TriBool} defaulting to allowed, on the server tier, for vanilla and
     * created slots. {@link BehaviorKey} is a record, so the same recipe with the
     * same id yields an EQUAL key, wherever it is built.
     */
    public static BehaviorKey<TriBool> recipe(String namespace, String path) {
        return BehaviorKey.of(Identifier.fromNamespaceAndPath(namespace, path),
                TriBool.class, TriBool.TRUE, Tier.SERVER, KindTag.VANILLA_SLOT, KindTag.CREATED_SLOT);
    }
}
