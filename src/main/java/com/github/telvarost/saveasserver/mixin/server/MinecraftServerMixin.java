package com.github.telvarost.saveasserver.mixin.server;

import com.github.telvarost.saveasserver.ModHelper;
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

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @Shadow public PlayerManager playerManager;

    @Shadow public abstract void stop();

    @Unique private int serverTicks = 0;

    @Inject(method = "loadWorld", at = @At("HEAD"), cancellable = true)
    private void loadWorld(WorldStorageSource storageSource, String worldDir, long seed, CallbackInfo ci) {
        File clientLockFile = new File("client.lock");
        if (clientLockFile.exists()) {
            ModHelper.ModHelperFields.IsClientServer = true;
        }
    }

    @Inject(method = "tick", at = @At("RETURN"), cancellable = true)
    private void tick(CallbackInfo ci) {
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
