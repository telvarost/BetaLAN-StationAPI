package com.github.telvarost.betalan.mixin.client;

import com.github.telvarost.betalan.LocalServerManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.entity.player.PlayerEntity;
import net.modificationstation.stationapi.api.entity.player.PlayerHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unchecked")
@Environment(EnvType.CLIENT)
@Mixin(GameMenuScreen.class)
public class PauseMixin extends Screen {
    public PauseMixin() {
    }

    @Inject(method = "init", at = @At("RETURN"))
    public void init_return(CallbackInfo ci) {
        if ((this.minecraft.world != null && !this.minecraft.world.isRemote)) {
            ButtonWidget optionsButton = minecraft.isApplet ? (ButtonWidget) this.buttons.get(this.buttons.size() - 2) : (ButtonWidget) this.buttons.get(2);

            int newWidth = optionsButton.width / 2 - 1;
            optionsButton.width = newWidth;

            TranslationStorage translationStorage = TranslationStorage.getInstance();
            this.buttons.add(new ButtonWidget(73, this.width / 2 + 2, optionsButton.y, newWidth, 20, translationStorage.get("menu.betalan.opentolan")));
        }
    }

    @Inject(method = "buttonClicked", at = @At("RETURN"))
    protected void betaLan_openToLanButtonClicked(ButtonWidget arg, CallbackInfo ci) {
        if (arg.id == 73) {
            PlayerEntity player = PlayerHelper.getPlayerFromGame();
            if (null != player) {
                LocalServerManager.getInstance().prepareAndLaunchServer(player);
            }
        }
    }
}
