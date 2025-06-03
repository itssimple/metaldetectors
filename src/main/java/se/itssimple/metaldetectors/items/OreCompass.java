package se.itssimple.metaldetectors.items;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class OreCompass extends Item {
    public OreCompass(Properties properties) {
        super(properties);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void inventoryTick(ItemStack stack, Level level, Player player, int slot, boolean selected) {
        if (level.isClientSide && (selected || player.getOffhandItem().getItem() == this)) {
            ItemStack offhand = player.getOffhandItem();
            if (!offhand.isEmpty() && offhand.getItem() != this) {
                Block targetOre = getOreBlockFromItem(offhand.getItem());
                if (targetOre != null) {
                    BlockPos nearest = findNearestOre(level, player.blockPosition(), targetOre, 48);
                    if (nearest != null) {
                        // TODO: Set compass angle to point to nearest
                        // This requires custom rendering or data syncing
                    }
                }
            }
        }
        super.inventoryTick(stack, level, player, slot, selected);
    }

    private Block getOreBlockFromItem(Item item) {
        // Simple mapping for vanilla ores, expand as needed
        if (item == Items.IRON_INGOT) return net.minecraft.world.level.block.Blocks.IRON_ORE;
        if (item == Items.GOLD_INGOT) return net.minecraft.world.level.block.Blocks.GOLD_ORE;
        if (item == Items.COPPER_INGOT) return net.minecraft.world.level.block.Blocks.COPPER_ORE;
        if (item == Items.DIAMOND) return net.minecraft.world.level.block.Blocks.DIAMOND_ORE;
        if (item == Items.EMERALD) return net.minecraft.world.level.block.Blocks.EMERALD_ORE;
        if (item == Items.REDSTONE) return net.minecraft.world.level.block.Blocks.REDSTONE_ORE;
        if (item == Items.LAPIS_LAZULI) return net.minecraft.world.level.block.Blocks.LAPIS_ORE;
        if (item == Items.COAL) return net.minecraft.world.level.block.Blocks.COAL_ORE;
        // Add more mappings as needed
        return null;
    }

    private BlockPos findNearestOre(Level level, BlockPos center, Block oreBlock, int radius) {
        BlockPos nearest = null;
        double minDist = Double.MAX_VALUE;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    if (state.getBlock() == oreBlock) {
                        double dist = pos.distSqr(center);
                        if (dist < minDist) {
                            minDist = dist;
                            nearest = pos;
                        }
                    }
                }
            }
        }
        return nearest;
    }
}
