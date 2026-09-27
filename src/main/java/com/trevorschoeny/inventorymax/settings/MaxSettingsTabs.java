package com.trevorschoeny.inventorymax.settings;

import com.trevorschoeny.inventorymax.config.IMConfig;
import com.trevorschoeny.inventorymax.config.IMKeybinds;
import com.trevorschoeny.inventorymax.pocket.PocketHudMode;

import com.trevlar.menukit.core.Button;
import com.trevlar.menukit.core.Checkbox;
import com.trevlar.menukit.core.Divider;
import com.trevlar.menukit.core.Dropdown;
import com.trevlar.menukit.core.Flow;
import com.trevlar.menukit.core.PanelElement;
import com.trevlar.menukit.core.Tabs;
import com.trevlar.menukit.core.TextLabel;
import com.trevlar.menukit.core.Toggle;

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
    //
    // The frame Inventory Plus's tabs have (Trev, 2026-09-27): the title, a
    // description, Reset to Defaults, the on/off checkbox, a line, then one
    // setting per line, all greyed while the feature is off. Every reach
    // lives in Inventory Plus's Reach tab.

    private static List<PanelElement> pockets() {
        return new Body()
                .frame("Pockets", "Adds up to three extra slots behind each hotbar slot, "
                        + "and keys to cycle through them.",
                        IMConfig::pocketCyclerEnabled, IMConfig::setPocketCyclerEnabled)
                .heading("Misc.")
                .placeholderCheckbox("Show the Pockets button")
                .checkbox("Restock and Auto Tool Switch may take from pockets",
                        IMConfig::pocketsSupplyAutomation, IMConfig::setPocketsSupplyAutomation, () -> false)
                .choice("Beside the hotbar", Arrays.asList(PocketHudMode.values()),
                        m -> m == PocketHudMode.NONE ? "Off" : "Mini hotbar",
                        IMConfig::pocketHudMode, IMConfig::setPocketHudMode, () -> false)
                .heading("Cycling")
                .key(IMKeybinds.CYCLE_FORWARD)
                .key(IMKeybinds.CYCLE_BACKWARD)
                .build();
    }

    private static List<PanelElement> equipmentSlots() {
        return new Body()
                .frame("Equipment Slots", "Adds an elytra slot and a totem slot to your inventory.",
                        IMConfig::equipmentSlotsEnabled, IMConfig::setEquipmentSlotsEnabled)
                .heading("Misc.")
                .placeholderCheckbox("Show the Equipment Slots button")
                .checkbox("Show elytra and totem icons beside the hotbar",
                        IMConfig::equipmentHudCue, IMConfig::setEquipmentHudCue, () -> false)
                .build();
    }

    private static List<PanelElement> mendAnywhere() {
        return new Body()
                .frame("Mend Anywhere", "Mending items repair from XP anywhere in your inventory, "
                        + "not only in your hands and armor.",
                        IMConfig::mendInventoryItems, IMConfig::setMendInventoryItems)
                .heading("Misc.")
                .placeholderCheckbox("Show the Mend Anywhere button")
                .build();
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

        /** While the feature is off, every control after the frame greys and disables. */
        private BooleanSupplier featureOn = () -> true;

        /**
         * The frame: the title at twice size; the description; the
         * feature's on/off toggle, reading "On" or "Off", with Reset to Defaults (greyed until
         * reset is built) to its right; then a line.
         */
        Body frame(String title, String description, BooleanSupplier on, Consumer<Boolean> setOn) {
            out.add(new TextLabel(0, y, Component.literal(title), TextLabel.COLOR_DARK, false).scale(2f));
            y += 24;
            out.add(new TextLabel(0, y, Component.literal(description), TEXT, false));
            y += 16;
            out.add(Flow.of(List.of(
                    Toggle.linked(0, 0, 40, 16, on, setOn, null)
                            .label(() -> Component.literal(on.getAsBoolean() ? "On" : "Off")),
                    new Button(0, 0, Minecraft.getInstance().font.width("Reset to Defaults") + 12, 16,
                            Component.literal("Reset to Defaults"), b -> {}, DISABLED)))
                    .gap(10, 4).at(0, y));
            y += 22;
            featureOn = on;
            // ponytail: a long divider; MenuKit caps it to the body's width.
            out.add(Divider.horizontal(0, y, 4000, 0xFF8B8B8B, 1));
            y += 6;
            return this;
        }

        /** {@code unavailable}, and also while the feature is off. */
        private BooleanSupplier gated(BooleanSupplier unavailable) {
            BooleanSupplier on = featureOn;
            return () -> !on.getAsBoolean() || unavailable.getAsBoolean();
        }

        /** Settings text, turning grey while the feature is off. */
        private TextLabel settingText(int x, int y, Component text, int color) {
            BooleanSupplier on = featureOn;
            return new TextLabel(x, y, () -> on.getAsBoolean() ? text : text.copy().withColor(0xFF8B8B8B), color, false);
        }

        /** A working key: Keybindery's button, labelled with the key's name. */
        // Every setting is its text on one line and its control below it, and
        // settings sit in categories, as in Inventory Plus's tabs.

        /** A category: its title and a short line under it; greys while the feature is off. */
        Body heading(String text) {
            y += 8;
            out.add(settingText(0, y, Component.literal(text), TextLabel.COLOR_DARK));
            y += 12;
            out.add(Divider.horizontal(0, y - 2, 160, 0xFF8B8B8B, 1));
            y += 2;
            return this;
        }

        /** A setting's text, on its own line. */
        private void label(Component text) {
            out.add(settingText(0, y, text, TEXT));
            y += 12;
        }

        Body key(KeyMapping key) {
            label(Component.translatable(key.getName()));
            out.add(new ChordButton(key).disabledWhen(gated(() -> false)).at(0, y));
            y += 20;
            return this;
        }

        /** A checkbox with no setting behind it yet: shown checked, greyed. */
        Body placeholderCheckbox(String label) {
            return checkbox(label, () -> true, v -> {}, DISABLED);
        }

        /** A setting with a few named values, bound to its config and greyed while {@code unavailable}. */
        <T> Body choice(String label, List<T> values, Function<T, String> name,
                        Supplier<T> get, Consumer<T> set, BooleanSupplier unavailable) {
            label(Component.literal(label));
            out.add(Dropdown.<T>builder()
                    .at(0, y)
                    .triggerSize(110, 16)
                    .items(values)
                    .label(v -> Component.literal(name.apply(v)))
                    .selection(get, set)
                    .disabledWhen(gated(unavailable))
                    .build());
            y += 20;
            return this;
        }

        /** A checkbox bound to its setting, greyed while {@code unavailable}. */
        Body checkbox(String label, BooleanSupplier get, Consumer<Boolean> set, BooleanSupplier unavailable) {
            label(Component.literal(label));
            out.add(Checkbox.linked(0, y, get, Component.empty(), set, gated(unavailable)));
            y += 16;
            return this;
        }

        List<PanelElement> build() {
            return List.copyOf(out);
        }
    }
}
