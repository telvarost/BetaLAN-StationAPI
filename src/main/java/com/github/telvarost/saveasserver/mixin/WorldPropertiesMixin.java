package com.github.telvarost.saveasserver.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtList;
import net.minecraft.world.WorldProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldProperties.class)
public class WorldPropertiesMixin {

    @Inject(
            method = "updateProperties",
            at = @At("HEAD"),
            cancellable = true
    )
    private void updateProperties(NbtCompound nbt, NbtCompound playerNbt, CallbackInfo ci) {
//
//        NbtList var2 = playerNbt.getList("Pos");
//        if (null != var2)
//        {
//            double velocityX = ((NbtDouble)var2.get(0)).value;
//            double velocityY = ((NbtDouble)var2.get(1)).value;
//            double velocityZ = ((NbtDouble)var2.get(2)).value;
//            System.out.println(velocityX + ", " + velocityY + ", " + velocityZ);
//        }
    }
}
