package com.github.telvarost.saveasserver.mixin.server;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.WorldProperties;
import net.minecraft.world.storage.AlphaWorldStorage;
import net.minecraft.world.storage.RegionWorldStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

@Mixin(RegionWorldStorage.class)
public class AlphaWorldStorageMixin {

    @Inject(
            method = "save(Lnet/minecraft/world/WorldProperties;Ljava/util/List;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    public void save(WorldProperties properties, List players, CallbackInfo ci) {
        //System.out.println(players);
    }
}
