package com.github.telvarost.saveasserver.mixin.client;

import com.github.telvarost.saveasserver.ModHelper;
import io.github.prospector.modmenu.gui.ModMenuButtonWidget;
import io.github.prospector.modmenu.mixin.MixinGuiButton;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.*;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.stat.Stats;
import net.minecraft.world.World;
import net.modificationstation.stationapi.api.entity.player.PlayerHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.io.FileOutputStream;

@Environment(EnvType.CLIENT)
@Mixin(GameMenuScreen.class)
public class PauseMixin extends Screen {
    public PauseMixin() { }

    @Inject(method = "init", at = @At("RETURN"), cancellable = true)
    public void init_return(CallbackInfo ci) {
        ButtonWidget optionsButton = minecraft.isApplet ? (ButtonWidget) this.buttons.get(this.buttons.size() - 2) : (ButtonWidget) this.buttons.get(2);

        int newWidth = ((MixinGuiButton) optionsButton).getWidth() / 2 - 1;
        ((MixinGuiButton) optionsButton).setWidth(newWidth);

        TranslationStorage translationStorage = TranslationStorage.getInstance();
        this.buttons.add(new ModMenuButtonWidget(73, this.width / 2 + 2, optionsButton.y, newWidth, 20,  translationStorage.get("menu.saveasserver.opentolan")));
    }

    @Inject(
            method = "buttonClicked",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;disconnect()V"
            ),
            cancellable = true
    )
    protected void saveAsServer_disconnectButtonClicked(ButtonWidget arg, CallbackInfo ci) {
        if (null != ModHelper.ModHelperFields.CurrentServer) {
            ModHelper.ModHelperFields.CurrentServer.destroy();
        }
    }

    @Inject(method = "buttonClicked", at = @At("RETURN"), cancellable = true)
    protected void saveAsServer_openToLanButtonClicked(ButtonWidget arg, CallbackInfo ci) {
        if (arg.id == 73) {
            PlayerEntity player = PlayerHelper.getPlayerFromGame();
            if (null != player) {
                prepareAndLaunchServer(player);
            }
        }
    }

    @Unique
    private void prepareAndLaunchServer(PlayerEntity player) {
        try {
            /** - Get server files and create server lock */
            File savesDir = new File(Minecraft.getRunDirectory(), "saves");
            File worldDir = new File(savesDir, ModHelper.ModHelperFields.CurrentWorldFolder);
            File serverLock = new File(worldDir, "server.lock");
            if (!serverLock.exists()) {
                serverLock.createNewFile();
            }
            File playerDataDir = new File(worldDir, "players");
            if (!playerDataDir.exists()) {
                playerDataDir.mkdirs();
            }

            /** - Save client player data to server player file */
            NbtCompound var2 = new NbtCompound();
            player.write(var2);
            File var3 = new File(playerDataDir, "_tmp_.dat");
            File var4 = new File(playerDataDir, this.minecraft.session.username + ".dat");
            NbtIo.writeCompressed(var2, new FileOutputStream(var3));
            if (var4.exists()) {
                var4.delete();
            }
            var3.renameTo(var4);

            /** - Close client world */
            this.minecraft.stats.increment(Stats.LEAVE_GAME, 1);
            if (this.minecraft.isWorldRemote()) {
                this.minecraft.world.disconnect();
            }
            this.minecraft.setWorld((World)null);
            this.minecraft.setScreen(new TitleScreen());

            /** - Launch server */
            ProcessBuilder pb = new ProcessBuilder("java", "-jar", "local-babric-server.0.16.9.jar");
            pb.directory(Minecraft.getRunDirectory());
            ModHelper.ModHelperFields.CurrentServer = pb.start();

        } catch (Exception ex) {
            System.out.println("Failed to open client world to LAN: " + ex.toString());
        }
    }
}
