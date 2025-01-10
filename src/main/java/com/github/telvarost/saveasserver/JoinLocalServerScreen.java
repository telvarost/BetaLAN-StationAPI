package com.github.telvarost.saveasserver;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;

@Environment(EnvType.CLIENT)
public class JoinLocalServerScreen extends Screen {

    LocalServerManager manager;

    public JoinLocalServerScreen(Screen parent) {
    }

    public void init() {
        this.buttons.clear();
        manager = LocalServerManager.getInstance();
        if (manager.status == ServerStatus.NOT_STARTED) {
            manager.start();
        }
        this.minecraft.progressRenderer.progressStart("Opening World to LAN...");
        this.minecraft.progressRenderer.progressStagePercentage(0);
    }

    @Override
    public void tick() {
        manager.run();
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
        this.minecraft.progressRenderer.progressStage(manager.loadingText);
        this.minecraft.progressRenderer.progressStagePercentage(manager.loadingProgress);
    }
}
