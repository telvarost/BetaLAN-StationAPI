package com.github.telvarost.saveasserver;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resource.language.TranslationStorage;
import org.lwjgl.input.Keyboard;

import java.io.File;
import java.io.IOException;

@Environment(EnvType.CLIENT)
public class JoinLocalServerScreen extends Screen {
    private Screen parent;
    private TextFieldWidget serverField;

    public JoinLocalServerScreen(Screen parent) {
        this.parent = parent;
    }

    public void tick() {
        this.serverField.tick();
    }

    public void init() {
        ModHelper.ModHelperFields.LaunchingLocalServer = false;
        TranslationStorage var1 = TranslationStorage.getInstance();
        Keyboard.enableRepeatEvents(true);
        this.buttons.clear();
        this.buttons.add(new ButtonWidget(0, this.width / 2 - 100, this.height / 4 + 96 + 12, var1.get("multiplayer.connect")));
        this.buttons.add(new ButtonWidget(1, this.width / 2 - 100, this.height / 4 + 120 + 12, var1.get("gui.cancel")));
        String var2 = this.minecraft.options.lastServer.replaceAll("_", ":");
        ((ButtonWidget)this.buttons.get(0)).active = var2.length() > 0;
        this.serverField = new TextFieldWidget(this, this.textRenderer, this.width / 2 - 100, this.height / 4 - 10 + 50 + 18, 200, 20, var2);
        this.serverField.focused = true;
        this.serverField.setMaxLength(128);

        /** - Launch server */
        String argNoGui = (Config.config.SERVER_GUI_ENABLED) ? "" : "nogui";
        ProcessBuilder pb = new ProcessBuilder(Config.config.JAVA_PATH, "-jar", "local-babric-server.0.16.9.jar", argNoGui);
        pb.directory(Minecraft.getRunDirectory());
        try {
            ModHelper.ModHelperFields.CurrentServer = pb.start();
        } catch (IOException ex) {
            System.out.println("Failed to open client world to LAN: " + ex.toString());
        }

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
    }

    public void removed() {
        Keyboard.enableRepeatEvents(false);
    }

    protected void buttonClicked(ButtonWidget button) {
        if (button.active) {
            if (button.id == 1) {
                this.minecraft.setScreen(this.parent);
            } else if (button.id == 0) {
                String var2 = this.serverField.getText().trim();
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

        }
    }

    private int parseInt(String s, int defaultValue) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception var4) {
            return defaultValue;
        }
    }

    protected void keyPressed(char character, int keyCode) {
        this.serverField.keyPressed(character, keyCode);
        if (character == '\r') {
            this.buttonClicked((ButtonWidget)this.buttons.get(0));
        }

        ((ButtonWidget)this.buttons.get(0)).active = this.serverField.getText().length() > 0;
    }

    protected void mouseClicked(int mouseX, int mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        this.serverField.mouseClicked(mouseX, mouseY, button);
    }

    public void render(int mouseX, int mouseY, float delta) {
        TranslationStorage var4 = TranslationStorage.getInstance();
        this.renderBackground();
        this.drawCenteredTextWithShadow(this.textRenderer, var4.get("multiplayer.title"), this.width / 2, this.height / 4 - 60 + 20, 16777215);
        this.drawTextWithShadow(this.textRenderer, var4.get("multiplayer.info1"), this.width / 2 - 140, this.height / 4 - 60 + 60 + 0, 10526880);
        this.drawTextWithShadow(this.textRenderer, var4.get("multiplayer.info2"), this.width / 2 - 140, this.height / 4 - 60 + 60 + 9, 10526880);
        this.drawTextWithShadow(this.textRenderer, var4.get("multiplayer.ipinfo"), this.width / 2 - 140, this.height / 4 - 60 + 60 + 36, 10526880);
        this.serverField.render();
        super.render(mouseX, mouseY, delta);
    }
}
