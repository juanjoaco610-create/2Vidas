package com.geoda1.client;

import com.geoda1.Net;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class LostScreen extends Screen {
    private final int lives;

    public LostScreen(int lives) {
        super(Component.literal("Has perdido una vida"));
        this.lives = lives;
    }

    @Override
    protected void init() {
        addRenderableWidget(Button.builder(Component.literal("Aparecer en el spawn point"), b -> choose(0))
                .bounds(width / 2 - 100, height / 2 + 40, 200, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Aparecer en el lugar de muerte"), b -> choose(1))
                .bounds(width / 2 - 100, height / 2 + 64, 200, 20).build());
    }

    private void choose(int c) {
        Net.CH.sendToServer(new Net.ChoiceMsg(c));
        onClose();
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        g.drawCenteredString(font, "Has perdido una vida", width / 2, height / 2 - 70, 0xFF5555);
        for (int i = 0; i < 2; i++) {
            var tex = i < lives ? Tex.FULL : Tex.BROKEN;
            g.blit(tex, width / 2 - 38 + i * 40, height / 2 - 50, 36, 36, 0, 0, 12, 12, 12, 12);
        }
        super.render(g, mx, my, pt);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
