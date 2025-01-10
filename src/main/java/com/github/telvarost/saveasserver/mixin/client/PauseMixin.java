package com.github.telvarost.saveasserver.mixin.client;

import com.github.telvarost.saveasserver.LocalServerManager;
import io.github.prospector.modmenu.gui.ModMenuButtonWidget;
import io.github.prospector.modmenu.mixin.MixinGuiButton;
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

            int newWidth = ((MixinGuiButton) optionsButton).getWidth() / 2 - 1;
            ((MixinGuiButton) optionsButton).setWidth(newWidth);

            TranslationStorage translationStorage = TranslationStorage.getInstance();
            this.buttons.add(new ModMenuButtonWidget(73, this.width / 2 + 2, optionsButton.y, newWidth, 20, translationStorage.get("menu.saveasserver.opentolan")));
        }
    }

    @Inject(method = "buttonClicked", at = @At("RETURN"))
    protected void saveAsServer_openToLanButtonClicked(ButtonWidget arg, CallbackInfo ci) {
        if (arg.id == 73) {
            PlayerEntity player = PlayerHelper.getPlayerFromGame();
            if (null != player) {
                LocalServerManager.getInstance().prepareAndLaunchServer(player);
            }
        }
    }
}
