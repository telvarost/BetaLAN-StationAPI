package com.github.telvarost.betalan.mixin.server;

import com.github.telvarost.betalan.BetaLAN;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.SERVER)
@Mixin(PlayerManager.class)
public class PlayerManagerMixin {

    @Shadow
    private MinecraftServer server;

    @Unique
    private String hostPlayerName = null;

    @Inject(method = "disconnect", at = @At("RETURN"))
    public void disconnect(ServerPlayerEntity player, CallbackInfo ci) {
        if(BetaLAN.isLanServer){
            if (player != null && hostPlayerName.equals(player.name)) {
                server.stop();
            }
        }
    }

    @Inject(method = "connectPlayer", at = @At("HEAD"))
    public void connectPlayer(ServerLoginNetworkHandler loginNetworkHandler, String name, CallbackInfoReturnable<ServerPlayerEntity> cir) {
        if (BetaLAN.isLanServer) {
            if (hostPlayerName == null) {
                hostPlayerName = name;
            }
        }
    }
}
