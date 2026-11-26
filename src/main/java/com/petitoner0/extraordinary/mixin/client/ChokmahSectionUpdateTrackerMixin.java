package com.petitoner0.extraordinary.mixin.client;

import com.petitoner0.extraordinary.finalritual.chokmah.ChokmahArea;
import com.petitoner0.extraordinary.finalritual.chokmah.client.ChokmahClient;
import net.minecraft.client.SectionUpdateTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.SectionPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SectionUpdateTracker.class)
public abstract class ChokmahSectionUpdateTrackerMixin {
	@Inject(method = "doesChunkExistAt", at = @At("HEAD"), cancellable = true)
	private void extraordinary$allowEmptyBoundaryNeighbor(ClientLevel level, long sectionNode, CallbackInfoReturnable<Boolean> cir) {
		SectionPos anchor = ChokmahClient.chunkAnchor();
		if (anchor != null && !ChokmahArea.isInRenderRange(anchor, SectionPos.x(sectionNode), SectionPos.z(sectionNode))) {
			cir.setReturnValue(true);
		}
	}
}
