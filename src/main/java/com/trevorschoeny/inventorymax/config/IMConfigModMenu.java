package com.trevorschoeny.inventorymax.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.trevorschoeny.inventoryplus.api.InventoryPlusApi;

/**
 * ModMenu integration: Inventory Max's row opens Inventory Plus's settings
 * menu, where its tabs live (Pockets first).
 */
public final class IMConfigModMenu implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> InventoryPlusApi.settingsScreen(parent, "inventorymax:pockets");
    }
}
