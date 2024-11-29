package com.github.telvarost.saveasserver.mixin.server;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @Shadow public PlayerManager playerManager;

    @Shadow public abstract void stop();

    @Unique private int serverTicks = 0;

    @Inject(method = "tick", at = @At("RETURN"), cancellable = true)
    private void tick(CallbackInfo ci) {
        serverTicks++;

        if (1000 < serverTicks) {
            serverTicks = 0;

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
