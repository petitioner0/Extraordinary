package com.petitoner0.extraordinary.mixin.client;

import com.mojang.blaze3d.ProjectionType;
import com.petitoner0.extraordinary.Extraordinary;
import com.petitoner0.extraordinary.finalritual.chokmah.client.ChokmahClient;
import net.minecraft.client.CameraType;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.GameRenderState;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(GameRenderer.class)
public abstract class ChokmahGameRendererMixin {
	@Unique
	private static final Identifier CHOKMAH_HALO = Extraordinary.id("chokmah_halo");

	@Shadow @Final
	private GameRenderState gameRenderState;

	@Shadow @Final
	private List<Identifier> requestedPostEffects;

	@Inject(method = "update", at = @At("RETURN"))
	private void extraordinary$requestChokmahHalo(CallbackInfo ci) {
		if (ChokmahClient.isControllingCamera()) {
			requestedPostEffects.add(CHOKMAH_HALO);
		}
	}

	@ModifyArg(
			method = "renderLevel",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ProjectionMatrixBuffer;getBuffer(Lorg/joml/Matrix4f;)Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;")
	)
	private Matrix4f extraordinary$captureChokmahPickingProjection(Matrix4f projection) {
		ChokmahClient.captureBlockPickingFrame(gameRenderState.levelRenderState.cameraRenderState, projection);
		return projection;
	}

	@ModifyArg(
			method = "renderLevel",
			at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setProjectionMatrix(Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;Lcom/mojang/blaze3d/ProjectionType;)V"),
			index = 1
	)
	private ProjectionType extraordinary$useChokmahProjectionType(ProjectionType original) {
		if (ChokmahClient.isControllingCamera()
				&& gameRenderState.levelRenderState.cameraRenderState.projectionMatrix.m23() == 0.0F) {
			return ProjectionType.ORTHOGRAPHIC;
		}
		return original;
	}

	@Inject(method = "extractOptions", at = @At("RETURN"))
	private void extraordinary$extractChokmahCameraOptions(CallbackInfo ci) {
		if (ChokmahClient.isControllingCamera()) {
			// Hide the first-person hands after detaching the camera from the player.
			gameRenderState.optionsRenderState.cameraType = CameraType.THIRD_PERSON_BACK;
			gameRenderState.optionsRenderState.bobView = false;
			gameRenderState.optionsRenderState.cloudStatus = CloudStatus.OFF;
		}
	}
}
