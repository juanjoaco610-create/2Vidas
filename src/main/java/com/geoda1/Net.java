package com.geoda1;

import com.geoda1.client.ClientCache;
import com.geoda1.client.ClientHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class Net {
    private static final String V = "1";
    public static final SimpleChannel CH = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Geoda1.ID, "main"), () -> V, V::equals, V::equals);

    public record Info(UUID id, String name, float health, int food, int lives, boolean dead, int secs) {}

    public static void init() {
        CH.registerMessage(0, ListMsg.class, ListMsg::enc, ListMsg::dec, ListMsg::handle);
        CH.registerMessage(1, LostMsg.class, LostMsg::enc, LostMsg::dec, LostMsg::handle);
        CH.registerMessage(2, ChoiceMsg.class, ChoiceMsg::enc, ChoiceMsg::dec, ChoiceMsg::handle);
        CH.registerMessage(3, ReviveMsg.class, ReviveMsg::enc, ReviveMsg::dec, ReviveMsg::handle);
    }

    public record ListMsg(List<Info> list) {
        void enc(FriendlyByteBuf b) {
            b.writeVarInt(list.size());
            for (Info i : list) {
                b.writeUUID(i.id());
                b.writeUtf(i.name());
                b.writeFloat(i.health());
                b.writeVarInt(i.food());
                b.writeVarInt(i.lives());
                b.writeBoolean(i.dead());
                b.writeVarInt(i.secs());
            }
        }

        static ListMsg dec(FriendlyByteBuf b) {
            int n = b.readVarInt();
            List<Info> l = new ArrayList<>();
            for (int k = 0; k < n; k++)
                l.add(new Info(b.readUUID(), b.readUtf(), b.readFloat(), b.readVarInt(), b.readVarInt(), b.readBoolean(), b.readVarInt()));
            return new ListMsg(l);
        }

        void handle(Supplier<NetworkEvent.Context> c) {
            c.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientCache.set(list)));
            c.get().setPacketHandled(true);
        }
    }

    public record LostMsg(int lives) {
        void enc(FriendlyByteBuf b) { b.writeVarInt(lives); }
        static LostMsg dec(FriendlyByteBuf b) { return new LostMsg(b.readVarInt()); }
        void handle(Supplier<NetworkEvent.Context> c) {
            c.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHooks.openLost(lives)));
            c.get().setPacketHandled(true);
        }
    }

    public record ChoiceMsg(int choice) {
        void enc(FriendlyByteBuf b) { b.writeVarInt(choice); }
        static ChoiceMsg dec(FriendlyByteBuf b) { return new ChoiceMsg(b.readVarInt()); }
        void handle(Supplier<NetworkEvent.Context> c) {
            c.get().enqueueWork(() -> {
                ServerPlayer p = c.get().getSender();
                if (p != null) Life.applyChoice(p, choice);
            });
            c.get().setPacketHandled(true);
        }
    }

    public record ReviveMsg(BlockPos pos, UUID target) {
        void enc(FriendlyByteBuf b) { b.writeBlockPos(pos); b.writeUUID(target); }
        static ReviveMsg dec(FriendlyByteBuf b) { return new ReviveMsg(b.readBlockPos(), b.readUUID()); }
        void handle(Supplier<NetworkEvent.Context> c) {
            c.get().enqueueWork(() -> {
                ServerPlayer p = c.get().getSender();
                if (p != null) ServerEvents.reviveWithJar(p, pos, target);
            });
            c.get().setPacketHandled(true);
        }
    }
}
