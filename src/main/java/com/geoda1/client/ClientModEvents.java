package com.geoda1.client;

import com.geoda1.Geoda1;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Geoda1.ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {
    public static final KeyMapping LIST = new KeyMapping("key.geoda1.list", InputConstants.KEY_J, "key.categories.geoda1");

    @SubscribeEvent
    public static void keys(RegisterKeyMappingsEvent e) { e.register(LIST); }
}
