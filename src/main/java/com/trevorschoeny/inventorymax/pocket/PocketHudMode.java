package com.trevorschoeny.inventorymax.pocket;

/**
 * How the Pocket Cycler's HUD is drawn.
 *
 * <p>Inventory Max's own setting, deliberately its own type. It previously
 * reused Inventory Plus's {@code columncycler.hud.PocketHudMode}, which is the
 * Column Cycler's internal enum: Inventory Max was typing one of its own
 * persisted settings with another mod's internal, so renaming a constant over
 * there would have silently changed what this mod reads back from disk.
 *
 * <p>The constant names match the old ones exactly, so existing
 * {@code config/inventorymax/config.json} files keep loading unchanged.
 */
public enum PocketHudMode {
    /** No pocket HUD. */
    NONE,
    /** The mini-hotbar strip beside the held slot. */
    MINI_HOTBAR;

    /** Parses a persisted name, falling back to {@code fallback} when unknown. */
    public static PocketHudMode fromName(String name, PocketHudMode fallback) {
        if (name == null) return fallback;
        for (PocketHudMode m : values()) if (m.name().equals(name)) return m;
        return fallback;
    }
}
