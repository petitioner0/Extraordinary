package com.petitoner0.extraordinary.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.petitoner0.extraordinary.finalritual.chokmah.client.ChokmahClient;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.core.SectionPos;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(LevelExtractor.class)
public abstract class ChokmahLevelExtractorMixin {
	@Shadow
	public abstract void allChanged();

	@Unique
	private SectionPos extraordinary$lastMeshAnchor;

	@Unique
	private Vec3 extraordinary$lastCameraPosition;

	@Inject(method = "extract", at = @At("HEAD"))
	private void extraordinary$rebuildMeshesAtBoundaryChange(DeltaTracker deltaTracker, Camera camera, float partialTick, CallbackInfo ci) {
		SectionPos anchor = ChokmahClient.chunkAnchor();
		if (!Objects.equals(anchor, extraordinary$lastMeshAnchor)) {
			extraordinary$lastMeshAnchor = anchor;
			allChanged();
		}
	}

	@ModifyArg(
			method = {"extract", "allChanged"},
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/SectionUpdateTracker;repositionCamera(Lnet/minecraft/core/SectionPos;)V")
	)
	private SectionPos extraordinary$anchorSectionUpdates(SectionPos original) {
		SectionPos anchor = ChokmahClient.chunkAnchor();
		return anchor == null ? original : anchor;
	}

	@ModifyExpressionValue(
			method = "extract",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SectionOcclusionGraph;consumeFrustumUpdate()Z")
	)
	private boolean extraordinary$refreshFrustumDuringFlight(boolean vanillaUpdate, DeltaTracker deltaTracker, Camera camera, float partialTick) {
		if (!ChokmahClient.isControllingCamera()) {
			extraordinary$lastCameraPosition = null;
			return vanillaUpdate;
		}

		Vec3 position = camera.position();
		boolean moved = !position.equals(extraordinary$lastCameraPosition);
		extraordinary$lastCameraPosition = position;
		return vanillaUpdate || moved;
	}
}
