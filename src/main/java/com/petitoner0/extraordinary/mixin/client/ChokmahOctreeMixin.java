package com.petitoner0.extraordinary.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.petitoner0.extraordinary.finalritual.chokmah.client.ChokmahClient;
import net.minecraft.client.renderer.Octree;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Octree.class)
public abstract class ChokmahOctreeMixin {
	@WrapOperation(
			method = "<init>",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;smallestEncompassingPowerOfTwo(I)I")
	)
	private int extraordinary$fitFullTerrainHeight(int horizontalSections, Operation<Integer> original,
			@Local(argsOnly = true, ordinal = 1) int verticalSections) {
		// The root is a cube and starts at the world's minimum Y when it fits the full height.
		int sections = ChokmahClient.isControllingCamera() ? Math.max(horizontalSections, verticalSections) : horizontalSections;
		return original.call(sections);
	}
}
