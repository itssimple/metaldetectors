package se.itssimple.metaldetectors;

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
import se.itssimple.metaldetectors.forge.events.ForgeMetalDetectorEvent;
import net.minecraftforge.registries.ForgeRegistries;

@Mod(MetalDetectors.MODID)
@Mod.EventBusSubscriber(modid = MetalDetectors.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MetalDetectors
{
    public static final String MODID = "metaldetectors";

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MetalDetectors.MODID);
    public static final RegistryObject<Item> SIMPLE_METAL_DETECTOR = ITEMS.register("simple_metal_detector", () ->
            new SimpleMetalDetector(new Item.Properties()
                    .stacksTo(1)));

    public MetalDetectors()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ITEMS.register(modEventBus);
        modEventBus.addListener(this::loadComplete);
    }

    private void loadComplete(final FMLLoadCompleteEvent event)
    {
        MinecraftForge.EVENT_BUS.register(new ForgeMetalDetectorEvent());
    }

    @SubscribeEvent
    public static void buildContents(BuildCreativeModeTabContentsEvent event) {
        if(event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(SIMPLE_METAL_DETECTOR);
        }
    }
}
