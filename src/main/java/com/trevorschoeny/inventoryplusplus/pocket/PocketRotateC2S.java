package com.trevorschoeny.inventoryplusplus.pocket;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Client→server request to rotate a hotbar slot's pocket cycle.
 *
 * <h3>Why server-authoritative</h3>
 *
 * Pocket cycling happens in-world (no screen) as well as in the inventory. In
 * world, the client's pocket slots are inert (hidden) — they reject clicks and
 * report empty — so a client-side click-rotation would desync. The server's
 * pocket slots are always active (MenuKit's side-aware reveal), so the server
 * can rotate the real content and {@code broadcastChanges} syncs it back. The
 * client just sends this intent; {@link PocketServerOps#rotate} does the work.
 *
 * <p>{@code count} is the client's current per-world pocket count for that
 * hotbar slot (the server doesn't track it). Trusting it is harmless — it only
 * bounds which pocket slots participate in the rotation.
 */
public record PocketRotateC2S(int hotbar, int count, boolean forward)
        implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PocketRotateC2S> TYPE =
            new CustomPacketPayload.Type<>(
                    Identifier.fromNamespaceAndPath(Pockets.MOD_ID, "pocket_rotate"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PocketRotateC2S> CODEC =
            StreamCodec.of(
                    (buf, p) -> {
                        buf.writeVarInt(p.hotbar);
                        buf.writeVarInt(p.count);
                        buf.writeBoolean(p.forward);
                    },
                    buf -> new PocketRotateC2S(buf.readVarInt(), buf.readVarInt(), buf.readBoolean()));

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
