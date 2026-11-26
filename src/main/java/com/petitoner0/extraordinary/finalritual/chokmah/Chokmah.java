package com.petitoner0.extraordinary.finalritual.chokmah;

import net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerPlayer;

public final class Chokmah {
	private ServerPlayer player;
	private ChokmahArea area;

	public static void register() {
		ServerEntityLevelChangeEvents.AFTER_PLAYER_CHANGE_LEVEL.register((player, from, to) -> stop(player));
		ServerPlayerEvents.AFTER_RESPAWN.register((previous, current, alive) -> {
			ChokmahArea.clear(previous);
			stop(current);
		});
		ServerPlayerEvents.LEAVE.register(ChokmahArea::clear);
	}

	/**
	 * Starts this game for the given player on the server thread.
	 *
	 * @return whether this call started the game and sent the camera takeover packet
	 */
	public boolean start(ServerPlayer player) {
		if (isRunning() || !ServerPlayNetworking.canSend(player, ChokmahCameraPayload.TYPE)
				|| !ServerPlayNetworking.canSend(player, ChokmahCameraStopPayload.TYPE)) {
			return false;
		}

		SectionPos anchor = SectionPos.of(player);
		this.player = player;
		area = ChokmahArea.set(player, anchor);
		ServerPlayNetworking.send(player, new ChokmahCameraPayload(anchor));
		refreshChunkTracking(player);
		return true;
	}

	public static boolean stop(ServerPlayer player) {
		if (!ChokmahArea.clear(player)) {
			return false;
		}

		if (ServerPlayNetworking.canSend(player, ChokmahCameraStopPayload.TYPE)) {
			ServerPlayNetworking.send(player, ChokmahCameraStopPayload.INSTANCE);
		}
		refreshChunkTracking(player);
		return true;
	}

	/** Refreshes the player's chunk tracking on the server thread using their current Chokmah area. */
	public static void refreshChunkTracking(ServerPlayer player) {
		player.level().getChunkSource().move(player);
	}

	public boolean isRunning() {
		return area != null && ChokmahArea.get(player) == area;
	}
}
