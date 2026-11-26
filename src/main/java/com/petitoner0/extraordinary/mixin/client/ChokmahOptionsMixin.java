package com.petitoner0.extraordinary.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.petitoner0.extraordinary.finalritual.chokmah.ChokmahArea;
import com.petitoner0.extraordinary.finalritual.chokmah.client.ChokmahClient;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Options.class)
public abstract class ChokmahOptionsMixin {
	@ModifyReturnValue(method = "getEffectiveRenderDistance", at = @At("RETURN"))
	private int extraordinary$useChokmahRenderDistance(int original) {
		return ChokmahClient.isControllingCamera() ? ChokmahArea.RENDER_DISTANCE : original;
	}
}
