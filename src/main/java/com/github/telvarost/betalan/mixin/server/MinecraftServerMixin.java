package com.github.telvarost.betalan.mixin.server;

import com.github.telvarost.betalan.BetaLAN;
import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.ServerProperties;
import net.minecraft.world.ServerWorld;
import net.minecraft.world.storage.WorldStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;

@Environment(EnvType.SERVER)
@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @Shadow
    public PlayerManager playerManager;

    @Shadow
    public abstract void stop();

    @Shadow
    public ServerProperties properties;
    @Shadow
    public ServerWorld[] worlds;
    @Unique
    private int serverTicks = 0;

    @Inject(method = "loadWorld", at = @At("HEAD"))
    private void betaLan_loadWorldHead(WorldStorageSource storageSource, String worldDir, long seed, CallbackInfo ci) {
        File clientLockFile = new File("client.lock");
        if (clientLockFile.exists()) {
            BetaLAN.isLanServer = true;
        }
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Ljava/util/logging/Logger;info(Ljava/lang/String;)V", ordinal = 0, shift = At.Shift.AFTER))
    public void shareProgressStartingServer(CallbackInfoReturnable<Boolean> cir) {
        System.out.println("$;info;starting;0;Starting Minecraft Server");
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Ljava/util/logging/Logger;warning(Ljava/lang/String;)V", ordinal = 0, shift = At.Shift.AFTER))
    public void shareProgressNotEnoughRam(CallbackInfoReturnable<Boolean> cir) {
        System.out.println("$;warning;lowram;0;Not Enough Allocated RAM");
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Ljava/util/logging/Logger;info(Ljava/lang/String;)V", ordinal = 2, shift = At.Shift.AFTER))
    public void shareProgressStartingNetworking(CallbackInfoReturnable<Boolean> cir) {
        System.out.println("$;info;startingnetwork;5;Start Server On Port");
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Ljava/util/logging/Logger;warning(Ljava/lang/String;)V", ordinal = 2, shift = At.Shift.AFTER))
    public void shareProgressFailedToBindPort(CallbackInfoReturnable<Boolean> cir) {
        System.out.println("$;error;failedbind;0;Failed to Bind to Port " + this.properties.getProperty("server-port", 25565));
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Ljava/util/logging/Logger;info(Ljava/lang/String;)V", ordinal = 3, shift = At.Shift.AFTER))
    public void shareProgressPreparingWorld(CallbackInfoReturnable<Boolean> cir) {
        System.out.println("$;info;preparingworld;10;Preparing World");
    }

    @Unique
    int currentlyPreparing = 0;

    @Unique
    double totalProgress = 0D;

    @Inject(method = "loadWorld", at = @At(value = "INVOKE", target = "Ljava/util/logging/Logger;info(Ljava/lang/String;)V", ordinal = 1, shift = At.Shift.AFTER))
    public void shareProgressPreparingStartRegion(WorldStorageSource worldDir, String seed, long par3, CallbackInfo ci, @Local(ordinal = 1) int var9) {
        currentlyPreparing = var9;
        totalProgress = 10D + ((90D / this.worlds.length) * currentlyPreparing);
        System.out.println("$;info;preparingstartregion;" + (int) totalProgress + ";Preparing World " + var9);
    }

    @Inject(method = "logProgress", at = @At(value = "INVOKE", target = "Ljava/util/logging/Logger;info(Ljava/lang/String;)V", ordinal = 0, shift = At.Shift.AFTER))
    public void shareProgressPreparingSpawnArea(String progressType, int progress, CallbackInfo ci) {
        if (progressType.equals("Preparing spawn area")) {
            double worldProgress = totalProgress + ((90D / this.worlds.length) * (progress / 100D));
            System.out.println("$;info;preparingspawnarea;" + (int) worldProgress + ";Preparing Spawn Area for World " + currentlyPreparing + ": " + progress + "%");
        }
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Ljava/util/logging/Logger;info(Ljava/lang/String;)V", ordinal = 4, shift = At.Shift.AFTER))
    public void shareProgressDone(CallbackInfoReturnable<Boolean> cir) {
        System.out.println("$;info;done;100;Done");
    }


    @Inject(method = "tick", at = @At("RETURN"))
    private void betaLan_tick(CallbackInfo ci) {
        serverTicks++;

        if (serverTicks > 100) {
            if (playerManager != null && playerManager.players.isEmpty()) {
                this.stop();
            }

            serverTicks = 0;
        }
    }
}
