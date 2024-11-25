package com.github.telvarost.saveasserver.mixin;

import com.github.telvarost.saveasserver.Config;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecartEntity.class)
public class MinecartMixin {

	@Inject(
			method = "getCollisionAgainstShape",
			at = @At("HEAD"),
			cancellable = true
	)
	public void saveAsServer_onCollision(Entity other, CallbackInfoReturnable<Box> ci) {
		if (Config.config.CONFIG_TEST) {
			/** - Do nothing */
		}
	}
}
