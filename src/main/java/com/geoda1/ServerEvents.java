package com.geoda1;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ServerEvents {

    @SubscribeEvent
    public void onDeath(LivingDeathEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer p)) return;
        if (Life.isDead(p)) return;
        CompoundTag t = Life.tag(p);
        int lives = t.getInt("lives") - 1;
        boolean admin = p.hasPermissions(2);

        t.putDouble("dx", p.getX());
        t.putDouble("dy", p.getY());
        t.putDouble("dz", p.getZ());
        t.putString("dim", p.level().dimension().location().toString());

        if (lives > 0 || admin) {
            if (lives <= 0) lives = 2; // los admin nunca mueren del todo
            t.putInt("lives", lives);
            t.putBoolean("pending", true);
            Life.broadcast(p, p.getGameProfile().getName() + " perdió una vida (le quedan " + lives + ")");
            return;
        }

        // muerte definitiva
        e.setCanceled(true);
        t.putInt("lives", 0);
        t.putBoolean("dead", true);
        t.putBoolean("expired", false);
        t.putLong("until", p.server.overworld().getGameTime() + Life.WAIT_TICKS);
        p.setHealth(p.getMaxHealth());
        p.setGameMode(GameType.SPECTATOR); // los espectadores no reciben daño de la explosión
        ServerLevel lv = p.serverLevel();
        lv.explode(null, p.getX(), p.getY(), p.getZ(), 3.0f, Level.ExplosionInteraction.MOB);
        net.minecraft.core.BlockPos bp = p.blockPosition();
        if (!lv.getBlockState(bp).canBeReplaced()) bp = bp.above();
        lv.setBlock(bp, Reg.TUMBA.get().defaultBlockState(), 3);
        if (lv.getBlockEntity(bp) instanceof TumbaBE be) {
            be.owner = p.getGameProfile().getName();
            be.setChanged();
        }
        Life.broadcast(p, p.getGameProfile().getName() + " perdió sus 2 vidas. Tiene 1 hora para ser revivido.");
    }

    @SubscribeEvent
    public void onRespawn(PlayerEvent.PlayerRespawnEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer p)) return;
        Life.applyHearts(p);
        if (Life.tag(p).getBoolean("pending"))
            Net.CH.send(PacketDistributor.PLAYER.with(() -> p), new Net.LostMsg(Life.lives(p)));
    }

    @SubscribeEvent
    public void onClone(PlayerEvent.Clone e) {
        CompoundTag o = e.getOriginal().getPersistentData().getCompound("geoda1");
        e.getEntity().getPersistentData().put("geoda1", o.copy());
    }

    @SubscribeEvent
    public void onLogin(PlayerEvent.PlayerLoggedInEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer p)) return;
        Life.tag(p);
        Life.applyHearts(p);
        if (Life.isDead(p)) p.setGameMode(GameType.SPECTATOR);
    }

    @SubscribeEvent
    public void onChat(ServerChatEvent e) {
        if (Life.isDead(e.getPlayer())) {
            e.setCanceled(true);
            e.getPlayer().sendSystemMessage(Component.literal("Los muertos no pueden escribir en el chat."));
        }
    }

    @SubscribeEvent
    public void onDrops(LivingDropsEvent e) {
        if (e.getEntity() instanceof WitherBoss w) {
            for (int i = 0; i < 3; i++)
                e.getDrops().add(new ItemEntity(w.level(), w.getX(), w.getY(), w.getZ(), new ItemStack(Reg.CORAZON_DORADO.get())));
        }
    }

    @SubscribeEvent
    public void tick(TickEvent.ServerTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        MinecraftServer s = e.getServer();
        if (s.getTickCount() % 20 != 0) return;
        long now = s.overworld().getGameTime();
        List<Net.Info> list = new ArrayList<>();
        for (ServerPlayer p : s.getPlayerList().getPlayers()) {
            CompoundTag t = Life.tag(p);
            boolean dead = t.getBoolean("dead");
            int secs = 0;
            if (dead) {
                long left = t.getLong("until") - now;
                secs = (int) Math.max(0, left / 20);
                if (left <= 0 && !t.getBoolean("expired")) {
                    t.putBoolean("expired", true);
                    Life.broadcast(p, p.getGameProfile().getName() + " no fue revivido a tiempo: muerte definitiva.");
                }
            }
            list.add(new Net.Info(p.getUUID(), p.getGameProfile().getName(), p.getHealth(),
                    p.getFoodData().getFoodLevel(), t.getInt("lives"), dead, secs));
        }
        Net.CH.send(PacketDistributor.ALL.noArg(), new Net.ListMsg(list));
    }

    /** Revivir usando el frasco + corazón dorado. */
    public static void reviveWithJar(ServerPlayer p, net.minecraft.core.BlockPos pos, UUID target) {
        if (p.distanceToSqr(Vec3.atCenterOf(pos)) > 64) return;
        if (!p.level().getBlockState(pos).is(Reg.FRASCO.get())) return;
        ItemStack st = p.getMainHandItem().is(Reg.CORAZON_DORADO.get()) ? p.getMainHandItem()
                : p.getOffhandItem().is(Reg.CORAZON_DORADO.get()) ? p.getOffhandItem() : ItemStack.EMPTY;
        if (st.isEmpty()) return;
        ServerPlayer t = p.server.getPlayerList().getPlayer(target);
        if (t == null) return;
        CompoundTag tt = Life.tag(t);
        if (!tt.getBoolean("dead") || tt.getBoolean("expired")) return;
        st.shrink(1);
        Life.revive(t, p.serverLevel(), new Vec3(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5), p.getGameProfile().getName());
    }

    @SubscribeEvent
    public void commands(RegisterCommandsEvent e) {
        var d = e.getDispatcher();
        d.register(Commands.literal("revive").requires(s -> s.hasPermission(2))
                .then(Commands.argument("jugador", EntityArgument.player()).executes(c -> {
                    ServerPlayer t = EntityArgument.getPlayer(c, "jugador");
                    Life.revive(t, t.serverLevel(), t.position(), c.getSource().getTextName());
                    return 1;
                })));
        d.register(lifeCmd("quitarvida", -1));
        d.register(lifeCmd("agregarvida", 1));
    }

    private LiteralArgumentBuilder<CommandSourceStack> lifeCmd(String name, int sign) {
        return Commands.literal(name).requires(s -> s.hasPermission(2))
                .then(Commands.argument("jugador", EntityArgument.player())
                        .executes(c -> change(c.getSource(), EntityArgument.getPlayer(c, "jugador"), sign))
                        .then(Commands.argument("cantidad", IntegerArgumentType.integer(1, 1000))
                                .executes(c -> change(c.getSource(), EntityArgument.getPlayer(c, "jugador"),
                                        sign * IntegerArgumentType.getInteger(c, "cantidad")))));
    }

    private int change(CommandSourceStack src, ServerPlayer t, int delta) {
        int v = Math.max(0, Life.lives(t) + delta);
        Life.setLives(t, v);
        src.sendSuccess(() -> Component.literal(t.getGameProfile().getName() + " ahora tiene " + v + " vidas"), true);
        return v;
    }
}
