package com.petitoner0.extraordinary.mixin.client;

import com.petitoner0.extraordinary.finalritual.chokmah.client.ChokmahClient;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.SectionPos;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LevelRenderer.class)
public abstract class ChokmahLevelRendererMixin {
	@ModifyArg(
			method = {"repositionCamera", "invalidateCompiledGeometry"},
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ViewArea;repositionCamera(Lnet/minecraft/core/SectionPos;)Z")
	)
	private SectionPos extraordinary$anchorRenderSections(SectionPos original) {
		SectionPos anchor = ChokmahClient.chunkAnchor();
		return anchor == null ? original : anchor;
	}

	@ModifyArg(
			method = "render",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SectionOcclusionGraph;update(Lnet/minecraft/client/renderer/state/level/CameraRenderState;ILnet/minecraft/client/renderer/state/level/ChunkLoadingRenderState;)V"),
			index = 0
	)
	private CameraRenderState extraordinary$anchorTerrainTraversal(CameraRenderState camera) {
		SectionPos anchor = ChokmahClient.chunkAnchor();
		if (anchor == null) {
			return camera;
		}

		// Seed terrain traversal from the fixed chunk area and cull using the actual camera frustum.
		CameraRenderState terrainCamera = new CameraRenderState();
		terrainCamera.blockPos = anchor.center();
		terrainCamera.pos = Vec3.atCenterOf(terrainCamera.blockPos);
		terrainCamera.cullFrustum = camera.cullFrustum;
		terrainCamera.isFrustumCaptured = camera.isFrustumCaptured;
		return terrainCamera;
	}
}
