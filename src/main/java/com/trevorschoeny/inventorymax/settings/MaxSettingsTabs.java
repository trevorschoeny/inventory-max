package com.trevorschoeny.inventorymax.settings;

import com.trevorschoeny.inventorymax.config.IMKeybinds;

import com.trevlar.menukit.core.Checkbox;
import com.trevlar.menukit.core.Divider;
import com.trevlar.menukit.core.Flow;
import com.trevlar.menukit.core.PanelElement;
import com.trevlar.menukit.core.Tabs;
import com.trevlar.menukit.core.TextLabel;
import com.trevlar.menukit.core.Toggle;
import com.trevlar.menukit.inject.SlotGroups;

import com.trevorschoeny.keybindery.chord.ChordButton;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * Inventory Max's tabs in the Inventory Plus settings menu: Pockets,
 * Equipment Slots and Mend Anywhere. Inventory Plus holds their places with
 * greyed stand-ins under the same ids; adding them here replaces those, in
 * the same places.
 *
 * <p>The two mods share only the menu's name and the three tab ids below;
 * Inventory Max imports nothing of Inventory Plus's for this.
 *
 * <p>Scaffold stage: the bodies are placeholders. Every control shows its
 * {@code IMConfig} default and is disabled; nothing reads or writes config.
 */
public final class MaxSettingsTabs {

    private MaxSettingsTabs() {}

    /** Inventory Plus's settings menu. */
    private static final Identifier MENU = Identifier.fromNamespaceAndPath("inventoryplus", "settings");

    /** Call once from client init, before the menu can be opened. */
    public static void register() {
        add("inventorymax:pockets", "Pockets", MaxSettingsTabs::pockets);
        add("inventorymax:equipment_slots", "Equipment Slots", MaxSettingsTabs::equipmentSlots);
        add("inventorymax:mend_anywhere", "Mend Anywhere", MaxSettingsTabs::mendAnywhere);
    }

    private static void add(String id, String label, Supplier<List<PanelElement>> body) {
        Tabs.addTo(MENU, Tabs.tab(id).label(Component.literal(label)).body(body));
    }

    // ── Bodies ──────────────────────────────────────────────────────────

    // Laid out as Inventory Plus's feature tabs are (Trev, 2026-09-27): one
    // wrapping row of on/off, the button toggle and the keys; then the
    // options; then the reach, closed.

    private static List<PanelElement> pockets() {
        Body b = new Body()
                .topRow("Use Pockets", true, "Show the Pockets button",
                        IMKeybinds.CYCLE_FORWARD, IMKeybinds.CYCLE_BACKWARD);
        b.heading("Options")
                .checkbox("Show the cycle beside the hotbar", true)
                .checkbox("Restock and Auto Tool Switch may take from pockets", true);
        // Pocket participation is heading for reach (deferred.md), so it gets the list now.
        // A group a mod added carries the mod in parentheses, as Inventory Plus's
        // tabs do (SettingsTabs.groupLabel, package-private there). Lock groups
        // join when Inventory Plus offers them through its API.
        List<Component> places = new ArrayList<>();
        for (SlotGroups.Entry e : SlotGroups.listing()) {
            Component source = e.source();
            places.add(source == null ? e.name() : e.name().copy().append(" (").append(source).append(")"));
        }
        places.addAll(IN_INVENTORY);
        b.reachSection("Reach", places);
        return b.build();
    }

    private static List<PanelElement> equipmentSlots() {
        Body b = new Body()
                .topRow("Use Equipment Slots", true, "Show the Equipment Slots button");
        b.heading("Options")
                .checkbox("Show elytra and totem icons beside the hotbar", true);
        return b.build();
    }

    private static List<PanelElement> mendAnywhere() {
        Body b = new Body()
                .topRow("Use Mend Anywhere", true, "Show the Mend Anywhere button");
        b.heading("Options")
                .line("Mending items repair from XP anywhere in your inventory, not only in your hands and armor.");
        return b.build();
    }

    /** Containers the player carries, offered after the slot groups, as Inventory Plus does. */
    private static final List<Component> IN_INVENTORY = List.of(
            Component.literal("Shulker Boxes (in inventory)"),
            Component.literal("Bundles (in inventory)"),
            Component.literal("Ender Chest (in inventory)"));

    // ── Rows ────────────────────────────────────────────────────────────
    //
    // ponytail: Inventory Plus's SettingsBody, trimmed to what these three tabs
    // use; it is internal to Inventory Plus, so it can't be shared. If a third
    // mod adds tabs, the row vocabulary belongs in MenuKit instead.

    private static final class Body {
        private static final BooleanSupplier DISABLED = () -> true;
        private static final int TEXT = 0xFF555555;
        private static final int GREY = 0xFF8B8B8B;

        private final List<PanelElement> out = new ArrayList<>();
        private int y = 0;

        Body heading(String text) {
            if (y > 0) y += 8;
            out.add(new TextLabel(0, y, Component.literal(text), TextLabel.COLOR_DARK, false));
            out.add(Divider.horizontal(0, y + 10, 160, 0xFF8B8B8B, 1));
            y += 14;
            return this;
        }

        Body line(String text) {
            out.add(new TextLabel(0, y, Component.literal(text), TEXT, false));
            y += 12;
            return this;
        }

        /** On/off, the button toggle and the keys, in one row that wraps. */
        Body topRow(String useLabel, boolean on, String showButtonLabel, KeyMapping... keys) {
            List<PanelElement> row = new ArrayList<>();
            row.add(new Toggle(0, 0, 40, 14, on, v -> {}, DISABLED).label(Component.literal(useLabel)));
            row.add(new Checkbox(0, 0, true, Component.literal(showButtonLabel), v -> {}, DISABLED));
            for (KeyMapping key : keys) {
                row.add(new ChordButton(key).label(Component.translatable(key.getName())));
            }
            out.add(Flow.of(row).gap(10, 4).at(0, y));
            y += 20;
            return this;
        }

        Body checkbox(String label, boolean on) {
            out.add(new Checkbox(0, y, on, Component.literal(label), v -> {}, DISABLED));
            y += 14;
            return this;
        }

        /**
         * A reach, drawn closed: an arrow, the title, and how many places are
         * on. MenuKit has no collapsible section yet, so it does not open.
         */
        Body reachSection(String title, List<Component> places) {
            if (y > 0) y += 8;
            String text = "▶ " + title;
            out.add(new TextLabel(0, y, Component.literal(text), TextLabel.COLOR_DARK, false));
            int x = Minecraft.getInstance().font.width(text) + 8;
            out.add(new TextLabel(x, y, Component.literal("all " + places.size() + " on"), GREY, false));
            y += 14;
            return this;
        }

        List<PanelElement> build() {
            return List.copyOf(out);
        }
    }
}
