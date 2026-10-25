package com.geoda1;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class Life {
    public static final int MAX_EXTRA = 15; // 10 base + 15 = 25 corazones
    public static final long WAIT_TICKS = 72000L; // 1 hora
    private static final UUID HP = UUID.fromString("5f1a8c50-0000-4000-8000-0000000000a1");

    public static CompoundTag tag(Player p) {
        CompoundTag d = p.getPersistentData();
        if (!d.contains("geoda1")) {
            CompoundTag t = new CompoundTag();
            t.putInt("lives", 2);
            d.put("geoda1", t);
        }
        return d.getCompound("geoda1");
    }

    public static int lives(Player p) { return tag(p).getInt("lives"); }
    public static void setLives(Player p, int v) { tag(p).putInt("lives", Math.max(0, v)); }
    public static boolean isDead(Player p) { return tag(p).getBoolean("dead"); }

    public static boolean addHeart(Player p) {
        CompoundTag t = tag(p);
        if (t.getInt("extra") >= MAX_EXTRA) return false;
        t.putInt("extra", t.getInt("extra") + 1);
        applyHearts(p);
        p.heal(2f);
        return true;
    }

    public static void applyHearts(Player p) {
        AttributeInstance a = p.getAttribute(Attributes.MAX_HEALTH);
        if (a == null) return;
        a.removeModifier(HP);
        int extra = tag(p).getInt("extra");
        if (extra > 0) a.addPermanentModifier(new AttributeModifier(HP, "geoda1_hearts", extra * 2.0, AttributeModifier.Operation.ADDITION));
    }

    public static void broadcast(ServerPlayer p, String msg) {
        p.server.getPlayerList().broadcastSystemMessage(Component.literal(msg), false);
    }

    /** Revive a un jugador (quita el modo espectador, 2 vidas) en una posición con rayo. */
    public static void revive(ServerPlayer target, ServerLevel lv, Vec3 pos, String by) {
        CompoundTag t = tag(target);
        t.putBoolean("dead", false);
        t.putBoolean("expired", false);
        t.putInt("lives", 2);
        target.setGameMode(GameType.SURVIVAL);
        target.teleportTo(lv, pos.x, pos.y, pos.z, target.getYRot(), target.getXRot());
        target.setHealth(target.getMaxHealth());
        LightningBolt b = EntityType.LIGHTNING_BOLT.create(lv);
        if (b != null) {
            b.moveTo(pos.x, pos.y, pos.z);
            b.setVisualOnly(true);
            lv.addFreshEntity(b);
        }
        broadcast(target, target.getGameProfile().getName() + " revivió" + (by == null ? "" : " (por " + by + ")"));
    }

    public static void applyChoice(ServerPlayer p, int choice) {
        CompoundTag t = tag(p);
        if (!t.getBoolean("pending")) return;
        t.putBoolean("pending", false);
        if (choice == 1 && t.contains("dx")) {
            net.minecraft.resources.ResourceLocation rl = new net.minecraft.resources.ResourceLocation(t.getString("dim"));
            ServerLevel lv = p.server.getLevel(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, rl));
            if (lv != null) p.teleportTo(lv, t.getDouble("dx"), t.getDouble("dy"), t.getDouble("dz"), p.getYRot(), p.getXRot());
        }
    }
}
