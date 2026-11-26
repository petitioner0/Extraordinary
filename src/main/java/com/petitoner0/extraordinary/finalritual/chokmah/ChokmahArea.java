package com.petitoner0.extraordinary.finalritual.chokmah;

import com.petitoner0.extraordinary.Extraordinary;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public record ChokmahArea(SectionPos anchor, Level level) {
	public static final int RENDER_DISTANCE = 4;
	private static final int HALF_WIDTH_IN_CHUNKS = 4;
	private static final AttachmentType<ChokmahArea> ACTIVE_AREA = AttachmentRegistry.create(Extraordinary.id("chokmah_area"));

	public static ChokmahArea set(Player player, SectionPos anchor) {
		ChokmahArea area = new ChokmahArea(anchor, player.level());
		player.setAttached(ACTIVE_AREA, area);
		return area;
	}

	public static ChokmahArea get(Player player) {
		ChokmahArea area = player.getAttached(ACTIVE_AREA);
		return area != null && area.level() == player.level() ? area : null;
	}

	public static boolean clear(Player player) {
		return player.removeAttached(ACTIVE_AREA) != null;
	}

	public static boolean isInRenderRange(SectionPos anchor, int chunkX, int chunkZ) {
		return Math.abs(chunkX - anchor.x()) <= RENDER_DISTANCE
				&& Math.abs(chunkZ - anchor.z()) <= RENDER_DISTANCE;
	}

	public double clampX(double x, double halfPlayerWidth) {
		return clampToArea(x, anchor.x(), halfPlayerWidth);
	}

	public double clampZ(double z, double halfPlayerWidth) {
		return clampToArea(z, anchor.z(), halfPlayerWidth);
	}

	private static double clampToArea(double coordinate, int anchorChunk, double halfPlayerWidth) {
		int min = SectionPos.sectionToBlockCoord(anchorChunk - HALF_WIDTH_IN_CHUNKS);
		int max = SectionPos.sectionToBlockCoord(anchorChunk + HALF_WIDTH_IN_CHUNKS);
		return Mth.clamp(coordinate, min + halfPlayerWidth, max - halfPlayerWidth);
	}
}
