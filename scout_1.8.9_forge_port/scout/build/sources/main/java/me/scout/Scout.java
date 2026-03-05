package me.scout;

import me.scout.config.ScoutConfig;
import me.scout.keybind.KeybindHandler;
import me.scout.modules.JumpCircles;
import me.scout.modules.TargetHud;
import me.scout.modules.Trails;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod(modid = Scout.MODID, version = Scout.VERSION, name = Scout.NAME,
     clientSideOnly = true, acceptedMinecraftVersions = "[1.8.9]")
public class Scout {

    public static final String MODID = "scout", VERSION = "1.0.0", NAME = "Scout";

    @Mod.Instance
    public static Scout instance;

    public static ScoutConfig config = new ScoutConfig();

    private final TargetHud      targetHud      = new TargetHud();
    private final Trails         trails         = new Trails();
    private final JumpCircles    jumpCircles    = new JumpCircles();
    private final KeybindHandler keybindHandler = new KeybindHandler();

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) { KeybindHandler.register(); }

    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {
        MinecraftForge.EVENT_BUS.register(targetHud);
        MinecraftForge.EVENT_BUS.register(trails);
        MinecraftForge.EVENT_BUS.register(jumpCircles);
        MinecraftForge.EVENT_BUS.register(keybindHandler);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Trails.onTick();
        JumpCircles.onTick();
    }
}
