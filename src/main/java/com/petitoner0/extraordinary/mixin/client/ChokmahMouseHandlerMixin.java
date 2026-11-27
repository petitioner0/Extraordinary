package com.petitoner0.extraordinary.mixin.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.petitoner0.extraordinary.finalritual.chokmah.client.ChokmahClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class ChokmahMouseHandlerMixin {
	@Shadow @Final
	private Minecraft minecraft;

	@Inject(method = "grabMouse", at = @At("HEAD"), cancellable = true)
	private void extraordinary$keepChokmahCursorVisible(CallbackInfo ci) {
		if (ChokmahClient.isSelectingBlock()) {
			ci.cancel();
		}
	}

	@Inject(method = "onButton", at = @At("HEAD"), cancellable = true)
	private void extraordinary$handleChokmahClick(long window, MouseButtonInfo button, int action, CallbackInfo ci) {
		if (action != InputConstants.PRESS || window != minecraft.getWindow().handle()
				|| minecraft.gui.screen() != null || minecraft.gui.overlay() != null
				|| !ChokmahClient.isControllingCamera()) {
			return;
		}

		minecraft.getFramerateLimitTracker().onInputReceived();
		if (button.button() == InputConstants.MOUSE_BUTTON_LEFT) {
			ChokmahClient.selectBlock(minecraft);
		}
		ci.cancel();
	}
}
