package com.petitoner0.extraordinary.mixin.client;

import com.petitoner0.extraordinary.finalritual.chokmah.client.ChokmahRenderBoundary;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.MutableQuadViewImpl;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.AltModelBlockRendererImpl;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AltModelBlockRendererImpl.class)
public abstract class ChokmahIndigoBlockRendererMixin {
	@Shadow
	private BlockAndTintGetter level;

	@Shadow
	private BlockPos pos;

	@Inject(method = "shadeQuad", at = @At("HEAD"), cancellable = true)
	private void extraordinary$lightOuterQuad(MutableQuadViewImpl quad, boolean ambientOcclusion, boolean emissive,
			boolean vanillaShade, CallbackInfo ci) {
		if (level instanceof ChokmahRenderBoundary boundary && boundary.extraordinary$isOuterFace(pos, quad.lightFace())) {
			int light = LightCoordsUtil.FULL_BRIGHT;
			quad.lightmap(light, light, light, light);
			ci.cancel();
		}
	}
}
