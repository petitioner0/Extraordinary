package com.petitoner0.extraordinary.finalritual.chokmah.client;

import com.petitoner0.extraordinary.finalritual.chokmah.ChokmahArea;
import com.petitoner0.extraordinary.finalritual.chokmah.client.ChokmahClient.CameraPose;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.joml.Matrix4f;
import org.joml.Vector3f;

record ChokmahBlockPickingFrame(CameraPose pose, Matrix4f inverseViewProjection, float farClipDepth) {
	private static final double LANDING_DISTANCE = 3.0;

	Vec3 landingPosition(ClientLevel level, SectionPos anchor, double x, double y) {
		float clipX = (float) (2.0 * x - 1.0);
		float clipY = (float) (1.0 - 2.0 * y);
		// Reversed depth puts the near plane at clip-space Z = 1 on both backends.
		Vec3 from = unproject(clipX, clipY, 1.0F);
		Vec3 to = unproject(clipX, clipY, farClipDepth);
		ClipContext ray = new ClipContext(from, to, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, CollisionContext.empty());
		BlockHitResult hit = BlockGetter.traverseBlocks(from, to, ray,
				(context, pos) -> clipBlock(level, anchor, context, pos),
				context -> null);
		if (hit == null || hit.isInside()) {
			return null;
		}

		Vec3 direction = from.vectorTo(to).normalize();
		Vec3 hitPosition = hit.getLocation();
		double distance = Math.min(LANDING_DISTANCE, from.distanceTo(hitPosition));
		return hitPosition.subtract(direction.scale(distance));
	}

	private Vec3 unproject(float x, float y, float z) {
		Vector3f offset = inverseViewProjection.transformProject(x, y, z, new Vector3f());
		return pose.position().add(offset.x(), offset.y(), offset.z());
	}

	private static BlockHitResult clipBlock(ClientLevel level, SectionPos anchor, ClipContext ray, BlockPos pos) {
		int chunkX = SectionPos.blockToSectionCoord(pos.getX());
		int chunkZ = SectionPos.blockToSectionCoord(pos.getZ());
		if (!ChokmahArea.isInRenderRange(anchor, chunkX, chunkZ)
				|| !level.hasChunk(chunkX, chunkZ) || level.isOutsideBuildHeight(pos)) {
			return null;
		}

		Vec3 from = ray.getFrom();
		Vec3 to = ray.getTo();
		BlockState state = level.getBlockState(pos);
		BlockHitResult block = null;
		if (state.getRenderShape() != RenderShape.INVISIBLE || state.hasBlockEntity()) {
			block = level.clipWithInteractionOverride(from, to, pos, ray.getBlockShape(state, level, pos), state);
		}
		BlockHitResult fluid = ray.getFluidShape(state.getFluidState(), level, pos).clip(from, to, pos);
		if (block == null) {
			return fluid;
		}
		if (fluid != null && from.distanceToSqr(fluid.getLocation()) < from.distanceToSqr(block.getLocation())) {
			return fluid;
		}
		return block;
	}
}
