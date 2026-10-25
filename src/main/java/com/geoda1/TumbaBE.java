package com.geoda1;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TumbaBE extends BlockEntity {
    public String owner = "";

    public TumbaBE(BlockPos pos, BlockState state) {
        super(Reg.TUMBA_BE.get(), pos, state);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("Owner", owner);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        owner = tag.getString("Owner");
    }
}
