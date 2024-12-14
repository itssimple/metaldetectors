package se.itssimple.metaldetectors;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import se.itssimple.metaldetectors.items.*;

@Mod(MetalDetectors.MODID)
@Mod.EventBusSubscriber(modid = MetalDetectors.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MetalDetectors
{
    public static final String MODID = "metaldetectors";

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MetalDetectors.MODID);

    public static final RegistryObject<Item> WOODEN_DETECTOR = ITEMS.register("wooden_detector", () ->
            new WoodenDetector(new Item.Properties()
                    .stacksTo(1)));

    public static final RegistryObject<Item> STONE_DETECTOR = ITEMS.register("stone_detector", () ->
            new StoneDetector(new Item.Properties()
                    .stacksTo(1)));

    public static final RegistryObject<Item> IRON_DETECTOR = ITEMS.register("iron_detector", () ->
            new IronDetector(new Item.Properties()
                    .stacksTo(1)));

    public static final RegistryObject<Item> GOLD_DETECTOR = ITEMS.register("gold_detector", () ->
            new GoldDetector(new Item.Properties()
                    .stacksTo(1)
                    .setNoRepair()
                    .durability(200)));

    public static final RegistryObject<Item> DIAMOND_DETECTOR = ITEMS.register("diamond_detector", () ->
            new DiamondDetector(new Item.Properties()
                    .stacksTo(1)));

    public static final RegistryObject<Item> NETHERITE_DETECTOR = ITEMS.register("netherite_detector", () ->
            new NetheriteDetector(new Item.Properties()
                    .stacksTo(1)));

    public static final RegistryObject<Item> MINERS_HELMET = ITEMS.register("miners_helmet", () ->
            new MinersHelmet(ArmorMaterials.IRON, ArmorItem.Type.HELMET, new Item.Properties()
                    .stacksTo(1)));

    public MetalDetectors()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ITEMS.register(modEventBus);
        modEventBus.addListener(this::loadComplete);
    }

    private void loadComplete(final FMLLoadCompleteEvent event)
    {
        MinecraftForge.EVENT_BUS.register(new se.itssimple.metaldetectors.events.MetalDetectorEvent());
        MinecraftForge.EVENT_BUS.register(new se.itssimple.metaldetectors.events.MinersHelmetEvent());
    }

    @SubscribeEvent
    public static void buildContents(BuildCreativeModeTabContentsEvent event) {
        if(event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(WOODEN_DETECTOR);
            event.accept(STONE_DETECTOR);
            event.accept(IRON_DETECTOR);
            event.accept(GOLD_DETECTOR);
            event.accept(DIAMOND_DETECTOR);
            event.accept(NETHERITE_DETECTOR);
        }

        if(event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(MINERS_HELMET);
        }
    }
}
