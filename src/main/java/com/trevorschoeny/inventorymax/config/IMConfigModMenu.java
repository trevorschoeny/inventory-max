package com.trevorschoeny.inventorymax.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/**
 * ModMenu integration — surfaces IM's config screen from the mods list.
 */
public final class IMConfigModMenu implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return IMConfigScreen::create;
    }
}
