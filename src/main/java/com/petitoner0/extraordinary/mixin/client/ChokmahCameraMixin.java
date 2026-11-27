package com.petitoner0.extraordinary.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.systems.RenderSystem;
import com.petitoner0.extraordinary.finalritual.chokmah.client.ChokmahClient;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class ChokmahCameraMixin {
	@Unique
	private static final float ORTHOGRAPHIC_REFERENCE_DEPTH = 160.0F;

	@Shadow
	protected abstract void setPosition(Vec3 position);

	@Shadow
	protected abstract void setRotation(float yaw, float pitch);

	@Shadow
	private boolean detached;

	@Shadow
	private Entity entity;

	@Shadow
	private float depthFar;

	@Unique
	private final Matrix4f extraordinary$orthographicProjection = new Matrix4f();

	@Unique
	private float extraordinary$flightProgress;

	@Unique
	private float extraordinary$fovBonus;

	@ModifyExpressionValue(method = "update", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(FF)F"))
	private float extraordinary$extendChokmahCameraDepth(float depth) {
		return ChokmahClient.isControllingCamera() ? Math.max(depth, 512.0F) : depth;
	}

	@ModifyExpressionValue(
			method = "alignWithEntity",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options;getCameraType()Lnet/minecraft/client/CameraType;")
	)
	private CameraType extraordinary$useFirstPersonCamera(CameraType original) {
		return ChokmahClient.isControllingCamera() ? CameraType.FIRST_PERSON : original;
	}

	@Inject(method = "alignWithEntity", at = @At("HEAD"), cancellable = true)
	private void extraordinary$applyChokmahCamera(float partialTick, CallbackInfo ci) {
		ChokmahClient.CameraPose pose = ChokmahClient.cameraPose();
		extraordinary$flightProgress = pose == null ? 0.0F : pose.flightProgress();
		extraordinary$fovBonus = pose == null ? 0.0F : pose.fovBonus();
		if (pose != null) {
			setPosition(pose.position());
			setRotation(pose.yaw(), pose.pitch());
			// Hide the model while it still overlaps the camera's near plane.
			Vec3 renderOffset = entity.getPosition(partialTick).subtract(entity.position());
			detached = !entity.getBoundingBox().move(renderOffset).inflate(Camera.PROJECTION_Z_NEAR).contains(pose.position());
			ci.cancel();
		}
	}

	@ModifyReturnValue(method = "calculateFov", at = @At("RETURN"))
	private float extraordinary$applyChokmahDescentFov(float fov) {
		return extraordinary$fovBonus > 0.0F ? Math.min(fov + extraordinary$fovBonus, 179.0F) : fov;
	}

	@Inject(method = "alignWithEntity", at = @At("RETURN"))
	private void extraordinary$beginChokmahCameraFlight(float partialTick, CallbackInfo ci) {
		if (ChokmahClient.isControllingCamera() && ChokmahClient.cameraPose() == null) {
			ChokmahClient.beginCameraFlight((Camera) (Object) this);
		}
	}

	@ModifyExpressionValue(
			method = "update",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;createProjectionMatrixForCulling()Lorg/joml/Matrix4f;")
	)
	private Matrix4f extraordinary$blendCullingProjection(Matrix4f perspective) {
		return extraordinary$blendProjection(perspective, Camera.PROJECTION_Z_NEAR, depthFar);
	}

	@ModifyExpressionValue(
			method = {"extractRenderState", "getViewRotationProjectionMatrix"},
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/Projection;getMatrix(Lorg/joml/Matrix4f;)Lorg/joml/Matrix4f;")
	)
	private Matrix4f extraordinary$blendWorldProjection(Matrix4f perspective) {
		// The world renderer uses reversed depth.
		return extraordinary$blendProjection(perspective, depthFar, Camera.PROJECTION_Z_NEAR);
	}

	@Unique
	private Matrix4f extraordinary$blendProjection(Matrix4f perspective, float near, float far) {
		float progress = extraordinary$flightProgress;
		if (progress == 0.0F) {
			return perspective;
		}

		float width = 2.0F * ORTHOGRAPHIC_REFERENCE_DEPTH / perspective.m00();
		float height = 2.0F * ORTHOGRAPHIC_REFERENCE_DEPTH / perspective.m11();
		boolean zeroToOne = RenderSystem.getDevice().getDeviceInfo().isZZeroToOne();
		Matrix4f orthographic = extraordinary$orthographicProjection.setOrthoSymmetric(width, height, near, far, zeroToOne);
		if (progress == 1.0F) {
			return perspective.set(orthographic);
		}

		// Match clip-space W at the reference depth so the blend preserves screen scale there.
		float orthographicWeight = progress * ORTHOGRAPHIC_REFERENCE_DEPTH;
		return perspective.lerp(orthographic, orthographicWeight / (1.0F - progress + orthographicWeight));
	}

	@Inject(method = "extractRenderState", at = @At("RETURN"))
	private void extraordinary$extractChokmahCamera(CameraRenderState state, DeltaTracker deltaTracker, CallbackInfo ci) {
		if (ChokmahClient.isControllingCamera()) {
			state.isFirstPerson = false;
		}
	}
}
