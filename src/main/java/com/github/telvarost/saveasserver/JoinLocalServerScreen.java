package com.github.telvarost.saveasserver;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;

import java.io.File;
import java.io.IOException;

@Environment(EnvType.CLIENT)
public class JoinLocalServerScreen extends Screen {
    private Screen parent;
    private int progress = 0;

    public JoinLocalServerScreen(Screen parent) {
        this.parent = parent;
    }

    public void init() {
        ModHelper.ModHelperFields.LaunchingLocalServer = false;
        this.buttons.clear();

        /** - Prepare loading bar */
        this.minecraft.progressRenderer.progressStartNoAbort("Opening World to LAN...");
        this.minecraft.progressRenderer.progressStage("Loading World");
        this.minecraft.progressRenderer.progressStagePercentage(0);

        /** - Launch server */
        String argNoGui = (Config.config.SERVER_GUI_ENABLED) ? "" : "nogui";
        ProcessBuilder pb = new ProcessBuilder(Config.config.JAVA_PATH, "-jar", "local-babric-server.0.16.9.jar", argNoGui);
        pb.directory(Minecraft.getRunDirectory());
        try {
            ModHelper.ModHelperFields.CurrentServer = pb.start();
        } catch (IOException ex) {
            System.out.println("Failed to open client world to LAN: " + ex.toString());
        }
    }

    public void tick() {
        // TODO: Give more info on loading percentage
        
        /** - Monitor server to see when world is ready */
        File saveAsServerBegin = new File("logging" + File.separator + "preparing-level");
        if (saveAsServerBegin.exists()) {
            progress = 25;
            saveAsServerBegin.delete();
            System.out.println("Preparing LAN server...");
        }

        File saveAsServerEnd = new File("logging" + File.separator + "done-loading");
        if (saveAsServerEnd.exists()) {
            progress = 75;
            saveAsServerEnd.delete();
            System.out.println("Done loading LAN server!");

            /** - Have the client join the local server */
            joinLocalServer();
        }
    }

    private void joinLocalServer() {
        String var2 = "127.0.0.1:" + Config.config.SERVER_PORT;
        this.minecraft.options.lastServer = var2.replaceAll(":", "_");
        this.minecraft.options.save();
        String[] var3 = var2.split(":");
        if (var2.startsWith("[")) {
            int var4 = var2.indexOf("]");
            if (var4 > 0) {
                String var5 = var2.substring(1, var4);
                String var6 = var2.substring(var4 + 1).trim();
                if (var6.startsWith(":") && var6.length() > 0) {
                    var6 = var6.substring(1);
                    var3 = new String[]{var5, var6};
                } else {
                    var3 = new String[]{var5};
                }
            }
        }

        if (var3.length > 2) {
            var3 = new String[]{var2};
        }

        this.minecraft.setScreen(new ConnectScreen(this.minecraft, var3[0], var3.length > 1 ? this.parseInt(var3[1], 25565) : 25565));
    }

    private int parseInt(String s, int defaultValue) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception var4) {
            return defaultValue;
        }
    }

    public void removed() {
        /** - Do nothing */
    }

    protected void buttonClicked(ButtonWidget button) {
        /** - Do nothing */
    }

    protected void keyPressed(char character, int keyCode) {
        /** - Do nothing */
    }

    protected void mouseClicked(int mouseX, int mouseY, int button) {
        /** - Do nothing */
    }

    public void render(int mouseX, int mouseY, float delta) {
        this.minecraft.progressRenderer.progressStagePercentage(progress);
    }
}
