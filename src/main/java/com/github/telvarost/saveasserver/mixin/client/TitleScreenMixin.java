package com.github.telvarost.saveasserver.mixin.client;

import com.github.telvarost.saveasserver.JoinLocalServerScreen;
import com.github.telvarost.saveasserver.LocalServerManager;
import com.github.telvarost.saveasserver.ServerStatus;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    public void init(CallbackInfo ci) {
        if (LocalServerManager.getInstance().status == ServerStatus.LAUNCHING) {
            this.minecraft.setScreen(new JoinLocalServerScreen(this));
            ci.cancel();
        }
    }
}
