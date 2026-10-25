package com.geoda1;

import com.geoda1.client.ClientHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class FrascoBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(3, 0, 3, 13, 14, 13);

    public FrascoBlock(Properties p) {
        super(p);
    }

    @Override
    public VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) {
        return SHAPE;
    }

    @Override
    public InteractionResult use(BlockState s, Level l, BlockPos pos, Player pl, InteractionHand h, BlockHitResult hit) {
        ItemStack st = pl.getItemInHand(h);
        if (!st.is(Reg.CORAZON_DORADO.get())) {
            if (!l.isClientSide) pl.displayClientMessage(Component.literal("Necesitás un corazón dorado en la mano."), true);
            return InteractionResult.sidedSuccess(l.isClientSide);
        }
        if (l.isClientSide) DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHooks.openFrasco(pos));
        return InteractionResult.sidedSuccess(l.isClientSide);
    }
}
