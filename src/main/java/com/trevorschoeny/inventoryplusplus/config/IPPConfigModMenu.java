package com.trevorschoeny.inventoryplusplus.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/**
 * ModMenu integration — surfaces IPP's config screen from the mods list.
 */
public final class IPPConfigModMenu implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return IPPConfigScreen::create;
    }
}
