package com.petitoner0.extraordinary.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.petitoner0.extraordinary.finalritual.chokmah.ChokmahArea;
import com.petitoner0.extraordinary.finalritual.chokmah.client.ChokmahClient;
import com.petitoner0.extraordinary.finalritual.chokmah.client.ChokmahRenderBoundary;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.chunk.RenderSectionRegion;
import net.minecraft.client.renderer.chunk.SectionCopy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderSectionRegion.class)
public abstract class ChokmahRenderSectionRegionMixin implements ChokmahRenderBoundary {
	@Shadow @Final
	private int minSectionX;

	@Shadow @Final
	private int minSectionZ;

	@Unique
	private boolean[] extraordinary$outsideColumns;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void extraordinary$captureRenderBoundary(ClientLevel level, int minX, int minY, int minZ, SectionCopy[] sections, CallbackInfo ci) {
		SectionPos anchor = ChokmahClient.chunkAnchor();
		if (anchor == null) {
			return;
		}

		int size = RenderSectionRegion.SIZE;
		for (int z = 0; z < size; z++) {
			for (int x = 0; x < size; x++) {
				if (!ChokmahArea.isInRenderRange(anchor, minX + x, minZ + z)) {
					if (extraordinary$outsideColumns == null) {
						extraordinary$outsideColumns = new boolean[size * size];
					}
					extraordinary$outsideColumns[z * size + x] = true;
				}
			}
		}
	}

	@WrapOperation(
			method = {"getBlockState", "getFluidState"},
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/SectionCopy;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;")
	)
	private BlockState extraordinary$exposeOuterFaces(SectionCopy section, BlockPos pos, Operation<BlockState> original) {
		if (extraordinary$isOutsideColumn(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()))) {
			if (ChokmahClient.hasEdgeLogSamples()) {
				ChokmahClient.logEdgeNeighbor(pos, original.call(section, pos));
			}
			return Blocks.AIR.defaultBlockState();
		}
		return original.call(section, pos);
	}

	@Override
	public boolean extraordinary$isOuterFace(BlockPos pos, Direction face) {
		if (extraordinary$outsideColumns == null || !face.getAxis().isHorizontal()) {
			return false;
		}

		int chunkX = SectionPos.blockToSectionCoord(pos.getX());
		int chunkZ = SectionPos.blockToSectionCoord(pos.getZ());
		int neighborX = SectionPos.blockToSectionCoord(pos.getX() + face.getStepX());
		int neighborZ = SectionPos.blockToSectionCoord(pos.getZ() + face.getStepZ());
		return (neighborX != chunkX || neighborZ != chunkZ) && extraordinary$isOutsideColumn(neighborX, neighborZ);
	}

	@Unique
	private boolean extraordinary$isOutsideColumn(int chunkX, int chunkZ) {
		return extraordinary$outsideColumns != null
				&& extraordinary$outsideColumns[(chunkZ - minSectionZ) * RenderSectionRegion.SIZE + chunkX - minSectionX];
	}
}
