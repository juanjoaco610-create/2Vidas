package com.geoda1.client;

import com.geoda1.Net;
import net.minecraft.client.Minecraft;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ClientCache {
    public static final Map<UUID, Net.Info> MAP = new LinkedHashMap<>();
    private static boolean wasDead = false;

    public static void set(List<Net.Info> list) {
        MAP.clear();
        for (Net.Info i : list) MAP.put(i.id(), i);
        Net.Info me = me();
        boolean dead = me != null && me.dead();
        if (dead && !wasDead) ClientHooks.openDead();
        wasDead = dead;
    }

    public static Net.Info me() {
        Minecraft mc = Minecraft.getInstance();
        return mc.player == null ? null : MAP.get(mc.player.getUUID());
    }

    public static String time(int secs) {
        return String.format("%02d:%02d", secs / 60, secs % 60);
    }
}
