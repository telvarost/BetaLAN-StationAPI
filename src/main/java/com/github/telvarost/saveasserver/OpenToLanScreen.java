package com.github.telvarost.saveasserver;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;

@SuppressWarnings("DanglingJavadoc")
@Environment(EnvType.CLIENT)
public class OpenToLanScreen extends Screen {

    LocalServerManager manager;

    public OpenToLanScreen(Screen parent) {
    }

    public void init() {
        this.buttons.clear();
        manager = LocalServerManager.getInstance();

        // This check is here because resizing the window will reinit the screen causing this to run again
        if (manager.status == ServerStatus.NOT_STARTED || manager.status == ServerStatus.RUNNING) {
            manager.start();
        }

        // Initial progress message
        this.minecraft.progressRenderer.progressStart("Opening World to LAN...");
        this.minecraft.progressRenderer.progressStagePercentage(0);
    }

    @Override
    public void tick() {
        this.minecraft.progressRenderer.stage = manager.loadingText;

        // This is here beacuse the server manager is running on another thread and if it was to call it
        // the game would crash because it needs the OpenGL context
        if (manager.status == ServerStatus.RUNNING) {
            this.minecraft.setScreen(new ConnectScreen(this.minecraft, "127.0.0.1", Config.config.SERVER_PORT));
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
        this.minecraft.progressRenderer.progressStagePercentage(manager.loadingProgress);
    }
}
