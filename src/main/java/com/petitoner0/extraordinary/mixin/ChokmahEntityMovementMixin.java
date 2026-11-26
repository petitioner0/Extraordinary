package com.petitoner0.extraordinary.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.petitoner0.extraordinary.finalritual.chokmah.ChokmahArea;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Entity.class)
public abstract class ChokmahEntityMovementMixin {
	@ModifyVariable(method = "move", at = @At("HEAD"), argsOnly = true)
	private Vec3 extraordinary$limitMovement(Vec3 movement) {
		if ((Object) this instanceof Player player) {
			ChokmahArea area = ChokmahArea.get(player);
			if (area != null) {
				double x = player.getX();
				double z = player.getZ();
				double halfWidth = player.getBbWidth() * 0.5;
				return new Vec3(area.clampX(x + movement.x(), halfWidth) - x, movement.y(),
						area.clampZ(z + movement.z(), halfWidth) - z);
			}
		}
		return movement;
	}

	@WrapMethod(method = "setPosRaw")
	private void extraordinary$keepPlayerInsideArea(double x, double y, double z, Operation<Void> original) {
		if ((Object) this instanceof Player player) {
			ChokmahArea area = ChokmahArea.get(player);
			if (area != null) {
				double halfWidth = player.getBbWidth() * 0.5;
				x = area.clampX(x, halfWidth);
				z = area.clampZ(z, halfWidth);
			}
		}
		original.call(x, y, z);
	}
}
