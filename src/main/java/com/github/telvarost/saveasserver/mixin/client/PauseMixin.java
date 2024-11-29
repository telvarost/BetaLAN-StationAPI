package com.github.telvarost.saveasserver.mixin.client;

import com.github.telvarost.saveasserver.Config;
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

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Date;

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
            File tempPlayerFile = new File(playerDataDir, "_tmp_.dat");
            File playerFile = new File(playerDataDir, this.minecraft.session.username + ".dat");
            NbtIo.writeCompressed(var2, new FileOutputStream(tempPlayerFile));
            if (playerFile.exists()) {
                playerFile.delete();
            }
            tempPlayerFile.renameTo(playerFile);

            /** - Close client world */
            this.minecraft.stats.increment(Stats.LEAVE_GAME, 1);
            if (this.minecraft.isWorldRemote()) {
                this.minecraft.world.disconnect();
            }
            this.minecraft.setWorld((World)null);
            this.minecraft.setScreen(new TitleScreen());

            /** - Create/edit LAN server OP list */
            if (Config.config.AUTO_OP_LAN_SERVER_HOST) {
                File serverOpListFile = new File(Minecraft.getRunDirectory(), "ops.txt");
                if (!serverOpListFile.exists())
                {
                    /** - LAN server OP list file does not exist, create new LAN server OP list file */
                    createLocalServerOpListFile(serverOpListFile);
                } else {
                    /** - LAN server OP list exists, edit LAN server OP list file */
                    editLocalServerOpListFile(serverOpListFile);
                }
            }

            /** - Create/edit LAN server properties */
            File serverPropertiesFile = new File(Minecraft.getRunDirectory(), "server.properties");
            if (!serverPropertiesFile.exists())
            {
                /** - LAN server properties file does not exist, create new LAN server properties file */
                createLocalServerPropertiesFile(serverPropertiesFile);
            } else {
                /** - LAN server properties exists, edit LAN server properties file */
                editLocalServerPropertiesFile(serverPropertiesFile);
            }

            /** - Extract server jar file */
            File storedServerJar = new File(Minecraft.getRunDirectory().getAbsolutePath() + File.separator + "local-babric-server.0.16.9.jar");
            if (!storedServerJar.exists()) {
                ModHelper.copy( getClass().getResourceAsStream("/assets/saveasserver/local-babric-server.0.16.9.jar")
                              , Minecraft.getRunDirectory().getAbsolutePath() + File.separator + "local-babric-server.0.16.9.jar");
            }

            /** - Create file telling the server it is a local server */
            File clientLockFile = new File(Minecraft.getRunDirectory(), "client.lock");
            if (!clientLockFile.exists()) {
                clientLockFile.createNewFile();
            }

            /** - Prepare folder for server logging info to client */
            File saveAsServerFolder = new File(Minecraft.getRunDirectory(), "logging");
            if (!saveAsServerFolder.exists()) {
                saveAsServerFolder.mkdirs();
            }

            /** - Launch server */
            String argNoGui = (Config.config.SERVER_GUI_ENABLED) ? "" : "nogui";
            ProcessBuilder pb = new ProcessBuilder(Config.config.JAVA_PATH, "-jar", "local-babric-server.0.16.9.jar", argNoGui);
            pb.directory(Minecraft.getRunDirectory());
            ModHelper.ModHelperFields.CurrentServer = pb.start();

            /** - Monitor server to see when world is ready */
            // TODO: Start Loading Progress bar and give more info on loading percentage
            File saveAsServerBegin = new File("logging" + File.separator + "preparing-level");
            while (!saveAsServerBegin.exists());
            saveAsServerBegin.delete();
            System.out.println("Preparing LAN server...");
            File saveAsServerEnd = new File("logging" + File.separator + "done-loading");
            while (!saveAsServerEnd.exists());
            saveAsServerEnd.delete();
            System.out.println("Done loading LAN server!");
            // TODO: Have client join the server
        } catch (Exception ex) {
            System.out.println("Failed to open client world to LAN: " + ex.toString());
        }
    }

    @Unique
    private void createLocalServerOpListFile(File serverOpListFile) {
        try {
            PrintWriter writer = new PrintWriter(serverOpListFile, "UTF-8");
            writer.println(this.minecraft.session.username);

            /** - Release file resources */
            writer.close();
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            System.out.println("Failed to create LAN server OP list: ");
            e.printStackTrace();
        }
    }

    @Unique
    private void editLocalServerOpListFile(File serverOpListFile) {
        File tempServerOpListFile = new File(Minecraft.getRunDirectory(), "_ops.txt");
        try {
            Files.copy(serverOpListFile.toPath(), tempServerOpListFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            FileWriter writer = new FileWriter(tempServerOpListFile);
            Reader reader = new FileReader(tempServerOpListFile);
            BufferedReader br = new BufferedReader(reader);
            boolean isClientPlayerOp = false;

            /** - Scan file for values to change */
            while(br.ready()) {
                String currentLine = br.readLine();

                /** - Check if client player is OP */
                if (currentLine.contains(this.minecraft.session.username)) {
                    isClientPlayerOp = true;
                }
            }

            /** - Client player is not OP, OP client player */
            if (false == isClientPlayerOp) {
                writer.write(this.minecraft.session.username + "\n");
            }

            /** - Release file resources */
            writer.close();
            br.close();
            reader.close();

            /** - Replace current OP list file with newly created OP list file */
            tempServerOpListFile.renameTo(serverOpListFile);
        } catch (IOException e) {
            System.out.println("Failed to update LAN server properties: ");
            e.printStackTrace();
        }

        /** - Ensure temporary OP list file is deleted if it is no longer needed */
        if (serverOpListFile.exists() && tempServerOpListFile.exists()) {
            tempServerOpListFile.delete();
        }
    }

    @Unique
    private void createLocalServerPropertiesFile(File serverPropertiesFile) {
        try {
            PrintWriter writer = new PrintWriter(serverPropertiesFile, "UTF-8");
            writer.println("#Minecraft server properties");
            writer.println("#" + new Date());
            if (null != ModHelper.ModHelperFields.CurrentWorldFolder) {
                writer.println("level-name=./saves/" + ModHelper.ModHelperFields.CurrentWorldFolder);
            } else {
                writer.println("#level-name=world");
            }
            writer.println("default-gamemode=0");
            writer.println("view-distance=10");
            writer.println("white-list=false");
            writer.println("server-ip=");
            writer.println("pvp=true");
            writer.println("level-seed=");
            writer.println("spawn-animals=true");
            writer.println("server-port=" + Config.config.SERVER_PORT);
            writer.println("allow-nether=true");
            writer.println("spawn-monsters=true");
            writer.println("max-players=20");
            writer.println("online-mode=false");
            writer.println("allow-flight=false");

            /** - Release file resources */
            writer.close();
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            System.out.println("Failed to create LAN server properties: ");
            e.printStackTrace();
        }
    }

    @Unique
    private void editLocalServerPropertiesFile(File serverPropertiesFile) {
        File tempServerPropertiesFile = new File(Minecraft.getRunDirectory(), "_server.properties");
        try {
            Files.copy(serverPropertiesFile.toPath(), tempServerPropertiesFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            FileWriter writer = new FileWriter(tempServerPropertiesFile);
            Reader reader = new FileReader(serverPropertiesFile);
            BufferedReader br = new BufferedReader(reader);

            /** - Scan file for values to change */
            while(br.ready()) {
                String currentLine = br.readLine();

                /** - Change level to currently selected client world */
                if (currentLine.contains("level-name")) {
                    if (null != ModHelper.ModHelperFields.CurrentWorldFolder) {
                        writer.write("level-name=./saves/" + ModHelper.ModHelperFields.CurrentWorldFolder + "\n");
                    }
                }

                /** - Change port to port from GCAPI3 config */
                if (currentLine.contains("server-port")) {
                    if (null != ModHelper.ModHelperFields.CurrentWorldFolder) {
                        writer.write("server-port=" + Config.config.SERVER_PORT + "\n");
                    }
                }

                /** - Change to offline mode if online-mode is forced false */
                if (Config.config.FORCE_ONLINEMODE_FALSE) {
                    if (currentLine.contains("online-mode")) {
                        if (null != ModHelper.ModHelperFields.CurrentWorldFolder) {
                            writer.write("online-mode=false\n");
                        }
                    }
                }
            }

            /** - Release file resources */
            writer.close();
            br.close();
            reader.close();

            /** - Replace current properties file with newly created properties file */
            tempServerPropertiesFile.renameTo(serverPropertiesFile);
        } catch (IOException e) {
            System.out.println("Failed to update LAN server properties: ");
            e.printStackTrace();
        }

        /** - Ensure temporary properties file is deleted if it is no longer needed */
        if (serverPropertiesFile.exists() && tempServerPropertiesFile.exists()) {
            tempServerPropertiesFile.delete();
        }
    }
}
