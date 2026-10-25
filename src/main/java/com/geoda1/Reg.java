package com.geoda1;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class Reg {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Geoda1.ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Geoda1.ID);
    public static final DeferredRegister<BlockEntityType<?>> BES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Geoda1.ID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Geoda1.ID);

    public static final RegistryObject<Block> FRASCO = BLOCKS.register("frasco",
            () -> new FrascoBlock(BlockBehaviour.Properties.of().strength(0.5f).sound(SoundType.GLASS).noOcclusion()));
    public static final RegistryObject<Block> TUMBA = BLOCKS.register("tumba",
            () -> new TumbaBlock(BlockBehaviour.Properties.of().strength(3f, 1200f).sound(SoundType.STONE)
                    .requiresCorrectToolForDrops().noOcclusion()));

    public static final RegistryObject<Item> FRASCO_ITEM = ITEMS.register("frasco", () -> new BlockItem(FRASCO.get(), new Item.Properties()));
    public static final RegistryObject<Item> TUMBA_ITEM = ITEMS.register("tumba", () -> new BlockItem(TUMBA.get(), new Item.Properties()));
    public static final RegistryObject<Item> CORAZON_DORADO = ITEMS.register("corazon_dorado",
            () -> new Item(new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> CORAZON_COMESTIBLE = ITEMS.register("corazon_comestible", () -> new AdminHeartItem(false));
    public static final RegistryObject<Item> CORAZON_VIDA = ITEMS.register("corazon_vida", () -> new AdminHeartItem(true));

    public static final RegistryObject<BlockEntityType<TumbaBE>> TUMBA_BE = BES.register("tumba",
            () -> BlockEntityType.Builder.of(TumbaBE::new, TUMBA.get()).build(null));

    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("geoda1",
            () -> CreativeModeTab.builder().title(Component.literal("Geoda1"))
                    .icon(() -> new ItemStack(CORAZON_DORADO.get()))
                    .displayItems((p, o) -> {
                        o.accept(FRASCO_ITEM.get());
                        o.accept(TUMBA_ITEM.get());
                        o.accept(CORAZON_DORADO.get());
                        o.accept(CORAZON_COMESTIBLE.get());
                        o.accept(CORAZON_VIDA.get());
                    }).build());
}
