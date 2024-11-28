package com.github.telvarost.saveasserver.mixin.client;

import com.github.telvarost.saveasserver.Config;
import com.github.telvarost.saveasserver.ModHelper;
import io.github.prospector.modmenu.gui.ModMenuButtonWidget;
import io.github.prospector.modmenu.mixin.MixinGuiButton;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
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

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.Scanner;

@Environment(EnvType.CLIENT)
@Mixin(GameMenuScreen.class)
public class PauseMixin extends Screen {
    public PauseMixin() { }

    @Inject(method = "init", at = @At("RETURN"), cancellable = true)
    public void init_return(CallbackInfo ci) {
        if (  (null  != this.minecraft.world)
           && (false == this.minecraft.world.isRemote)
        ) {
            ButtonWidget optionsButton = minecraft.isApplet ? (ButtonWidget) this.buttons.get(this.buttons.size() - 2) : (ButtonWidget) this.buttons.get(2);

            int newWidth = ((MixinGuiButton) optionsButton).getWidth() / 2 - 1;
            ((MixinGuiButton) optionsButton).setWidth(newWidth);

            TranslationStorage translationStorage = TranslationStorage.getInstance();
            this.buttons.add(new ModMenuButtonWidget(73, this.width / 2 + 2, optionsButton.y, newWidth, 20,  translationStorage.get("menu.saveasserver.opentolan")));
        }
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

            /** - Edit server properties */
            File serverPropertiesFile = new File(Minecraft.getRunDirectory(), "server.properties");
            if (!serverPropertiesFile.exists())
            {
                try {
                    serverPropertiesFile.createNewFile();
                    FileWriter writer = new FileWriter(serverPropertiesFile);
                    writer.write("#Minecraft server properties\n");
                    writer.write("#" + new Date() + "\n");
                    if (null != ModHelper.ModHelperFields.CurrentWorldFolder) {
                        writer.write("level-name=./saves/" + ModHelper.ModHelperFields.CurrentWorldFolder + "\n");
                    } else {
                        writer.write("#level-name=world\n");
                    }
                    writer.write("default-gamemode=0\n");
                    writer.write("view-distance=10\n");
                    writer.write("white-list=false\n");
                    writer.write("server-ip=\n");
                    writer.write("pvp=true\n");
                    writer.write("level-seed=\n");
                    writer.write("spawn-animals=true\n");
                    writer.write("server-port=" + Config.config.SERVER_PORT + "\n");
                    writer.write("allow-nether=true\n");
                    writer.write("spawn-monsters=tru\n");
                    writer.write("max-players=20\n");
                    writer.write("online-mode=false\n");
                    writer.write("allow-flight=false\n");
                } catch (FileNotFoundException e) {
                    System.out.println("Failed to create LAN server properties: ");
                    e.printStackTrace();
                }
            } else {
                File tempServerPropertiesFile = new File(Minecraft.getRunDirectory(), "_server.properties");
                Files.copy(serverPropertiesFile.toPath(), tempServerPropertiesFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                try {
                    FileWriter writer = new FileWriter(tempServerPropertiesFile);
                    Reader reader = new FileReader(serverPropertiesFile);
                    BufferedReader br = new BufferedReader(reader);

                    while(br.ready()) {
                        String currentLine = br.readLine();

                        // Change level to client world
                        if (currentLine.contains("level-name")) {
                            if (null != ModHelper.ModHelperFields.CurrentWorldFolder) {
                                writer.write("level-name=./saves/" + ModHelper.ModHelperFields.CurrentWorldFolder + "\n");
                            }
                        }

                        // Change port to configured port
                        if (currentLine.contains("server-port")) {
                            if (null != ModHelper.ModHelperFields.CurrentWorldFolder) {
                                writer.write("server-port=" + Config.config.SERVER_PORT + "\n");
                            }
                        }

                        // Change to offline mode
                        if (currentLine.contains("online-mode")) {
                            if (null != ModHelper.ModHelperFields.CurrentWorldFolder) {
                                writer.write("online-mode=false\n");
                            }
                        }
                    }

                    writer.close();
                    br.close();
                    reader.close();
                    tempServerPropertiesFile.renameTo(serverPropertiesFile);
                } catch (FileNotFoundException e) {
                    System.out.println("Failed to update LAN server properties: ");
                    e.printStackTrace();
                }
                if (serverPropertiesFile.exists() && tempServerPropertiesFile.exists()) {
                    tempServerPropertiesFile.delete();
                }
            }

            /** - Launch server */
            String argNoGui = (Config.config.SERVER_GUI_ENABLED) ? "" : "nogui";
            ProcessBuilder pb = new ProcessBuilder(Config.config.JAVA_PATH, "-jar", "local-babric-server.0.16.9.jar", argNoGui);
            pb.directory(Minecraft.getRunDirectory());
            ModHelper.ModHelperFields.CurrentServer = pb.start();

            /** - Monitor server to see when world is ready */
            // Also need to create a loading screen to display monitored data
//            new Thread(new Runnable() {
//                public void run() {
//                    try {
//                        RandomAccessFile in = new RandomAccessFile("server.log", "r");
//                        String line;
//                        while (true) {
//                            if ((line = in.readLine()) != null) {
//                                System.out.println(line);
//                            } else {
//                                Thread.sleep(1000); // poll the file every 1 second
//                            }
//                        }
//                    } catch (FileNotFoundException e) {
//                        throw new RuntimeException(e);
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
//                    } catch (IOException e) {
//                        throw new RuntimeException(e);
//                    }
//                }
//            }).start();

            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                public void run() {
                    if (null != ModHelper.ModHelperFields.CurrentServer) {
                        ModHelper.ModHelperFields.CurrentServer.destroy();
                    }
                }
            }, "ShutdownServer-thread"));
        } catch (Exception ex) {
            System.out.println("Failed to open client world to LAN: " + ex.toString());
        }
    }
}
