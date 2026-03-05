package scout.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import scout.config.ScoutConfig;
import scout.events.EventManager;
import scout.modules.ModuleManager;

@Mod(modid = ScoutClient.MODID, name = ScoutClient.NAME, version = ScoutClient.VERSION,
        clientSideOnly = true, acceptedMinecraftVersions = "[1.8.9]")
public class ScoutClient {

    public static final String MODID   = "scoutclient";
    public static final String NAME    = "ScoutClient";
    public static final String VERSION = "1.0.0";

    public static final Minecraft mc = Minecraft.getMinecraft();

    @Mod.Instance
    public static ScoutClient INSTANCE;

    public static ScoutConfig config;
    public static ModuleManager modules;
    public static EventManager   events;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        config  = new ScoutConfig();
        modules = new ModuleManager();
        events  = new EventManager();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(events);
        modules.registerAll();
        System.out.println("[ScoutClient] Loaded successfully.");
    }
}
