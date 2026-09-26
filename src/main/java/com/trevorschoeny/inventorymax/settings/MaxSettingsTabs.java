package com.trevorschoeny.inventorymax.settings;

import com.trevorschoeny.inventorymax.config.IMKeybinds;

import com.trevlar.menukit.core.Button;
import com.trevlar.menukit.core.Checkbox;
import com.trevlar.menukit.core.Divider;
import com.trevlar.menukit.core.Flow;
import com.trevlar.menukit.core.PanelElement;
import com.trevlar.menukit.core.Tabs;
import com.trevlar.menukit.core.TextLabel;
import com.trevlar.menukit.core.Toggle;
import com.trevlar.menukit.inject.SlotGroups;

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

    private static List<PanelElement> pockets() {
        Body b = new Body();
        b.heading("On/off").onOff("Use Pockets", true);
        // Pocket participation is heading for reach (deferred.md), so it gets the list now.
        List<Component> groups = new ArrayList<>();
        for (SlotGroups.Entry e : SlotGroups.listing()) groups.add(e.name());
        b.heading("Reach").reach(groups);
        b.heading("Keys")
                .key(IMKeybinds.CYCLE_FORWARD)
                .key(IMKeybinds.CYCLE_BACKWARD);
        b.heading("Options")
                .checkbox("Show the cycle beside the hotbar", true)
                .checkbox("Restock and Auto Tool Switch may take from pockets", true);
        return b.build();
    }

    private static List<PanelElement> equipmentSlots() {
        Body b = new Body();
        b.heading("On/off").onOff("Use Equipment Slots", true);
        b.heading("Options")
                .checkbox("Show elytra and totem icons beside the hotbar", true);
        return b.build();
    }

    private static List<PanelElement> mendAnywhere() {
        Body b = new Body();
        b.heading("On/off").onOff("Use Mend Anywhere", true);
        b.heading("Options")
                .line("Mending items repair from XP anywhere in your inventory, not only in your hands and armor.");
        return b.build();
    }

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

        Body onOff(String label, boolean on) {
            out.add(new Toggle(0, y, 40, 14, on, v -> {}, DISABLED).label(Component.literal(label)));
            y += 18;
            return this;
        }

        Body checkbox(String label, boolean on) {
            out.add(new Checkbox(0, y, on, Component.literal(label), v -> {}, DISABLED));
            y += 14;
            return this;
        }

        Body reach(List<Component> places) {
            List<PanelElement> boxes = new ArrayList<>();
            for (Component place : places) boxes.add(new Checkbox(0, 0, true, place, v -> {}, DISABLED));
            out.add(Flow.of(boxes).gap(10, 4).at(12, y));
            y += 14;
            return this;
        }

        Body key(KeyMapping key) {
            Component text = Component.translatable(key.getName()).copy()
                    .append(": ").append(key.getTranslatedKeyMessage());
            out.add(new TextLabel(0, y + 4, text, TEXT, false));
            int x = Math.max(150, Minecraft.getInstance().font.width(text) + 8);
            out.add(new Button(x, y, 50, 16, Component.literal("Change"), b -> {}, DISABLED));
            y += 20;
            return this;
        }

        List<PanelElement> build() {
            return List.copyOf(out);
        }
    }
}
