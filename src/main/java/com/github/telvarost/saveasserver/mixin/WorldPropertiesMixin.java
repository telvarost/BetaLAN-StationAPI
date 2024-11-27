package com.github.telvarost.saveasserver.mixin;

import com.github.telvarost.saveasserver.ModHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.WorldProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.io.FileInputStream;

@Mixin(WorldProperties.class)
public class WorldPropertiesMixin {

    @Shadow private NbtCompound playerNbt;

    @Environment(EnvType.CLIENT)
    @Inject(
            method = "getPlayerNbt",
            at = @At("HEAD"),
            cancellable = true
    )
    public void getPlayerNbt(CallbackInfoReturnable<NbtCompound> cir) {
        try {
            File savesDir = new File(Minecraft.getRunDirectory(), "saves");
            File worldDir = new File(savesDir, ModHelper.ModHelperFields.CurrentWorldFolder);
            File serverLock = new File(worldDir, "server.lock");
            if (!serverLock.exists()) {
                return;
            }
            File playerDataDir = new File(worldDir, "players");
            if (!playerDataDir.exists()) {
                playerDataDir.mkdirs();
            }

            NbtCompound var2 = new NbtCompound();
            Minecraft minecraft = (Minecraft)FabricLoader.getInstance().getGameInstance();
            File var4 = new File(playerDataDir, minecraft.session.username + ".dat");
            if (var4.exists()) {
                var2 = NbtIo.readCompressed(new FileInputStream(var4));
            }
            this.playerNbt = var2;
            serverLock.delete();

        } catch (Exception var5) {
            Minecraft minecraft = (Minecraft)FabricLoader.getInstance().getGameInstance();
            System.out.println("Failed to save player data for " + minecraft.session.username);
        }

        System.out.println(this.playerNbt);
    }
}
