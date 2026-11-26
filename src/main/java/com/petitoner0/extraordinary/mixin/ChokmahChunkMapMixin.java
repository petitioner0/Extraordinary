package com.petitoner0.extraordinary.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.petitoner0.extraordinary.finalritual.chokmah.ChokmahArea;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.entity.EntityAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkMap.class)
public abstract class ChokmahChunkMapMixin {
	@Shadow
	private int serverViewDistance;

	@WrapOperation(
			method = {"move", "updatePlayerPos"},
			at = @At(value = "INVOKE", target = "Lnet/minecraft/core/SectionPos;of(Lnet/minecraft/world/level/entity/EntityAccess;)Lnet/minecraft/core/SectionPos;")
	)
	private SectionPos extraordinary$anchorPlayerChunkTickets(EntityAccess entity, Operation<SectionPos> original) {
		ChokmahArea area = ChokmahArea.get((ServerPlayer) entity);
		return area == null ? original.call(entity) : area.anchor();
	}

	@WrapOperation(
			method = "updateChunkTracking",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;chunkPosition()Lnet/minecraft/world/level/ChunkPos;")
	)
	private ChunkPos extraordinary$anchorPlayerChunkTracking(ServerPlayer player, Operation<ChunkPos> original) {
		ChokmahArea area = ChokmahArea.get(player);
		return area == null ? original.call(player) : area.anchor().chunk();
	}

	@Inject(method = "getPlayerViewDistance", at = @At("HEAD"), cancellable = true)
	private void extraordinary$limitPlayerViewDistance(ServerPlayer player, CallbackInfoReturnable<Integer> cir) {
		if (ChokmahArea.get(player) != null) {
			cir.setReturnValue(Math.min(ChokmahArea.RENDER_DISTANCE, serverViewDistance));
		}
	}
}
