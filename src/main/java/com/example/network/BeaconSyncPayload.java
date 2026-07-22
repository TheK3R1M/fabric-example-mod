package com.example.network;

import com.example.ExampleMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public class BeaconSyncPayload {

    // C2S Payload: Client selects effects and submits payment tier
    public record BeaconSelectionPayload(BlockPos pos, String primaryEffect, String secondaryEffect, int requestedLevel) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<BeaconSelectionPayload> TYPE = new CustomPacketPayload.Type<>(ExampleMod.id("beacon_selection"));

        public static final StreamCodec<FriendlyByteBuf, BeaconSelectionPayload> CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeBlockPos(payload.pos());
                buf.writeUtf(payload.primaryEffect());
                buf.writeUtf(payload.secondaryEffect());
                buf.writeInt(payload.requestedLevel());
            },
            buf -> new BeaconSelectionPayload(
                buf.readBlockPos(),
                buf.readUtf(),
                buf.readUtf(),
                buf.readInt()
            )
        );

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    // S2C Payload: Server broadcasts Master tier, power multiplier, and active Slave links to clients
    public record BeaconStateSyncPayload(BlockPos masterPos, int tier, double powerWeight, List<BlockPos> slavePositions) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<BeaconStateSyncPayload> TYPE = new CustomPacketPayload.Type<>(ExampleMod.id("beacon_state_sync"));

        public static final StreamCodec<FriendlyByteBuf, BeaconStateSyncPayload> CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeBlockPos(payload.masterPos());
                buf.writeInt(payload.tier());
                buf.writeDouble(payload.powerWeight());
                buf.writeInt(payload.slavePositions().size());
                for (BlockPos p : payload.slavePositions()) {
                    buf.writeBlockPos(p);
                }
            },
            buf -> {
                BlockPos mPos = buf.readBlockPos();
                int t = buf.readInt();
                double pw = buf.readDouble();
                int count = buf.readInt();
                List<BlockPos> slaves = new ArrayList<>();
                for (int i = 0; i < count; i++) {
                    slaves.add(buf.readBlockPos());
                }
                return new BeaconStateSyncPayload(mPos, t, pw, slaves);
            }
        );

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
