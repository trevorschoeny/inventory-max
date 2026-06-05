package com.trevorschoeny.inventoryplusplus.mixin.client;

import com.trevorschoeny.inventoryplusplus.config.IPPConfig;
import com.trevorschoeny.inventoryplusplus.pocket.PocketHover;
import com.trevorschoeny.inventoryplusplus.pocket.PocketPanelRender;
import com.trevorschoeny.inventoryplusplus.pocket.PocketRow;
import com.trevorschoeny.menukit.core.MenuKitGraftRender;
import com.trevorschoeny.menukit.mixin.AbstractContainerScreenAccessor;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Client render mixin for pockets — the counterpart to the graft mixin.
 * {@link InventoryScreen} renders through {@link AbstractRecipeBookScreen}, so
 * this targets that class (same injection MenuKit uses for recipe-book-hosted
 * panels). Only the inventory screen carries pockets; others are skipped.
 *
 * <ul>
 *   <li><b>HEAD</b> — update the hover/reveal state before slots render, so
 *       the grafted slots' inertness reflects the current hover.</li>
 *   <li><b>AFTER renderContents</b> — draw the panel backing, then MenuKit's
 *       grafted slot frames + items, then the +/− buttons on top.</li>
 * </ul>
 */
@Mixin(AbstractRecipeBookScreen.class)
public abstract class PocketRenderMixin {

    @Inject(method = "render", at = @At("HEAD"))
    private void inventoryplusplus$updatePocketHover(GuiGraphics g, int mouseX, int mouseY,
                                                     float partialTick, CallbackInfo ci) {
        if (!((Object) this instanceof InventoryScreen)) return;
        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;
        AbstractContainerScreenAccessor acc = (AbstractContainerScreenAccessor) screen;
        PocketHover.updateHover(acc.menuKit$getLeftPos(), acc.menuKit$getTopPos(), mouseX, mouseY);
    }

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderContents(Lnet/minecraft/client/gui/GuiGraphics;IIF)V",
                    shift = At.Shift.AFTER
            )
    )
    private void inventoryplusplus$renderPockets(GuiGraphics g, int mouseX, int mouseY,
                                                 float partialTick, CallbackInfo ci) {
        if (!((Object) this instanceof InventoryScreen)) return;
        if (!IPPConfig.pocketCyclerEnabled()) return;
        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;
        AbstractContainerScreenAccessor acc = (AbstractContainerScreenAccessor) screen;
        int leftPos = acc.menuKit$getLeftPos();
        int topPos = acc.menuKit$getTopPos();

        // §0047: move the revealed pockets into the centered horizontal row
        // BEFORE the grafted slots draw (and before input hit-tests them, which
        // happens on later click frames). Render + clicks both read graftX/Y.
        PocketRow.reposition(screen.getMenu());

        PocketPanelRender.drawBackground(g, leftPos, topPos);
        MenuKitGraftRender.renderGraftedSlots(screen, g, mouseX, mouseY);
        PocketPanelRender.drawButtons(g, leftPos, topPos, mouseX, mouseY);
    }
}
