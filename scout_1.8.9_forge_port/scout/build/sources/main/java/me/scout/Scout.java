package me.scout;

import me.scout.config.ScoutConfig;
import me.scout.modules.TargetHud;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod(modid = Scout.MODID, name = Scout.NAME, version = Scout.VERSION, clientSideOnly = true)
public class Scout {

    public static final String MODID = "scout";
    public static final String NAME = "Scout";
    public static final String VERSION = "1.0.0";

    public static final Minecraft mc = Minecraft.getMinecraft();
    public static ScoutConfig config = new ScoutConfig();

    @Mod.Instance
    public static Scout instance;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        config = new ScoutConfig();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new TargetHud());
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            if (mc.theWorld != null && mc.thePlayer != null) {
                TargetHud.onTick();
            }
        }
    }
}
