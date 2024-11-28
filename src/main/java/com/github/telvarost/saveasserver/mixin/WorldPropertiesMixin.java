package com.github.telvarost.saveasserver.mixin;

import com.github.telvarost.saveasserver.ModHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
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

            Minecraft minecraft = (Minecraft)FabricLoader.getInstance().getGameInstance();
            if (  (null  != minecraft.world)
               && (false == minecraft.world.isRemote)
               )
            {
                NbtCompound readPlayerNbt = new NbtCompound();
                File var4 = new File(playerDataDir, minecraft.session.username + ".dat");
                if (var4.exists()) {
                    readPlayerNbt = NbtIo.readCompressed(new FileInputStream(var4));
                    NbtList posNbt = readPlayerNbt.getList("Pos");
                    double playerYLevel = ((NbtDouble)posNbt.get(1)).value + 2.0;
                    ((NbtDouble)posNbt.get(1)).value = playerYLevel;
                }
                this.playerNbt = readPlayerNbt;
                serverLock.delete();
            }

        } catch (Exception var5) {
            Minecraft minecraft = (Minecraft)FabricLoader.getInstance().getGameInstance();
            System.out.println("Failed to save player data for " + minecraft.session.username);
        }

        System.out.println(this.playerNbt);
    }
}
