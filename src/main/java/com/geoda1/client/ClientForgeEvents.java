package com.geoda1.client;

import com.geoda1.Geoda1;
import com.geoda1.Net;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Geoda1.ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientForgeEvents {

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        while (ClientModEvents.LIST.consumeClick())
            if (mc.player != null && mc.screen == null) mc.setScreen(new PlayersScreen());
    }

    /** Reemplaza los corazones vanilla por los de Geoda1. */
    @SubscribeEvent
    public static void healthPre(RenderGuiOverlayEvent.Pre e) {
        if (e.getOverlay() != VanillaGuiOverlay.PLAYER_HEALTH.type()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.gameMode == null || !mc.gameMode.canHurtPlayer()) return;
        if (!(mc.getCameraEntity() instanceof Player p) || !(mc.gui instanceof ForgeGui fg)) return;
        e.setCanceled(true);
        GuiGraphics g = e.getGuiGraphics();
        int w = g.guiWidth(), h = g.guiHeight();
        int hearts = (int) Math.ceil(p.getMaxHealth() / 2f);
        float hp = p.getHealth();
        for (int i = 0; i < hearts; i++) {
            int x = w / 2 - 91 + (i % 10) * 8;
            int y = h - fg.leftHeight - (i / 10) * 10;
            g.blit(Tex.EMPTY, x, y, 9, 9, 0, 0, 12, 12, 12, 12);
            if (hp >= (i + 1) * 2) g.blit(Tex.FULL, x, y, 9, 9, 0, 0, 12, 12, 12, 12);
            else if (hp > i * 2) g.blit(Tex.HALF, x, y, 9, 9, 0, 0, 12, 12, 12, 12);
        }
        fg.leftHeight += ((hearts + 9) / 10) * 10;
    }

    /** Vidas a la izquierda del inventario (hotbar). */
    @SubscribeEvent
    public static void livesPost(RenderGuiOverlayEvent.Post e) {
        if (e.getOverlay() != VanillaGuiOverlay.HOTBAR.type()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.player == null) return;
        Net.Info me = ClientCache.me();
        int lives = me == null ? 2 : me.lives();
        GuiGraphics g = e.getGuiGraphics();
        int w = g.guiWidth(), h = g.guiHeight();
        int slots = Math.max(2, Math.min(lives, 10));
        for (int i = 0; i < slots; i++) {
            int x = w / 2 - 91 - 6 - (i + 1) * 15;
            g.blit(i < lives ? Tex.FULL : Tex.BROKEN, x, h - 22, 14, 14, 0, 0, 12, 12, 12, 12);
        }
    }

    /** Sobre cada jugador: vidas, corazones y muslitos (o tiempo si está muerto). */
    @SubscribeEvent
    public static void nameTag(RenderNameTagEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;
        Net.Info i = ClientCache.MAP.get(p.getUUID());
        if (i == null) return;
        MutableComponent c = Component.literal("").append(e.getOriginalContent());
        if (i.dead()) c.append(Component.literal("  §7[muerto " + ClientCache.time(i.secs()) + "]"));
        else c.append(Component.literal("  §cVidas §f" + i.lives() + " §c♥§f" + (int) Math.ceil(i.health() / 2f)
                + " §6●§f" + (int) Math.ceil(i.food() / 2f)));
        e.setContent(c);
    }
}
