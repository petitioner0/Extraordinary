package com.petitoner0.extraordinary.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import com.petitoner0.extraordinary.finalritual.chokmah.client.ChokmahRenderBoundary;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(FluidRenderer.class)
public abstract class ChokmahFluidRendererMixin {
	@ModifyArgs(
			method = "tesselate",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/FluidRenderer;addFace(Lcom/mojang/blaze3d/vertex/VertexConsumer;FFFFFFFFFFFFFFFFFFFFIIZ)V", ordinal = 2)
	)
	private void extraordinary$lightOuterFluidFace(Args args, @Local(argsOnly = true) BlockAndTintGetter level,
			@Local(argsOnly = true) BlockPos pos, @Local Direction face, @Local(name = "tintColor") int tintColor) {
		if (level instanceof ChokmahRenderBoundary boundary && boundary.extraordinary$isOuterFace(pos, face)) {
			// The last two integer arguments are the shaded color and packed light.
			args.set(21, tintColor);
			args.set(22, LightCoordsUtil.FULL_BRIGHT);
		}
	}
}
