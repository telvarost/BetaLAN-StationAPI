package com.github.telvarost.saveasserver.mixin.client;

import com.github.telvarost.saveasserver.ModHelper;
import io.github.prospector.modmenu.gui.ModMenuButtonWidget;
import io.github.prospector.modmenu.mixin.MixinGuiButton;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.modificationstation.stationapi.api.entity.player.PlayerHelper;
import org.spongepowered.asm.mixin.Mixin;
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

    @Inject(method = "buttonClicked", at = @At("RETURN"), cancellable = true)
    protected void buttonClicked(ButtonWidget arg, CallbackInfo ci) {
        if (arg.id == 73) {
            PlayerEntity player = PlayerHelper.getPlayerFromGame();
            if (null != player)
            {
                savePlayerData(player);
            }
            //this.minecraft.setScreen(new PackScreen(this));
        }
    }


    public void savePlayerData(PlayerEntity player) {
        try {
            File savesDir = new File(Minecraft.getRunDirectory(), "saves");
            File worldDir = new File(savesDir, ModHelper.ModHelperFields.CurrentWorldFolder);

            File playerDataDir = new File(worldDir, "players");
            if (!playerDataDir.exists()) {
                playerDataDir.mkdirs();
            }

            NbtCompound var2 = new NbtCompound();
            player.write(var2);
            File var3 = new File(playerDataDir, "_tmp_.dat");
            File var4 = new File(playerDataDir, this.minecraft.session.username + ".dat");
            NbtIo.writeCompressed(var2, new FileOutputStream(var3));
            if (var4.exists()) {
                var4.delete();
            }

            var3.renameTo(var4);
        } catch (Exception var5) {
            System.out.println("Failed to save player data for " + this.minecraft.session.username);
        }

    }
}
