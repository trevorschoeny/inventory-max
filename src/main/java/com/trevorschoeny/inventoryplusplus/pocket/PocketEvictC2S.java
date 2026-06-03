package com.trevorschoeny.inventoryplusplus.pocket;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Client→server request to evict the items from a range of a hotbar slot's
 * pockets — depths {@code [fromDepth, toDepth)} — into the player's inventory
 * (or dropped if there's no room).
 *
 * <p>Sent when the player shrinks a pocket panel (the {@code −} button). The
 * count itself is client-per-world state; this payload just tells the server
 * which pocket depths to empty, server-side, using vanilla's
 * inventory-add-or-drop logic ({@link PocketServerOps#evict}).
 */
public record PocketEvictC2S(int hotbar, int fromDepth, int toDepth)
        implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PocketEvictC2S> TYPE =
            new CustomPacketPayload.Type<>(
                    Identifier.fromNamespaceAndPath(Pockets.MOD_ID, "pocket_evict"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PocketEvictC2S> CODEC =
            StreamCodec.of(
                    (buf, p) -> {
                        buf.writeVarInt(p.hotbar);
                        buf.writeVarInt(p.fromDepth);
                        buf.writeVarInt(p.toDepth);
                    },
                    buf -> new PocketEvictC2S(buf.readVarInt(), buf.readVarInt(), buf.readVarInt()));

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
