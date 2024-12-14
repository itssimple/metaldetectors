package se.itssimple.metaldetectors.events;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import se.itssimple.metaldetectors.MetalDetectors;

@Mod.EventBusSubscriber
public class MinersHelmetEvent {
    private static BlockPos lastLightPos = null;

    private static final int LIGHT_RADIUS = 20;
    private static final double MIN_MOVE_DISTANCE = 1.0;

    @SubscribeEvent
    public static void onLivingTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        Level world = player.level();
        BlockPos pos = player.blockPosition();

        if (event.phase.equals(TickEvent.Phase.START)
                && event.player.tickCount % 20 == 0) {

            // Check if the player is wearing the custom torch helmet
            if (player.getItemBySlot(EquipmentSlot.HEAD).getItem() == MetalDetectors.MINERS_HELMET.get()) {
                // Add light around the player
                if (lastLightPos == null || lastLightPos.distSqr(pos) > MIN_MOVE_DISTANCE * MIN_MOVE_DISTANCE) {
                    if (lastLightPos != null) {
                        cleanUpLightBlocks(world, lastLightPos);
                    }

                    BlockState blockUnderPlayer = world.getBlockState(pos);
                    if (blockUnderPlayer.getBlock() != Blocks.LIGHT) {
                        world.setBlockAndUpdate(pos, Blocks.LIGHT.defaultBlockState());
                    }

                    lastLightPos = pos;
                }
            } else {
                // Remove the light block if it's still there after taking off the helmet
                BlockState blockUnderPlayer = world.getBlockState(pos);
                if (blockUnderPlayer.getBlock() == Blocks.LIGHT) {
                    world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                }
                if (lastLightPos != null) {
                    cleanUpLightBlocks(world, lastLightPos);
                    lastLightPos = null;
                }
            }
        }
    }

    private static void cleanUpLightBlocks(Level world, BlockPos centerPos) {
        BlockPos.betweenClosedStream(
                        centerPos.getX() - LIGHT_RADIUS, centerPos.getY() - LIGHT_RADIUS, centerPos.getZ() - LIGHT_RADIUS,
                        centerPos.getX() + LIGHT_RADIUS, centerPos.getY() + LIGHT_RADIUS, centerPos.getZ() + LIGHT_RADIUS)
                .filter(blockPos -> blockPos != centerPos && blockPos.distSqr(centerPos) <= LIGHT_RADIUS * LIGHT_RADIUS && world.getBlockState(blockPos).getBlock() == Blocks.LIGHT)
                .forEach(blockPos -> world.setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState()));
    }
}
