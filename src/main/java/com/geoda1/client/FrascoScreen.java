package com.geoda1.client;

import com.geoda1.Net;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

public class FrascoScreen extends Screen {
    private final BlockPos pos;

    public FrascoScreen(BlockPos pos) {
        super(Component.literal("Revivir"));
        this.pos = pos;
    }

    @Override
    protected void init() {
        int y = 50;
        for (Net.Info i : ClientCache.MAP.values()) {
            if (!i.dead() || i.secs() <= 0) continue;
            addRenderableWidget(Button.builder(Component.literal(i.name() + "  (" + ClientCache.time(i.secs()) + ")"), b -> {
                Net.CH.sendToServer(new Net.ReviveMsg(pos, i.id()));
                onClose();
            }).bounds(width / 2 - 100, y, 200, 20).build());
            y += 24;
        }
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        g.drawCenteredString(font, "¿A quién querés revivir?", width / 2, 30, 0xFFFFFF);
        if (renderables.isEmpty()) g.drawCenteredString(font, "No hay jugadores para revivir", width / 2, 60, 0xAAAAAA);
        super.render(g, mx, my, pt);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
