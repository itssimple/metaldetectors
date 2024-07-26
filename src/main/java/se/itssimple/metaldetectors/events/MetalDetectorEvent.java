package se.itssimple.metaldetectors.events;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
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
import se.itssimple.metaldetectors.items.WoodenDetector;

import java.util.*;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber
public class MetalDetectorEvent {
    final int TICKS_PER_SECOND = 20;

    public static final Logger LOG = LoggerFactory.getLogger(MetalDetectors.MODID);

    private static final TagKey<Block> DETECTABLE_BLOCKS = BlockTags.create(new ResourceLocation(MetalDetectors.MODID, "detectable_blocks"));

    private static Item WOODEN_DETECTOR;
    private static Item STONE_DETECTOR;
    private static Item IRON_DETECTOR;
    private static Item GOLD_DETECTOR;
    private static Item DIAMOND_DETECTOR;
    private static Item NETHERITE_DETECTOR;

    public MetalDetectorEvent() {
        WOODEN_DETECTOR = MetalDetectors.WOODEN_DETECTOR.get();
        STONE_DETECTOR = MetalDetectors.STONE_DETECTOR.get();
        IRON_DETECTOR = MetalDetectors.IRON_DETECTOR.get();
        GOLD_DETECTOR = MetalDetectors.GOLD_DETECTOR.get();
        DIAMOND_DETECTOR = MetalDetectors.DIAMOND_DETECTOR.get();
        NETHERITE_DETECTOR = MetalDetectors.NETHERITE_DETECTOR.get();
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent e) {
        var player = e.player;
        var level = player.level();

        if(level.isClientSide && e.phase.equals(TickEvent.Phase.START)
                && e.player.tickCount % 10 == 0) {

            var mainHandItem = player.getMainHandItem().getItem();
            var offHandItem = player.getOffhandItem().getItem();

            var item = getDetectionItem(mainHandItem, offHandItem);

            if(item != null) {
                checkSurroundingBlocks(level, player, item);
            }
        }
    }

    public Item getDetectionItem(Item mainHandItem, Item offHandItem) {
        if(mainHandItem == WOODEN_DETECTOR || offHandItem == WOODEN_DETECTOR) {
            return WOODEN_DETECTOR;
        } else if(mainHandItem == STONE_DETECTOR || offHandItem == STONE_DETECTOR) {
            return STONE_DETECTOR;
        } else if(mainHandItem == IRON_DETECTOR || offHandItem == IRON_DETECTOR) {
            return IRON_DETECTOR;
        } else if(mainHandItem == GOLD_DETECTOR || offHandItem == GOLD_DETECTOR) {
            return GOLD_DETECTOR;
        } else if(mainHandItem == DIAMOND_DETECTOR || offHandItem == DIAMOND_DETECTOR) {
            return DIAMOND_DETECTOR;
        } else if(mainHandItem == NETHERITE_DETECTOR || offHandItem == NETHERITE_DETECTOR) {
            return NETHERITE_DETECTOR;
        }

        return null;
    }

    public void checkSurroundingBlocks(Level level, Player player, Item detectionItem) {
        var toolData = getToolData(detectionItem);

        if(toolData == null) return;

        if(player.tickCount % toolData.tickSpeed != 0) {
            return;
        }

        var radius = toolData.radius;
        var pos = player.blockPosition();

        //LOG.debug("Using {} to detect blocks, have radius {}, tickspeed is {} seconds", detectionItem.getDescriptionId(), radius, tickSpeed / 20f);

        Map<BlockPos, BlockState> foundBlocks = new HashMap<>();

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
                        foundBlocks.put(blockPos, blockState);
                    }
                }
            });

        if(!foundBlocks.isEmpty()) {
            LOG.debug("Found {} blocks of interest with {}", foundBlocks.size(), detectionItem.getDescriptionId());
            level.playSound(player, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0f, 1.0f);

            var closestBlock = foundBlocks.keySet().stream().min(Comparator.comparingDouble(entry -> entry.distSqr(pos)));

            if(closestBlock.isPresent())
            {
                var blockPos = closestBlock.get();
                var blockState = level.getBlockState(blockPos);
                var block = blockState.getBlock();
                var blockTags = Objects.requireNonNull(ForgeRegistries.BLOCKS.tags()).getReverseTag(block).map(tags -> tags.getTagKeys().collect(Collectors.toSet())).orElse(Set.of());
                String tags = blockTags.stream()
                        .map(TagKey::location)
                        .map(ResourceLocation::toString)
                        .collect(Collectors.joining(", "));

                //LOG.debug("Block at {} is {} with tags {}", blockPos, block.getDescriptionId(), tags);
            }

            if(toolData.emitParticles) {
                LOG.debug("Emitting particles randomly");
            }

            if(toolData.emitParticlesTowardsNearest) {
                LOG.debug("Emitting particles towards nearest block");
            }

            if(toolData.makeItemsGlow) {
                LOG.debug("Makes nearest block glow");
            }

            if(toolData.makesAllItemsGlow) {
                LOG.debug("Makes all items glow");
            }

            if(toolData.directionalTowardsNearest) {
                LOG.debug("Directional towards nearest block");
            }
        }
    }

    private ToolData getToolData(Item detectionItem)
    {
        if(detectionItem == WOODEN_DETECTOR) {
            return new ToolData(3,
                    5 * TICKS_PER_SECOND,
                    false,
                    false,
                    false,
                    false,
                    false
            );
        } else if(detectionItem == STONE_DETECTOR) {
            return new ToolData(5,
                    4 * TICKS_PER_SECOND,
                    true,
                    false,
                    false,
                    false,
                    false
            );
        } else if(detectionItem == IRON_DETECTOR) {
            return new ToolData(7,
                    3 * TICKS_PER_SECOND,
                    true,
                    true,
                    false,
                    false,
                    false
            );
        } else if(detectionItem == GOLD_DETECTOR) {
            return new ToolData(6,
                    3 * TICKS_PER_SECOND,
                    true,
                    false,
                    false,
                    true,
                    false
            );
        } else if(detectionItem == DIAMOND_DETECTOR) {
            return new ToolData(10,
                    2 * TICKS_PER_SECOND,
                    true,
                    true,
                    false,
                    false,
                    true
            );
        } else if(detectionItem == NETHERITE_DETECTOR) {
            return new ToolData(12,
                    TICKS_PER_SECOND,
                    true,
                    true,
                    true,
                    true,
                    true
            );
        }

        return null;
    }

    private static class ToolData {
        public final int radius;
        public final int tickSpeed;
        public final boolean emitParticles;
        public final boolean emitParticlesTowardsNearest;
        public final boolean makeItemsGlow;
        public final boolean makesAllItemsGlow;
        public final boolean directionalTowardsNearest;

        public ToolData(
                int radius,
                int tickSpeed,
                boolean emitParticles,
                boolean emitParticlesTowardsNearest,
                boolean makeItemsGlow,
                boolean makeAllItemsGlow,
                boolean directionalTowardsNearest) {
            this.radius = radius;
            this.tickSpeed = tickSpeed;
            this.emitParticles = emitParticles;
            this.emitParticlesTowardsNearest = emitParticlesTowardsNearest;
            this.makeItemsGlow = makeItemsGlow;
            this.makesAllItemsGlow = makeAllItemsGlow;
            this.directionalTowardsNearest = directionalTowardsNearest;
        }
    }
}
