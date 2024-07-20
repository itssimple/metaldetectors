package se.itssimple.metaldetectors.forge.events;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.itssimple.metaldetectors.MetalDetectors;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber
public class ForgeMetalDetectorEvent {
    final int TICKS_PER_SECOND = 20;
    final float SECONDS = 2.5f;

    final float EVENT_TICKS = TICKS_PER_SECOND * SECONDS;

    public static final Logger LOG = LoggerFactory.getLogger(MetalDetectors.MODID);

    private static final TagKey<Block> DETECTABLE_BLOCKS = BlockTags.create(new ResourceLocation(MetalDetectors.MODID, "detectable_blocks"));

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent e) {
        var player = e.player;
        var level = player.level();

        if(level.isClientSide && e.phase.equals(TickEvent.Phase.START)
                && e.side.isClient() && e.player.tickCount % EVENT_TICKS == 0) {

            var mainHandItem = player.getMainHandItem();
            var offHandItem = player.getOffhandItem();

            if(mainHandItem.getItem() == MetalDetectors.SIMPLE_METAL_DETECTOR.get()
            || offHandItem.getItem() == MetalDetectors.SIMPLE_METAL_DETECTOR.get()) {
                checkSurroundingBlocks(level, player, 10);
            }
        }
    }

    public void checkSurroundingBlocks(Level level, Player player, int radius) {
        var pos = player.blockPosition();

        ArrayList<BlockState> foundBlocks = new ArrayList<>();

        BlockPos.betweenClosedStream(
            pos.getX() - radius, pos.getY() - radius, pos.getZ() - radius,
            pos.getX() + radius, pos.getY() + radius, pos.getZ() + radius)
            .filter(blockPos -> blockPos.distSqr(pos) <= radius * radius)
            .forEach(blockPos -> {
                var blockState = level.getBlockState(blockPos);
                var block = blockState.getBlock();
                // Check if the block is something we're looking for
                if(blockState.is(DETECTABLE_BLOCKS) || (block.getName().toString().contains("_ore'") || block.getName().toString().contains("_ore_"))) {
                    var blockTags = Objects.requireNonNull(ForgeRegistries.BLOCKS.tags()).getReverseTag(block).map(tags -> tags.getTagKeys().collect(Collectors.toSet())).orElse(Set.of());
                    if(!blockTags.isEmpty()) {
                        /*String tags = blockTags.stream()
                                .map(TagKey::location)
                                .map(ResourceLocation::toString)
                                .collect(Collectors.joining(", "));
                        LOG.debug("Block at {} is {} with tags {}", blockPos, block.getDescriptionId(), tags);*/
                        foundBlocks.add(blockState);
                    }
                }
            });

        if(!foundBlocks.isEmpty()) {
            level.playSound(player, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0f, 1.0f);
        }
    }
}
