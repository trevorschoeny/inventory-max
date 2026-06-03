package com.trevorschoeny.inventoryplusplus.mixin.client;

import com.trevorschoeny.inventoryplusplus.config.IPPConfig;
import com.trevorschoeny.inventoryplusplus.pocket.PocketButtons;
import com.trevorschoeny.inventoryplusplus.pocket.PocketHoverState;
import com.trevorschoeny.inventoryplusplus.pocket.Pockets;
import com.trevorschoeny.inventoryplusplus.pocket.PocketState;
import com.trevorschoeny.menukit.mixin.AbstractContainerScreenAccessor;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.MouseButtonEvent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Routes clicks on the pocket panel's +/− resize buttons. Injects at the HEAD
 * of {@code AbstractContainerScreen.mouseClicked}; if a click lands on a
 * revealed pocket's +/− button it performs the resize and consumes the click
 * (so it doesn't also register as a slot/inventory click).
 */
@Mixin(AbstractContainerScreen.class)
public abstract class PocketClickMixin {

    @Inject(method = "mouseClicked(Lnet/minecraft/client/input/MouseButtonEvent;Z)Z",
            at = @At("HEAD"), cancellable = true)
    private void inventoryplusplus$pocketButtonClick(MouseButtonEvent event, boolean doubleClick,
                                                     CallbackInfoReturnable<Boolean> cir) {
        if (!((Object) this instanceof InventoryScreen)) return;
        if (!IPPConfig.pocketCyclerEnabled()) return;
        int rev = PocketHoverState.revealedHotbar();
        if (rev < 0) return; // a column must be revealed (the +/- panel is showing)
        int c = PocketState.count(rev);

        double mouseX = event.x();
        double mouseY = event.y();
        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;
        AbstractContainerScreenAccessor acc = (AbstractContainerScreenAccessor) screen;
        int leftPos = acc.menuKit$getLeftPos();
        int topPos = acc.menuKit$getTopPos();

        // Buttons are always present (grayed at their limit). Consume the
        // click on a button rect regardless; act only when the limit allows.
        if (PocketButtons.inRect(mouseX, mouseY, PocketButtons.plusRect(leftPos, topPos, rev))) {
            if (c < Pockets.MAX_PER_SLOT) PocketState.grow(rev); // grayed at max → no-op
            cir.setReturnValue(true);
        } else if (PocketButtons.inRect(mouseX, mouseY, PocketButtons.minusRect(leftPos, topPos, rev))) {
            if (c > 0) PocketState.shrink(rev); // grayed at 0 → no-op
            cir.setReturnValue(true);
        }
    }
}
