package com.petitoner0.extraordinary.finalritual.chokmah;

import com.petitoner0.extraordinary.Extraordinary;
import net.minecraft.core.SectionPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ChokmahCameraPayload(SectionPos anchor) implements CustomPacketPayload {
	public static final Type<ChokmahCameraPayload> TYPE = new Type<>(Extraordinary.id("chokmah_camera_start"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ChokmahCameraPayload> CODEC =
			StreamCodec.composite(SectionPos.STREAM_CODEC, ChokmahCameraPayload::anchor, ChokmahCameraPayload::new);

	@Override
	public Type<ChokmahCameraPayload> type() {
		return TYPE;
	}
}
