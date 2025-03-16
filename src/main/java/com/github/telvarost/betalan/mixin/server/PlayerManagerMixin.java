package com.github.telvarost.betalan.mixin.server;

import com.github.telvarost.betalan.BetaLAN;
import com.github.telvarost.betalan.Config;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
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

import java.io.File;

@Environment(EnvType.SERVER)
@Mixin(PlayerManager.class)
public class PlayerManagerMixin {

    @Shadow
    private MinecraftServer server;

    @Unique
    private String hostPlayerName = null;

    @Inject(method = "loadPlayerData", at = @At("HEAD"))
    public void loadPlayerData(ServerPlayerEntity player, CallbackInfo ci) {
        File savesDir = new File(Minecraft.getRunDirectory(), "saves");
        File worldDir = new File(savesDir, BetaLAN.CurrentWorldFolder);
        File playerDataDir = new File(worldDir, "players");
        File var2 = new File(playerDataDir, player.name + ".dat");
        try {
            if (!var2.exists()) {
                if (FabricLoader.getInstance().isModLoaded("bhcreative")) {
                    player.creative_setCreative(1 == Config.config.ADVANCED_SERVER_CONFIG.DEFAULT_GAMEMODE.ordinal());
                }
            }
        } catch (Exception ex) {
            /** - Do nothing */
        }
    }

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
