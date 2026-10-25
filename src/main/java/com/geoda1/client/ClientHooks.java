package com.geoda1.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

public class ClientHooks {
    public static void openFrasco(BlockPos pos) { Minecraft.getInstance().setScreen(new FrascoScreen(pos)); }
    public static void openLost(int lives) { Minecraft.getInstance().setScreen(new LostScreen(lives)); }
    public static void openDead() { Minecraft.getInstance().setScreen(new DeadScreen()); }
}
