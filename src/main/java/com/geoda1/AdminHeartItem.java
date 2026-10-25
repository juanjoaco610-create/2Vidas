package com.geoda1;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

/** Corazón comestible (suma corazones, máx 25) o corazón de vida (regenera una vida). Solo admins. */
public class AdminHeartItem extends Item {
    private final boolean life;

    public AdminHeartItem(boolean life) {
        super(new Item.Properties().rarity(Rarity.EPIC)
                .food(new FoodProperties.Builder().nutrition(0).saturationMod(0f).alwaysEat().build()));
        this.life = life;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack s, Level l, LivingEntity e) {
        if (!l.isClientSide && e instanceof ServerPlayer p) {
            if (!p.hasPermissions(2)) {
                p.sendSystemMessage(Component.literal("Solo los administradores pueden usar este corazón."));
                return s;
            }
            if (life) {
                int v = Life.lives(p);
                if (v >= 2) {
                    p.sendSystemMessage(Component.literal("Ya tenés las vidas completas."));
                    return s;
                }
                Life.setLives(p, v + 1);
            } else {
                if (!Life.addHeart(p)) {
                    p.sendSystemMessage(Component.literal("Ya tenés el máximo de 25 corazones."));
                    return s;
                }
            }
        }
        return super.finishUsingItem(s, l, e);
    }
}
