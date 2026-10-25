package com.geoda1;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TumbaBlock extends BaseEntityBlock {
    private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 14, 15);

    public TumbaBlock(Properties p) {
        super(p);
    }

    @Override
    public RenderShape getRenderShape(BlockState s) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) {
        return SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TumbaBE(pos, state);
    }

    @Override
    public InteractionResult use(BlockState s, Level l, BlockPos pos, Player pl, InteractionHand h, BlockHitResult hit) {
        if (!l.isClientSide && l.getBlockEntity(pos) instanceof TumbaBE be && !be.owner.isEmpty())
            pl.displayClientMessage(Component.literal("Tumba de " + be.owner), true);
        return InteractionResult.sidedSuccess(l.isClientSide);
    }
}
