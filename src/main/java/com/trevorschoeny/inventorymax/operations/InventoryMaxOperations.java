package com.trevorschoeny.inventorymax.operations;

import com.trevlar.menukit.api.window.BehaviorKey;
import com.trevlar.menukit.api.window.KindTag;
import com.trevlar.menukit.api.window.SlotOperations;
import com.trevlar.menukit.api.window.SlotOperations.Role;
import com.trevlar.menukit.api.window.Tier;
import com.trevlar.menukit.api.window.TriBool;

import net.minecraft.resources.Identifier;

import org.jetbrains.annotations.Nullable;

/**
 * Inventory Max's own slot operation, Pocket Cycler
 * ({@code plans/slot-operations.md}), and the recipe it is built with.
 *
 * <p>Common code, safe on a dedicated server: it names MenuKit types only. A
 * pocket move can also serve another mod's operation (Inventory Plus's restock
 * or Auto Tool Switch); the packet names it by id and the server looks it up in
 * MenuKit with {@link #served}, which is why no copy of those keys lives here.
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
     * The operation a pocket packet names, as the server knows it: any operation
     * some mod defined, typed as the allow/deny kind every slot operation is.
     * {@code null} for an id nobody defined, or of another type; the caller
     * drops the packet rather than guess (never read as Pocket Cycler).
     */
    @SuppressWarnings("unchecked")
    public static @Nullable BehaviorKey<TriBool> served(Identifier id) {
        BehaviorKey<?> key = SlotOperations.byId(id);
        return key != null && key.valueType() == TriBool.class ? (BehaviorKey<TriBool>) key : null;
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
