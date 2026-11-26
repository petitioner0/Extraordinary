package com.petitoner0.extraordinary.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.petitoner0.extraordinary.finalritual.chokmah.ChokmahArea;
import com.petitoner0.extraordinary.finalritual.chokmah.client.ChokmahClient;
import net.minecraft.client.renderer.SectionOcclusionGraph;
import net.minecraft.core.SectionPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SectionOcclusionGraph.class)
public abstract class ChokmahSectionOcclusionGraphMixin {
	@Inject(method = "isInViewDistance", at = @At("HEAD"), cancellable = true)
	private void extraordinary$includeSquareTerrain(long cameraSection, long targetSection, CallbackInfoReturnable<Boolean> cir) {
		SectionPos anchor = ChokmahClient.chunkAnchor();
		if (anchor != null) {
			cir.setReturnValue(ChokmahArea.isInRenderRange(anchor, SectionPos.x(targetSection), SectionPos.z(targetSection)));
		}
	}

	@ModifyExpressionValue(
			method = "getRelativeFrom",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ViewArea;getViewDistance()I")
	)
	private int extraordinary$includeFullTerrainColumns(int verticalDistance) {
		return ChokmahClient.isControllingCamera() ? Integer.MAX_VALUE : verticalDistance;
	}
}
