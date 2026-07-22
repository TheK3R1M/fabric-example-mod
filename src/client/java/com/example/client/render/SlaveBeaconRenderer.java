package com.example.client.render;

import com.example.beacon.BeaconNetworkState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.phys.Vec3;

public class SlaveBeaconRenderer implements BlockEntityRenderer<BeaconBlockEntity, SlaveBeaconRenderer.SlaveBeaconRenderState> {

    public static class SlaveBeaconRenderState extends BlockEntityRenderState {
        public boolean isSlave = false;
        public boolean isLinked = false;
        public BlockPos slavePos = null;
        public BlockPos masterPos = null;
    }

    @Override
    public SlaveBeaconRenderState createRenderState() {
        return new SlaveBeaconRenderState();
    }

    @Override
    public void extractRenderState(BeaconBlockEntity blockEntity, SlaveBeaconRenderState state, float partialTick, Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay overlay) {
        if (blockEntity != null && blockEntity.getBlockPos() != null) {
            BlockPos pos = blockEntity.getBlockPos();
            BeaconNetworkState netState = BeaconNetworkState.getOrCreate(pos);

            state.isSlave = (netState.getRole() == BeaconNetworkState.Role.SLAVE);
            state.isLinked = netState.isSlaveLinked();
            state.slavePos = pos;
            state.masterPos = netState.getMasterPos();
        } else {
            state.isSlave = false;
            state.isLinked = false;
            state.slavePos = null;
            state.masterPos = null;
        }
    }

    @Override
    public void submit(SlaveBeaconRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
        if (!state.isSlave || !state.isLinked || state.slavePos == null || state.masterPos == null) {
            return;
        }

        // Render target beam/particle directional vector from Slave to Master
        poseStack.pushPose();
        
        // Offset relative to slave block center
        double dx = state.masterPos.getX() - state.slavePos.getX();
        double dy = state.masterPos.getY() - state.slavePos.getY();
        double dz = state.masterPos.getZ() - state.slavePos.getZ();

        // Beam matrix transformation for slave-to-master direction
        poseStack.translate(0.5 + (dx * 0.001), 0.5 + (dy * 0.001), 0.5 + (dz * 0.001));

        // Rendering logic placeholder for particle stream / directional beam line
        poseStack.popPose();
    }
}
