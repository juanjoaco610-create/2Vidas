package com.geoda1;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Geoda1.ID)
public class Geoda1 {
    public static final String ID = "geoda1";

    public Geoda1() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        Reg.BLOCKS.register(bus);
        Reg.ITEMS.register(bus);
        Reg.BES.register(bus);
        Reg.TABS.register(bus);
        Net.init();
        MinecraftForge.EVENT_BUS.register(new ServerEvents());
    }
}
