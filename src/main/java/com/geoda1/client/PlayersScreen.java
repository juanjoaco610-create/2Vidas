package com.geoda1.client;

import com.geoda1.Net;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class PlayersScreen extends Screen {
    public PlayersScreen() { super(Component.literal("Jugadores")); }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        int x = width / 2 - 120, y = 30;
        g.drawString(font, "Vivos", x, y, 0x55FF55);
        y += 14;
        for (Net.Info i : ClientCache.MAP.values()) {
            if (i.dead()) continue;
            g.drawString(font, i.name() + "  Vidas: " + i.lives() + "  Corazones: " + (int) Math.ceil(i.health() / 2f)
                    + "  Muslitos: " + (int) Math.ceil(i.food() / 2f), x, y, 0xFFFFFF);
            y += 11;
        }
        y += 8;
        g.drawString(font, "Muertos", x, y, 0xFF5555);
        y += 14;
        for (Net.Info i : ClientCache.MAP.values()) {
            if (!i.dead()) continue;
            g.drawString(font, i.name() + (i.secs() > 0 ? "  (se puede revivir " + ClientCache.time(i.secs()) + ")" : "  (definitivo)"), x, y, 0xAAAAAA);
            y += 11;
        }
        super.render(g, mx, my, pt);
    }

    @Override
    public boolean keyPressed(int key, int sc, int mod) {
        if (ClientModEvents.LIST.matches(key, sc)) { onClose(); return true; }
        return super.keyPressed(key, sc, mod);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
