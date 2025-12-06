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
@Mixin(value = GameMenuScreen.class, priority = 1100)
public class PauseMixin extends Screen {
    public PauseMixin() {
    }

    @Inject(method = "init", at = @At("TAIL"))
    public void init_return(CallbackInfo ci) {
        if ((this.minecraft.world != null && !this.minecraft.world.isRemote)) {
            ButtonWidget optionsButton = null;

            for (int buttonIndex = 0; buttonIndex < this.buttons.size(); buttonIndex++) {
                ButtonWidget buttonWidget = (ButtonWidget)this.buttons.get(buttonIndex);

                if (  (null != buttonWidget)
                   && (0 == buttonWidget.id)
                ) {
                    optionsButton = buttonWidget;
                }
            }

            if (null != optionsButton) {
                int newWidth = optionsButton.width / 2 - 1;
                optionsButton.width = newWidth;

                TranslationStorage translationStorage = TranslationStorage.getInstance();
                this.buttons.add(new ButtonWidget( 73
                                                 , (optionsButton.x + optionsButton.width) + 3
                                                 , optionsButton.y
                                                 , newWidth
                                                 , 20
                                                 , translationStorage.get("menu.betalan.opentolan") ));
            }
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
