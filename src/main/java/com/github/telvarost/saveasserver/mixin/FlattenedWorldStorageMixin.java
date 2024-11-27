package com.github.telvarost.saveasserver.mixin;

import com.github.telvarost.saveasserver.ModHelper;
import net.minecraft.world.storage.AlphaWorldStorageSource;
import net.minecraft.world.storage.WorldStorage;
import net.modificationstation.stationapi.impl.world.storage.FlattenedWorldStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;

@Mixin(FlattenedWorldStorage.class)
public class FlattenedWorldStorageMixin extends AlphaWorldStorageSource {

    public FlattenedWorldStorageMixin(File file) {
        super(file);
    }

    @Inject(
            method = "method_1009",
            at = @At("HEAD")
    )
    public void method_1009(String saveName, boolean createPlayerDataDir, CallbackInfoReturnable<WorldStorage> cir) {
        ModHelper.ModHelperFields.CurrentWorldFolder = saveName;
    }
}
