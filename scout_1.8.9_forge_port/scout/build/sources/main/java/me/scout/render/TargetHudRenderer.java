package me.scout.render;

import me.scout.Scout;
import me.scout.modules.TargetHud;
import me.scout.particle.Particle2D;
import me.scout.utils.MathUtility;
import me.scout.utils.Palette;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * TargetHudRenderer — 1.8.9 Forge port of SoupAPI TargetHudRenderer.java.
 *
 * All five styles are implemented:
 *   MINI, NORMAL, ARES, ALT_1, TINY
 *
 * Rendering strategy differences from 1.21.4:
 *  - No DrawContext / MatrixStack → direct GL11 + GlStateManager calls
 *  - No RenderLayer, no Tessellator v3 API → use WorldRenderer / Tessellator 1.8.9
 *  - Player skin fetched via NetworkPlayerInfo.getLocationSkin()
 *  - Items drawn via itemRenderer
 *  - Text drawn via mc.fontRendererObj
 */
public class TargetHudRenderer {

    private static final Minecraft mc = Minecraft.getMinecraft();

    // ── Shared state (mirrors SoupAPI fields) ─────────────────────────────────
    public static final ArrayList<Particle2D> particles = new ArrayList<>();
    public static boolean sentParticles = false;
    public static float   ticks         = 0f;

    public static float hpColorAnimationProgress  = 0f;
    public static float colorAnimationProgress    = 0f;

    // Animated corner colors updated each frame by TargetHud.updateColors()
    public static Color topLeft     = Color.WHITE;
    public static Color topRight    = Color.WHITE;
    public static Color bottomLeft  = Color.WHITE;
    public static Color bottomRight = Color.WHITE;

    // ── MINI ──────────────────────────────────────────────────────────────────

    /**
     * MINI style — compact 95×35 card with rounded gradient border.
     * Mirrors SoupAPI renderMiniHUD.
     */
    public static void renderMiniHUD(float normalizedDelta, float health, float animationFactor,
                                     EntityPlayer target, int x, int y) {
        float hurtPct = getHurtPercent(target, normalizedDelta);
        Color c1 = Palette.getColor(0f);
        Color c3 = Palette.getColor(0.66f);

        int w = 95, h = 35;
        float r = 7f;

        // Gradient border glow
        RenderHelper2D.drawGlowShadow(x + 2, y + 2, 91, 31, 4, bottomLeft);

        // Gradient border quad
        RenderHelper2D.drawRoundedRectGradient(x, y, w, h, r, topLeft, topRight, bottomRight, bottomLeft);

        // Inner dark background
        RenderHelper2D.drawRoundedRect(x + 0.5f, y + 0.5f, w - 1, h - 1, r, Palette.getBackColor());

        // Player head
        String displayName = "Invisible";
        NetworkPlayerInfo info = getPlayerInfo(target);
        if (!target.isInvisible() && info != null) {
            displayName = target.getName();
        }

        GL11PushPop(() -> {
            float headCX = x + 2.5f + 15;
            float headCY = y + 2.5f + 15;
            org.lwjgl.opengl.GL11.glTranslatef(headCX, headCY, 0);
            org.lwjgl.opengl.GL11.glScalef(1 - hurtPct / 20f, 1 - hurtPct / 20f, 1f);
            org.lwjgl.opengl.GL11.glTranslatef(-headCX, -headCY, 0);
            RenderHelper2D.drawPlayerHead(info, x + 2.5f, y + 2.5f, 30, 5, hurtPct);
        });

        // Particles
        renderParticles(x, y, target, c1, c3, 1.0f);

        // HP bar
        Color hpLeft  = getHPBarLeft();
        Color hpRight = getHPBarRight();
        float barX = x + 38, barY = y + 25;
        float totalW = 52, barH = 7;
        float filledW = (float)MathUtility.clamp(totalW * (health / target.getMaxHealth()), 8, totalW);
        Color bgColor = c3.darker().darker();
        RenderHelper2D.drawHealthBar(barX, barY, totalW, filledW, barH, 2f, bgColor, hpLeft, hpRight);

        // Health text
        FontRenderer fr = mc.fontRendererObj;
        String hpText = String.valueOf(Math.round(10.0 * health) / 10.0);
        Color textCol = RenderHelper2D.applyOpacity(new Color(Palette.getTextColor(), true), animationFactor);
        fr.drawStringWithShadow(hpText, x + 65 - fr.getStringWidth(hpText) / 2f, y + 27, textCol.getRGB());
        fr.drawStringWithShadow(displayName, x + 38, y + 5, textCol.getRGB());

        // Items (scale 0.5)
        renderItems(target, x + 38, y + 13, 9f, 0.5f, animationFactor);
    }

    // ── NORMAL ────────────────────────────────────────────────────────────────

    /**
     * NORMAL style — 137×47.5 card.
     * Mirrors SoupAPI renderNormalHUD.
     */
    public static void renderNormalHUD(float normalizedDelta, float health, float animationFactor,
                                       EntityPlayer target, int x, int y) {
        float hurtPct = getHurtPercent(target, normalizedDelta);
        Color c1 = Palette.getColor(0f);
        Color c3 = Palette.getColor(0.66f);

        int w = 137; float h = 47.5f;
        float r = 9f;

        RenderHelper2D.drawGlowShadow(x + 2, y + 2, 133, 43, 4, bottomLeft);
        RenderHelper2D.drawRoundedRectGradient(x, y, w, h, r, topLeft, topRight, bottomRight, bottomLeft);
        RenderHelper2D.drawRoundedRect(x + 0.5f, y + 0.5f, w - 1, h - 1, r, Palette.getBackColor());

        String displayName = "Invisible";
        NetworkPlayerInfo info = getPlayerInfo(target);
        if (!target.isInvisible() && info != null) displayName = target.getName();

        GL11PushPop(() -> {
            float headCX = x + 3.5f + 20;
            float headCY = y + 3.5f + 20;
            org.lwjgl.opengl.GL11.glTranslatef(headCX, headCY, 0);
            org.lwjgl.opengl.GL11.glScalef(1 - hurtPct / 15f, 1 - hurtPct / 15f, 1f);
            org.lwjgl.opengl.GL11.glTranslatef(-headCX, -headCY, 0);
            RenderHelper2D.drawPlayerHead(info, x + 3.5f, y + 3.5f, 40, 7, hurtPct);
        });

        renderParticles(x, y, target, c1, c3, 1.0f);

        Color hpLeft  = getHPBarLeft();
        Color hpRight = getHPBarRight();
        float barX = x + 48, barY = y + 32;
        float totalW = 85, barH = 11;
        float filledW = (float)MathUtility.clamp(totalW * (health / target.getMaxHealth()), 8, totalW);
        RenderHelper2D.drawHealthBar(barX, barY, totalW, filledW, barH, 4f,
                c3.darker().darker(), hpLeft, hpRight);

        FontRenderer fr = mc.fontRendererObj;
        Color textCol = RenderHelper2D.applyOpacity(new Color(Palette.getTextColor(), true), animationFactor);
        String hpText = String.valueOf(Math.round(10.0 * health) / 10.0);
        fr.drawStringWithShadow(hpText, x + 92 - fr.getStringWidth(hpText) / 2f, y + 35, textCol.getRGB());
        fr.drawStringWithShadow(displayName, x + 48, y + 7, textCol.getRGB());

        renderItems(target, x + 48, y + 15, 12f, 0.75f, animationFactor);
    }

    // ── ARES ─────────────────────────────────────────────────────────────────

    /**
     * ARES style — dynamic width based on name, no gradient border, explicit HP text.
     * Mirrors SoupAPI renderAresHUD.
     */
    public static void renderAresHUD(float normalizedDelta, float health, float animationFactor,
                                     EntityPlayer target, int x, int y) {
        float hurtPct = getHurtPercent(target, normalizedDelta);
        Color c1 = Palette.getColor(0f);
        Color c3 = Palette.getColor(0.66f);

        String displayName = "Invisible";
        NetworkPlayerInfo info = getPlayerInfo(target);
        if (!target.isInvisible() && info != null) displayName = target.getName();

        FontRenderer fr = mc.fontRendererObj;
        // Bold-17 equivalent: in 1.8.9 we use the vanilla font renderer (slightly bigger via GL scale)
        int textWidth = fr.getStringWidth(displayName);
        int minWidth  = 120;
        int padding   = 50;
        int totalW    = Math.max(minWidth, textWidth + padding);
        int barW      = totalW - 52;
        int cardH     = 46;
        float r       = 7f;

        // Plain dark background (no gradient border for ARES)
        RenderHelper2D.drawRoundedRect(x + 0.5f, y + 0.5f, totalW, cardH, r, Palette.getBackColor());

        float headScale = 20f;
        GL11PushPop(() -> {
            float headCX = x + 3.5f + headScale;
            float headCY = y + 3.5f + headScale;
            org.lwjgl.opengl.GL11.glTranslatef(headCX, headCY, 0);
            org.lwjgl.opengl.GL11.glScalef(1 - hurtPct / 15f, 1 - hurtPct / 15f, 1f);
            org.lwjgl.opengl.GL11.glTranslatef(-headCX, -headCY, 0);
            RenderHelper2D.drawPlayerHead(info, x + 3.5f, y + 3.5f, headScale * 2, 5, hurtPct);
        });

        renderParticles(x, y, target, c1, c3, 1.0f);

        Color hpLeft  = getHPBarLeft();
        Color hpRight = getHPBarRight();
        int barX = x + 48, barY = y + 32, barH = 9;
        float filledW = (float)MathUtility.clamp(barW * (health / target.getMaxHealth()), 8, barW);
        Color bg = Palette.getBackColor().darker().darker().darker();
        RenderHelper2D.drawGlowShadow(barX, barY, (int)filledW, barH, 2, bottomLeft);
        RenderHelper2D.drawHealthBar(barX, barY, barW, filledW, barH, 2f, bg, hpLeft, hpRight);

        final Color textCol = RenderHelper2D.applyOpacity(new Color(Palette.getTextColor(), true), animationFactor);
        final String finalDisplayName = displayName;
        // Name (bold-17 sim: scale 1.1×)
        GL11PushPop(() -> {
            org.lwjgl.opengl.GL11.glTranslatef(x + 48, y + 7, 0);
            org.lwjgl.opengl.GL11.glScalef(1.1f, 1.1f, 1f);
            fr.drawStringWithShadow(finalDisplayName, 0, 0, textCol.getRGB());
        });
        // "HP: X.X"
        fr.drawStringWithShadow("HP: " + (Math.round(10.0 * health) / 10.0), x + 48, y + 20, textCol.getRGB());
    }

    // ── ALT_1 ─────────────────────────────────────────────────────────────────

    /**
     * ALT_1 style — compact 90×33 dark card, small head, name + thin HP bar + items.
     * Mirrors SoupAPI renderAlt_1_HUD.
     */
    public static void renderAlt1HUD(float normalizedDelta, float health, float animationFactor,
                                     EntityPlayer target, int x, int y) {
        float hurtPct = getHurtPercent(target, normalizedDelta);
        Color c1 = topLeft;
        Color c3 = bottomRight;
        Color c4 = bottomLeft;

        int w = 90, h = 33;
        float r = 3f;
        int xOff = -5;

        RenderHelper2D.drawRoundedRect(x + xOff, y + 0.5f, w, h, r, Palette.getBackColor());

        String displayName = "Invisible";
        NetworkPlayerInfo info = getPlayerInfo(target);
        if (!target.isInvisible() && info != null) displayName = target.getName();

        int headScale = 20;
        GL11PushPop(() -> {
            float headCX = x + 2.5f + 15;
            float headCY = y + 2.5f + 15;
            org.lwjgl.opengl.GL11.glTranslatef(headCX, headCY, 0);
            org.lwjgl.opengl.GL11.glScalef(1 - hurtPct / 20f, 1 - hurtPct / 20f, 1f);
            org.lwjgl.opengl.GL11.glTranslatef(-headCX, -headCY, 0);
            RenderHelper2D.drawPlayerHead(info, x - 2, y + 2.5f, headScale, r, hurtPct);
        });

        renderParticles(x, y, target, c1, c3, 1.0f);

        // Thin HP bar (h=2)
        int barX = x - 2, barY = y + 27;
        float totalW = 84, barH = 2;
        float filledW = (float)MathUtility.clamp(totalW * (health / target.getMaxHealth()), 8, totalW);
        Color bgColor = new Color(0x424242);
        RenderHelper2D.drawGlowShadow(barX, barY, (int)filledW, (int)barH, 2, bottomLeft);
        RenderHelper2D.drawHealthBar(barX, barY, totalW, filledW, barH, 1f, bgColor, c4, c3);

        // Name (bold-12 sim: scale 0.85×)
        final Color textCol2 = RenderHelper2D.applyOpacity(new Color(Palette.getTextColor(), true), animationFactor);
        final String finalDisplayName2 = displayName;
        GL11PushPop(() -> {
            org.lwjgl.opengl.GL11.glTranslatef(x + 20, y + 6, 0);
            org.lwjgl.opengl.GL11.glScalef(0.85f, 0.85f, 1f);
            mc.fontRendererObj.drawStringWithShadow(finalDisplayName2, 0, 0, textCol2.getRGB());
        });

        renderItems(target, x + 20, y + 12, 10f, 0.5f, animationFactor);
    }

    // ── TINY ──────────────────────────────────────────────────────────────────

    /**
     * TINY style — minimal 90×29 dark chip with head + thin bar + items + effects.
     * Mirrors SoupAPI renderTinyHUD.
     */
    public static void renderTinyHUD(float normalizedDelta, float health, float animationFactor,
                                     EntityPlayer target, int x, int y) {
        float hurtPct = getHurtPercent(target, normalizedDelta);
        Color c1 = topLeft;
        Color c3 = bottomRight;
        Color c4 = bottomLeft;

        int w = 90, h = 29;
        float r = 3f;
        int xOff = -4;

        RenderHelper2D.drawRoundedRect(x + xOff, y + 0.5f, w, h, r, Palette.getBackColor());

        NetworkPlayerInfo info = getPlayerInfo(target);
        int headScale = 25;

        GL11PushPop(() -> {
            float headCX = x + 2.5f + 15;
            float headCY = y + 2.5f + 15;
            org.lwjgl.opengl.GL11.glTranslatef(headCX, headCY, 0);
            org.lwjgl.opengl.GL11.glScalef(1 - hurtPct / 20f, 1 - hurtPct / 20f, 1f);
            org.lwjgl.opengl.GL11.glTranslatef(-headCX, -headCY, 0);
            RenderHelper2D.drawPlayerHead(info, x - 2, y + 2.5f, headScale, r, hurtPct);
        });

        renderParticles(x, y, target, c1, c3, 1.0f);

        // HP bar (h=2)
        int barX = x + 25, barY = y + 15;
        float totalW = 59, barH = 2;
        float filledW = (float)MathUtility.clamp(totalW * (health / target.getMaxHealth()), 8, totalW);
        Color bgColor = new Color(0x424242);
        RenderHelper2D.drawGlowShadow(barX, barY, (int)filledW, (int)barH, 2, bottomLeft);
        RenderHelper2D.drawHealthBar(barX, barY, totalW, filledW, barH, 2f, bgColor, c4, c3);

        // Items
        renderItems(target, x + 25, y + 4, 10f, 0.5f, animationFactor);

        // Active potion effects (tiny icons, scale 0.5)
        renderEffectIcons(target, x + 25, y + 18, animationFactor);
    }

    // ── Shared helpers ────────────────────────────────────────────────────────

    private static void renderParticles(int x, int y, EntityPlayer target,
                                        Color c1, Color c3, float depthFactor) {
        for (Particle2D p : new ArrayList<>(particles)) {
            if (p.opacity > 4) {
                p.render2D(TargetHud.smoothedScreenX, TargetHud.smoothedScreenY, depthFactor);
            }
        }
        if (target.hurtTime == 9 && !sentParticles) {
            for (int i = 0; i <= 6; i++) {
                Particle2D p = new Particle2D();
                Color c = Particle2D.mixColors(c1, c3,
                        (float)((Math.sin(ticks + x * 0.4f + i) + 1) * 0.5));
                p.init(x - TargetHud.smoothedScreenX, y - TargetHud.smoothedScreenY,
                        MathUtility.random(-3f, 3f), MathUtility.random(-3f, 3f),
                        20, c, Scout.config.targetHudFollow);
                particles.add(p);
            }
            sentParticles = true;
        }
        if (target.hurtTime == 8) sentParticles = false;
    }

    private static void renderItems(EntityPlayer target,
                                    float startX, float y,
                                    float gap, float scale,
                                    float animationFactor) {
        ItemStack[] armor = target.inventory.armorInventory;  // [0]=boots [3]=helmet
        ItemStack[] items = {
            target.getHeldItem(),       // main hand
            armor[3],               // helmet
            armor[2],               // chestplate
            armor[1],               // leggings
            armor[0],               // boots
            // 1.8.9 has no offhand, omit or leave null
        };

        GlStateManager.pushMatrix();
        float xPos = startX;
        for (ItemStack stack : items) {
            if (stack == null) { xPos += gap; continue; }
            GlStateManager.pushMatrix();
            GlStateManager.translate(xPos, y, 0);
            GlStateManager.scale(scale, scale, scale);
            GlStateManager.color(1f, 1f, 1f, animationFactor);
            mc.getRenderItem().renderItemIntoGUI(stack, 0, 0);
            mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRendererObj, stack, 0, 0, null);
            GlStateManager.popMatrix();
            xPos += gap;
        }
        GlStateManager.popMatrix();
        GlStateManager.color(1f, 1f, 1f, 1f);
    }

    private static void renderEffectIcons(EntityPlayer target, float startX, float y, float animationFactor) {
        // 1.8.9: iterate active potion effects and draw their icon textures
        GlStateManager.enableBlend();
        float xPos = startX;
        for (net.minecraft.potion.PotionEffect effect : target.getActivePotionEffects()) {
            net.minecraft.potion.Potion potion = net.minecraft.potion.Potion.potionTypes[effect.getPotionID()];
            if (potion == null || !potion.hasStatusIcon()) continue;
            ResourceLocation icons = new ResourceLocation("textures/gui/container/inventory.png");
            mc.getTextureManager().bindTexture(icons);
            int iconIndex = potion.getStatusIconIndex();
            int iconU = (iconIndex % 8) * 18;
            int iconV = 198 + (iconIndex / 8) * 18;
            GlStateManager.pushMatrix();
            GlStateManager.translate(xPos, y, 0);
            GlStateManager.scale(0.5f, 0.5f, 0.5f);
            GlStateManager.color(1f, 1f, 1f, animationFactor);
            RenderHelper2D.drawTexturedRect(0, 0, 18, 18, iconU, iconV, 18, 18, 256, 256);
            GlStateManager.popMatrix();
            xPos += 10;
        }
        GlStateManager.disableBlend();
        GlStateManager.color(1f, 1f, 1f, 1f);
    }

    private static float getHurtPercent(EntityPlayer target, float normalizedDelta) {
        float raw = target.hurtTime == 0 ? 0 : Math.min(target.hurtTime + 1, 10);
        return raw / 8f;
    }

    private static NetworkPlayerInfo getPlayerInfo(EntityPlayer target) {
        if (!(target instanceof AbstractClientPlayer)) return null;
        return mc.getNetHandler().getPlayerInfo(target.getUniqueID());
    }

    /** Animated HP bar colors cycling between palette c1 and c3. */
    private static Color getHPBarLeft() {
        float p = hpColorAnimationProgress % 1.0f;
        Color c1 = Palette.getColor(0f), c3 = Palette.getColor(0.66f);
        if (p < 0.5f) return RenderHelper2D.interpolateColor(c1, c3, p / 0.5f);
        return RenderHelper2D.interpolateColor(c3, c1, (p - 0.5f) / 0.5f);
    }

    private static Color getHPBarRight() {
        float p = hpColorAnimationProgress % 1.0f;
        Color c1 = Palette.getColor(0f), c3 = Palette.getColor(0.66f);
        if (p < 0.5f) return RenderHelper2D.interpolateColor(c3, c1, p / 0.5f);
        return RenderHelper2D.interpolateColor(c1, c3, (p - 0.5f) / 0.5f);
    }

    // ── GL helper ─────────────────────────────────────────────────────────────

    private static void GL11PushPop(Runnable body) {
        GlStateManager.pushMatrix();
        try { body.run(); } finally { GlStateManager.popMatrix(); }
    }
}
