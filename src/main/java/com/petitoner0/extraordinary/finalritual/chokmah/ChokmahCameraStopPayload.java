package com.petitoner0.extraordinary.finalritual.chokmah;

import com.petitoner0.extraordinary.Extraordinary;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ChokmahCameraStopPayload() implements CustomPacketPayload {
	public static final ChokmahCameraStopPayload INSTANCE = new ChokmahCameraStopPayload();
	public static final Type<ChokmahCameraStopPayload> TYPE = new Type<>(Extraordinary.id("chokmah_camera_stop"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ChokmahCameraStopPayload> CODEC = StreamCodec.unit(INSTANCE);

	@Override
	public Type<ChokmahCameraStopPayload> type() {
		return TYPE;
	}
}
