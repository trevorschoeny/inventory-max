package com.trevorschoeny.inventorymax.settings;

import com.trevorschoeny.inventorymax.config.IMConfig;
import com.trevorschoeny.inventorymax.config.IMKeybinds;
import com.trevorschoeny.inventorymax.pocket.PocketHudMode;

import com.trevlar.menukit.core.Checkbox;
import com.trevlar.menukit.core.Divider;
import com.trevlar.menukit.core.Dropdown;
import com.trevlar.menukit.core.Flow;
import com.trevlar.menukit.core.PanelElement;
import com.trevlar.menukit.core.Section;
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
import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
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
 * <p>Half wired: a control with an {@code IMConfig} setting behind it reads
 * it every frame and saves on change; the rest (the inventory-button
 * toggles, the reach) are greyed placeholders, as in Inventory Plus's tabs.
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
                .topRow("Use Pockets", IMConfig::pocketCyclerEnabled, IMConfig::setPocketCyclerEnabled,
                        "Show the Pockets button",
                        IMKeybinds.CYCLE_FORWARD, IMKeybinds.CYCLE_BACKWARD);
        b.heading("Options")
                .choice("Beside the hotbar", Arrays.asList(PocketHudMode.values()),
                        m -> m == PocketHudMode.NONE ? "Off" : "Mini hotbar",
                        IMConfig::pocketHudMode, IMConfig::setPocketHudMode,
                        () -> !IMConfig.pocketCyclerEnabled())
                .checkbox("Restock and Auto Tool Switch may take from pockets",
                        IMConfig::pocketsSupplyAutomation, IMConfig::setPocketsSupplyAutomation,
                        () -> !IMConfig.pocketCyclerEnabled());
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
                .topRow("Use Equipment Slots", IMConfig::equipmentSlotsEnabled, IMConfig::setEquipmentSlotsEnabled,
                        "Show the Equipment Slots button");
        b.heading("Options")
                .checkbox("Show elytra and totem icons beside the hotbar",
                        IMConfig::equipmentHudCue, IMConfig::setEquipmentHudCue,
                        () -> !IMConfig.equipmentSlotsEnabled());
        return b.build();
    }

    private static List<PanelElement> mendAnywhere() {
        Body b = new Body()
                .topRow("Use Mend Anywhere", IMConfig::mendInventoryItems, IMConfig::setMendInventoryItems,
                        "Show the Mend Anywhere button");
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

        /**
         * On/off, the button toggle and the keys, in one row that wraps. The
         * switch reads {@code on} every frame and saves through {@code setOn};
         * the button toggle has no setting yet, so it is a greyed placeholder.
         */
        Body topRow(String useLabel, BooleanSupplier on, Consumer<Boolean> setOn,
                    String showButtonLabel, KeyMapping... keys) {
            List<PanelElement> row = new ArrayList<>();
            row.add(Toggle.linked(0, 0, 40, 14, on, setOn, null).label(Component.literal(useLabel)));
            row.add(Checkbox.linked(0, 0, () -> true, Component.literal(showButtonLabel), v -> {}, DISABLED));
            for (KeyMapping key : keys) {
                row.add(new ChordButton(key).label(Component.translatable(key.getName())));
            }
            out.add(Flow.of(row).gap(10, 4).at(0, y));
            y += 20;
            return this;
        }

        /** A setting with a few named values, bound to its config and greyed while {@code unavailable}. */
        <T> Body choice(String label, List<T> values, Function<T, String> name,
                        Supplier<T> get, Consumer<T> set, BooleanSupplier unavailable) {
            out.add(new TextLabel(12, y + 4, Component.literal(label), TEXT, false));
            out.add(Dropdown.<T>builder()
                    .at(12 + Minecraft.getInstance().font.width(label) + 6, y)
                    .triggerSize(110, 16)
                    .items(values)
                    .label(v -> Component.literal(name.apply(v)))
                    .selection(get, set)
                    .disabledWhen(unavailable)
                    .build());
            y += 20;
            return this;
        }

        /** A checkbox bound to its setting, greyed while {@code unavailable}. */
        Body checkbox(String label, BooleanSupplier get, Consumer<Boolean> set, BooleanSupplier unavailable) {
            out.add(Checkbox.linked(0, y, get, Component.literal(label), set, unavailable));
            y += 14;
            return this;
        }

        /**
         * A reach, as MenuKit's collapsible {@link Section}: closed, how many
         * places are on; open, their checkboxes (greyed placeholders until
         * reach is wired). Rows after it are placed as if it were closed.
         */
        Body reachSection(String title, List<Component> places) {
            if (y > 0) y += 8;
            List<PanelElement> boxes = new ArrayList<>();
            for (Component place : places) boxes.add(new Checkbox(0, 0, true, place, v -> {}, DISABLED));
            out.add(Section.builder(Component.literal(title))
                    .at(0, y)
                    .summary(() -> Component.literal("all " + places.size() + " on"))
                    .add(Flow.of(boxes).gap(10, 4).at(12, 0))
                    .build());
            y += Section.HEADER_HEIGHT + 2;
            return this;
        }

        List<PanelElement> build() {
            return List.copyOf(out);
        }
    }
}
