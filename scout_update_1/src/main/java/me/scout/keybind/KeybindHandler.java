package me.scout.keybind;

import me.scout.gui.ScoutGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

public class KeybindHandler {

    public static final KeyBinding OPEN_GUI = new KeyBinding(
        "key.scout.open_gui",
        Keyboard.KEY_F6,
        "key.categories.scout"
    );

    public static void register() {
        ClientRegistry.registerKeyBinding(OPEN_GUI);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (OPEN_GUI.isPressed()) {
            Minecraft.getMinecraft().displayGuiScreen(new ScoutGui());
        }
    }
}
