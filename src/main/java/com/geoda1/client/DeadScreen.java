package com.geoda1.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;

public class DeadScreen extends Screen {
    public DeadScreen() { super(Component.literal("Has muerto")); }

    @Override
    protected void init() {
        addRenderableWidget(Button.builder(Component.literal("Esperar que te revivan"), b -> onClose())
                .bounds(width / 2 - 100, height / 2 + 10, 200, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Menú principal"), b -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) mc.level.disconnect();
            mc.disconnect();
            mc.setScreen(new TitleScreen());
        }).bounds(width / 2 - 100, height / 2 + 34, 200, 20).build());
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        g.pose().pushPose();
        g.pose().scale(2f, 2f, 1f);
        g.drawCenteredString(font, "Has muerto", width / 4, height / 4 - 25, 0xFF5555);
        g.pose().popPose();
        var me = ClientCache.me();
        if (me != null)
            g.drawCenteredString(font, me.secs() > 0 ? "Tiempo restante: " + ClientCache.time(me.secs()) : "Se acabó el tiempo", width / 2, height / 2 - 14, 0xFFFFFF);
        super.render(g, mx, my, pt);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
