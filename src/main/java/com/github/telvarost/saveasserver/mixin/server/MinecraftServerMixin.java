package com.github.telvarost.saveasserver.mixin.server;

import com.github.telvarost.saveasserver.ModHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.world.storage.WorldStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.io.IOException;

@Environment(EnvType.SERVER)
@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @Shadow public PlayerManager playerManager;

    @Shadow public abstract void stop();

    @Unique private int serverTicks = 0;

    @Unique private int loadingLevel = 0;

    @Unique private int currentProgress = 0;

    @Inject(method = "loadWorld", at = @At("HEAD"), cancellable = true)
    private void saveAsServer_loadWorldHead(WorldStorageSource storageSource, String worldDir, long seed, CallbackInfo ci) {
        File clientLockFile = new File("client.lock");
        if (clientLockFile.exists()) {
            ModHelper.ModHelperFields.IsClientServer = true;
        }

        File saveAsServerFolder = new File("logging");
        if (!saveAsServerFolder.exists()) {
            saveAsServerFolder.mkdirs();
        }

        File saveAsServerBegin = new File("logging" + File.separator + "preparing-level");
        try {
            saveAsServerBegin.createNewFile();
        } catch (IOException e) {
            System.out.println("Failed to log server actions to client: " + e.toString());
        }
    }

    @Inject(
            method = "loadWorld",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/logging/Logger;info(Ljava/lang/String;)V",
                    ordinal = 1
            ),
            cancellable = true
    )
    private void saveAsServer_currentlyLoadingLevel(WorldStorageSource storageSource, String worldDir, long seed, CallbackInfo ci) {
        File saveAsServerEnd = new File("logging" + File.separator + "loading-level-" + loadingLevel);
        try {
            saveAsServerEnd.createNewFile();
        } catch (IOException e) {
            System.out.println("Failed to log server actions to client: " + e.toString());
        }
        loadingLevel++;
    }

    @Inject(method = "logProgress", at = @At("RETURN"), cancellable = true)
    private void saveAsServer_logProgress(String progressType, int progress, CallbackInfo ci) {
        File saveAsServerEnd = new File("logging" + File.separator + "level-progress-" + progress);
        try {
            saveAsServerEnd.createNewFile();
        } catch (IOException e) {
            System.out.println("Failed to log level loading progress to client.");
        }
    }

    @Inject(method = "loadWorld", at = @At("RETURN"), cancellable = true)
    private void saveAsServer_loadWorldReturn(WorldStorageSource storageSource, String worldDir, long seed, CallbackInfo ci) {
        loadingLevel = 0;
        File saveAsServerEnd = new File("logging" + File.separator + "done-loading");
        try {
            saveAsServerEnd.createNewFile();
        } catch (IOException e) {
            System.out.println("Failed to log server actions to client: " + e.toString());
        }
    }

    @Inject(method = "tick", at = @At("RETURN"), cancellable = true)
    private void saveAsServer_tick(CallbackInfo ci) {
        serverTicks++;

        if (1000 < serverTicks) {
            serverTicks = 0;

            if (ModHelper.ModHelperFields.IsClientServer) {
                if (  (null != playerManager)
                   && (null != playerManager.players)
                ) {
                    if (playerManager.players.isEmpty()) {
                        stop();
                    }
                }
            }
        }
    }
}
