package com.petitoner0.extraordinary.finalritual.chokmah.client;

import com.petitoner0.extraordinary.Extraordinary;
import com.petitoner0.extraordinary.finalritual.chokmah.ChokmahArea;
import com.petitoner0.extraordinary.finalritual.chokmah.ChokmahCameraPayload;
import com.petitoner0.extraordinary.finalritual.chokmah.ChokmahCameraStopPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLevelEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.Vec3;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

public final class ChokmahClient implements ClientModInitializer {
	private static final long CAMERA_FLIGHT_DURATION_NANOS = Duration.ofSeconds(8).toNanos();
	private static final double CAMERA_TARGET_X_OFFSET = 124.45;
	private static final double CAMERA_TARGET_Z_OFFSET = -124.45;
	private static final double CAMERA_TARGET_Y = 160.0;
	private static final float CAMERA_TARGET_YAW = 45.0F;
	private static final float CAMERA_TARGET_PITCH = 45.0F;
	private static final double CAMERA_ORBIT_DEGREES_PER_SECOND = 3.0;
	private static final double CAMERA_BREATH_AMPLITUDE = 3.0;
	private static final double CAMERA_BREATH_PERIOD_SECONDS = 6.0;
	private static final AtomicInteger EDGE_LOG_SAMPLES = new AtomicInteger();

	private static volatile SectionPos chunkAnchor;
	private static LocalPlayer cameraPlayer;
	private static CameraFlight cameraFlight;

	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(ChokmahCameraPayload.TYPE, (payload, context) -> {
			releaseCamera();
			cameraPlayer = context.player();
			ChokmahArea.set(cameraPlayer, payload.anchor());
			EDGE_LOG_SAMPLES.set(8);
			chunkAnchor = payload.anchor();
			Extraordinary.LOGGER.info("Chokmah terrain started: anchor={}, radius={}", chunkAnchor, ChokmahArea.RENDER_DISTANCE);
		});
		ClientPlayNetworking.registerGlobalReceiver(ChokmahCameraStopPayload.TYPE, (payload, context) -> releaseCamera());
		ClientLevelEvents.AFTER_CLIENT_LEVEL_CHANGE.register((client, level) -> releaseCamera());
		ClientPlayConnectionEvents.DISCONNECT.register((listener, client) -> releaseCamera());
		ClientTickEvents.START_CLIENT_TICK.register(client -> {
			if (cameraPlayer != null && cameraPlayer != client.player) {
				releaseCamera();
			}
		});

		HudElementRegistry.replaceElement(VanillaHudElements.CROSSHAIR, ChokmahClient::hideWhileControllingCamera);
		HudElementRegistry.replaceElement(VanillaHudElements.HOTBAR, ChokmahClient::hideWhileControllingCamera);
		LevelExtractionEvents.AFTER_BLOCK_OUTLINE_EXTRACTION.register((context, hitResult) -> {
			if (isControllingCamera()) {
				context.levelState().blockOutlineRenderState = null;
			}
		});
		LevelExtractionEvents.END_EXTRACTION.register(context -> {
			if (isControllingCamera()) {
				var state = context.levelState();
				var fog = state.cameraRenderState.fogData;
				state.skyRenderState.skybox = DimensionType.Skybox.NONE;
				fog.color.set(1.0F, 1.0F, 1.0F, 1.0F);
				// Terrain combines environmental fog (including the Nether) with distance fog.
				fog.environmentalStart = Float.MAX_VALUE;
				fog.environmentalEnd = Float.MAX_VALUE;
				fog.renderDistanceStart = Float.MAX_VALUE;
				fog.renderDistanceEnd = Float.MAX_VALUE;
			}
		});
	}

	private static HudElement hideWhileControllingCamera(HudElement original) {
		return (graphics, deltaTracker) -> {
			if (!isControllingCamera()) {
				original.extractRenderState(graphics, deltaTracker);
			}
		};
	}

	public static boolean isControllingCamera() {
		return chunkAnchor != null;
	}

	public static SectionPos chunkAnchor() {
		return chunkAnchor;
	}

	public static boolean hasEdgeLogSamples() {
		return EDGE_LOG_SAMPLES.get() > 0;
	}

	public static void logEdgeNeighbor(BlockPos pos, BlockState state) {
		if (!state.isAir()
				&& EDGE_LOG_SAMPLES.getAndUpdate(remaining -> Math.max(0, remaining - 1)) > 0) {
			Extraordinary.LOGGER.info("Chokmah terrain edge: hiding neighbor {} at {} in the mesh snapshot", state, pos);
		}
	}

	public static CameraPose cameraPose() {
		return cameraFlight == null ? null : cameraFlight.currentPose();
	}

	public static void beginCameraFlight(Camera camera) {
		Vec3 startPosition = camera.position();
		CameraPose start = new CameraPose(startPosition, camera.yRot(), camera.xRot(), 0.0F);
		Vec3 targetPosition = new Vec3(
				startPosition.x() + CAMERA_TARGET_X_OFFSET,
				CAMERA_TARGET_Y,
				startPosition.z() + CAMERA_TARGET_Z_OFFSET);
		CameraPose target = new CameraPose(targetPosition, CAMERA_TARGET_YAW, CAMERA_TARGET_PITCH, 1.0F);
		cameraFlight = new CameraFlight(start, target, System.nanoTime());
	}

	private static void releaseCamera() {
		chunkAnchor = null;
		cameraFlight = null;
		EDGE_LOG_SAMPLES.set(0);
		if (cameraPlayer != null) {
			ChokmahArea.clear(cameraPlayer);
			cameraPlayer = null;
		}
	}

	public record CameraPose(Vec3 position, float yaw, float pitch, float flightProgress) {
	}

	private record CameraFlight(CameraPose start, CameraPose target, long startedAtNanos) {
		private CameraPose currentPose() {
			long elapsedNanos = System.nanoTime() - startedAtNanos;
			if (elapsedNanos >= CAMERA_FLIGHT_DURATION_NANOS) {
				double orbitSeconds = (elapsedNanos - CAMERA_FLIGHT_DURATION_NANOS) / 1_000_000_000.0;
				return orbitPose(orbitSeconds);
			}

			double elapsedFraction = elapsedNanos / (double) CAMERA_FLIGHT_DURATION_NANOS;
			double progress = elapsedFraction * elapsedFraction;
			return new CameraPose(
					start.position().lerp(target.position(), progress),
					Mth.rotLerp((float) progress, start.yaw(), target.yaw()),
					Mth.lerp((float) progress, start.pitch(), target.pitch()),
					(float) progress);
		}

		private CameraPose orbitPose(double elapsedSeconds) {
			double yawOffset = elapsedSeconds * CAMERA_ORBIT_DEGREES_PER_SECOND % 360.0;
			double angle = Math.toRadians(yawOffset);
			double cos = Math.cos(angle);
			double sin = Math.sin(angle);
			double breathPhase = Math.TAU * (elapsedSeconds % CAMERA_BREATH_PERIOD_SECONDS) / CAMERA_BREATH_PERIOD_SECONDS;
			double heightOffset = Math.sin(breathPhase) * CAMERA_BREATH_AMPLITUDE;

			Vec3 center = start.position();
			double offsetX = target.position().x() - center.x();
			double offsetZ = target.position().z() - center.z();
			Vec3 position = new Vec3(
					center.x() + offsetX * cos - offsetZ * sin,
					target.position().y() + heightOffset,
					center.z() + offsetX * sin + offsetZ * cos);
			return new CameraPose(position, target.yaw() + (float) yawOffset, target.pitch(), target.flightProgress());
		}
	}
}
