package com.github.telvarost.saveasserver.mixin;

import com.github.telvarost.saveasserver.ModHelper;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
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

    @WrapOperation(
            method = "updateProperties",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/nbt/NbtCompound;putString(Ljava/lang/String;Ljava/lang/String;)V"
            )
    )
    private void updateProperties(NbtCompound instance, String value, String s, Operation<Void> original) {
        original.call(instance, value, s.replace("./saves/", ""));
    }

    @Environment(EnvType.CLIENT)
    @Inject(
            method = "getPlayerNbt",
            at = @At("HEAD"),
            cancellable = true
    )
    public void getPlayerNbt(CallbackInfoReturnable<NbtCompound> cir) {
        try {
            /** - Get server files and check for server lock */
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

            /** - Check if joining a client or server world */
            Minecraft minecraft = (Minecraft)FabricLoader.getInstance().getGameInstance();
            if (  (null  != minecraft.world)
               && (false == minecraft.world.isRemote)
               )
            {
                /** - If world is client, retrieve client player from server player data */
                File playerFile = new File(playerDataDir, minecraft.session.username + ".dat");
                if (playerFile.exists()) {
                    NbtCompound readPlayerNbt = new NbtCompound();
                    readPlayerNbt = NbtIo.readCompressed(new FileInputStream(playerFile));

                    /** - Fix player position */
                    NbtList posNbt = readPlayerNbt.getList("Pos");
                    double playerYLevel = ((NbtDouble)posNbt.get(1)).value + 1.0;
                    ((NbtDouble)posNbt.get(1)).value = playerYLevel;

                    this.playerNbt = readPlayerNbt;
                }

                /** - Free server lock */
                serverLock.delete();
            }
        } catch (Exception ex) {
            System.out.println("Failed to retrieve player data: " + ex.toString());
        }
    }
}
