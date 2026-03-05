package me.scout.gui;

import me.scout.Scout;
import me.scout.config.ScoutConfig;
import me.scout.modules.JumpCircles;
import me.scout.modules.TargetHud;
import me.scout.modules.Trails;
import net.minecraft.client.gui.*;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ScoutGui extends GuiScreen {

    private static final int W = 320, H = 220, TAB_H = 22;
    private static final int BTN_W = 130, BTN_H = 18;
    private static final int COL1_X = 16, COL2_X = 164;
    private static final int ROW_START = TAB_H + 14, ROW_STEP = 22;

    private int guiX, guiY;
    private enum Tab { TARGET_HUD, TRAILS, JUMP_CIRCLES, APPEARANCE }
    private Tab activeTab = Tab.TARGET_HUD;
    private final List<GuiButton> tabButtons = new ArrayList<>();

    @Override
    public void initGui() {
        guiX = (width - W) / 2;
        guiY = (height - H) / 2;
        buttonList.clear();
        tabButtons.clear();
        String[] tabNames = {"TargetHud", "Trails", "JumpCircles", "Appearance"};
        int tabW = W / tabNames.length;
        for (int i = 0; i < tabNames.length; i++) {
            GuiButton btn = new GuiButton(200 + i, guiX + i * tabW, guiY, tabW, TAB_H, tabNames[i]);
            tabButtons.add(btn);
            buttonList.add(btn);
        }
        rebuildButtons();
    }

    private void rebuildButtons() {
        buttonList.removeIf(b -> b.id < 200);
        int x1 = guiX + COL1_X, x2 = guiX + COL2_X;
        int y = guiY + ROW_START;
        switch (activeTab) {
            case TARGET_HUD:
                addToggle(0, x1, y,            "TargetHud",   Scout.config.targetHudEnabled);
                addCycle (1, x2, y,            "Style: ",     TargetHud.Style.values(), Scout.config.targetHudStyle.ordinal());
                addToggle(2, x1, y+ROW_STEP,   "Blur Shadow", Scout.config.blurShadowEnabled);
                addToggle(3, x2, y+ROW_STEP,   "Hide HP Bar", Scout.config.hideHPBar);
                addToggle(4, x1, y+ROW_STEP*2, "Hide Items",  Scout.config.hideItemOverlay);
                addToggle(5, x2, y+ROW_STEP*2, "Follow Cam",  Scout.config.targetHudFollow);
                break;
            case TRAILS:
                addToggle(0, x1, y,            "Trails",      Scout.config.trailsEnabled);
                addCycle (1, x2, y,            "Style: ",     Trails.Style.values(), Scout.config.trailsStyle.ordinal());
                addToggle(2, x1, y+ROW_STEP,   "Render Half", Scout.config.trailsRenderHalf);
                addToggle(3, x2, y+ROW_STEP,   "1st Person",  Scout.config.trailsFirstPerson);
                addToggle(4, x1, y+ROW_STEP*2, "For Mobs",    Scout.config.trailsForGliders);
                break;
            case JUMP_CIRCLES:
                addToggle(0, x1, y,          "JumpCircles", Scout.config.jumpCirclesEnabled);
                addCycle (1, x2, y,          "Style: ",     JumpCircles.Style.values(), Scout.config.jumpCirclesStyle.ordinal());
                addToggle(2, x1, y+ROW_STEP, "Fade Out",    Scout.config.jumpCirclesFadeOut);
                break;
            case APPEARANCE:
                addCycle(0, x1, y, "Palette: ", ScoutConfig.PaletteStyle.values(), Scout.config.paletteStyle.ordinal());
                break;
        }
    }

    private void addToggle(int id, int x, int y, String label, boolean value) {
        buttonList.add(new GuiButton(id, x, y, BTN_W, BTN_H, label + ": " + (value ? "§aON" : "§cOFF")));
    }

    private void addCycle(int id, int x, int y, String prefix, Object[] values, int current) {
        buttonList.add(new GuiButton(id, x, y, BTN_W, BTN_H, prefix + "§e" + values[current].toString()));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        for (int i = 0; i < tabButtons.size(); i++) {
            if (button == tabButtons.get(i)) { activeTab = Tab.values()[i]; rebuildButtons(); return; }
        }
        switch (activeTab) {
            case TARGET_HUD:   handleTargetHud(button);   break;
            case TRAILS:       handleTrails(button);       break;
            case JUMP_CIRCLES: handleJumpCircles(button);  break;
            case APPEARANCE:   handleAppearance(button);   break;
        }
        rebuildButtons();
    }

    private void handleTargetHud(GuiButton b) {
        switch (b.id) {
            case 0: Scout.config.targetHudEnabled  = !Scout.config.targetHudEnabled;  break;
            case 1: Scout.config.targetHudStyle     = cycleEnum(TargetHud.Style.values(),  Scout.config.targetHudStyle); break;
            case 2: Scout.config.blurShadowEnabled  = !Scout.config.blurShadowEnabled;  break;
            case 3: Scout.config.hideHPBar          = !Scout.config.hideHPBar;           break;
            case 4: Scout.config.hideItemOverlay    = !Scout.config.hideItemOverlay;     break;
            case 5: Scout.config.targetHudFollow    = !Scout.config.targetHudFollow;     break;
        }
    }

    private void handleTrails(GuiButton b) {
        switch (b.id) {
            case 0: Scout.config.trailsEnabled     = !Scout.config.trailsEnabled;     break;
            case 1: Scout.config.trailsStyle        = cycleEnum(Trails.Style.values(), Scout.config.trailsStyle); break;
            case 2: Scout.config.trailsRenderHalf  = !Scout.config.trailsRenderHalf;  break;
            case 3: Scout.config.trailsFirstPerson = !Scout.config.trailsFirstPerson; break;
            case 4: Scout.config.trailsForGliders  = !Scout.config.trailsForGliders;  break;
        }
    }

    private void handleJumpCircles(GuiButton b) {
        switch (b.id) {
            case 0: Scout.config.jumpCirclesEnabled = !Scout.config.jumpCirclesEnabled; break;
            case 1: Scout.config.jumpCirclesStyle    = cycleEnum(JumpCircles.Style.values(), Scout.config.jumpCirclesStyle); break;
            case 2: Scout.config.jumpCirclesFadeOut  = !Scout.config.jumpCirclesFadeOut; break;
        }
    }

    private void handleAppearance(GuiButton b) {
        if (b.id == 0) Scout.config.paletteStyle = cycleEnum(ScoutConfig.PaletteStyle.values(), Scout.config.paletteStyle);
    }

    private <T> T cycleEnum(T[] values, T current) {
        for (int i = 0; i < values.length; i++) {
            if (values[i] == current) return values[(i + 1) % values.length];
        }
        return values[0];
    }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        drawRect(0, 0, width, height, 0x88000000);
        drawRect(guiX, guiY, guiX + W, guiY + H, 0xE0101010);
        int tabW = W / 4;
        drawRect(guiX + activeTab.ordinal() * tabW, guiY, guiX + (activeTab.ordinal() + 1) * tabW, guiY + TAB_H, 0xFF1A1A2E);
        drawGradientRect(guiX, guiY + TAB_H, guiX + W, guiY + TAB_H + 2, 0xFF7B2FBE, 0xFF4A90E2);
        drawCenteredString(fontRendererObj, "§bScout §7v1.0", guiX + W / 2, guiY + H - 12, 0xFFFFFF);
        super.drawScreen(mx, my, pt);
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }

    @Override
    public void keyTyped(char c, int key) throws IOException {
        if (key == Keyboard.KEY_ESCAPE || key == Keyboard.KEY_F6) mc.displayGuiScreen(null);
    }
}
