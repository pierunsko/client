package me.scout.modules;

import me.scout.Scout;
import me.scout.interpolation.EaseOutCirc;
import me.scout.particle.Particle2D;
import me.scout.render.TargetHudRenderer;
import me.scout.utils.MathUtility;
import me.scout.utils.Palette;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.Color;
import java.util.ArrayList;

public class TargetHud {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private static float hudScale = 0f;
    private static final float SCALE_SPEED = 0.2f;
    private static float hudTimer = 0f;

    public static EaseOutCirc headAnimation = new EaseOutCirc();
    public static EntityLivingBase target = null;
    private static EntityLivingBase lastTarget = null;

    private static float displayedHealth = 0f;
    private static final float HP_LERP_SPEED = 0.2f;
    private static final float COLOR_ANIM_SPEED = 0.01f;

    private static long lastUpdateTime = System.currentTimeMillis();

    public static float smoothedScreenX = 0f;
    public static float smoothedScreenY = 0f;
    private static final float SMOOTH_FACTOR = 0.8f;

    public static void onTick() {
        if (!Scout.config.targetHudEnabled) return;
        EntityPlayerSP player = mc.thePlayer;
        if (player == null) return;

        acquireTarget();

        if (lastTarget != null && !player.canEntityBeSeen(lastTarget)) {
            hudTimer = 0;
        }

        if (target instanceof EntityPlayer) {
            if (target != lastTarget) {
                displayedHealth = Math.min(target.getMaxHealth(), getHealth());
                lastTarget = target;
            }
            hudTimer = Scout.config.targetHudRenderTime;
            target = null;
        }

        if (lastTarget instanceof EntityPlayer) {
            float targetHealth = Math.min(lastTarget.getMaxHealth(), getHealth());
            displayedHealth = displayedHealth + HP_LERP_SPEED * (targetHealth - displayedHealth);
        }

        if (Scout.config.hideHPBar) {
            displayedHealth = 20f;
        }
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.HOTBAR) return;

        updateColors(TargetHudRenderer.colorAnimationProgress);

        if (!Scout.config.targetHudEnabled) return;

        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastUpdateTime) / 1000f;
        deltaTime = Math.min(deltaTime, 0.1f);
        lastUpdateTime = currentTime;

        float normalizedDelta = deltaTime / (1.0f / 60.0f);

        TargetHudRenderer.colorAnimationProgress =
                (TargetHudRenderer.colorAnimationProgress + normalizedDelta * COLOR_ANIM_SPEED) % 1.0f;
        TargetHudRenderer.hpColorAnimationProgress =
                (TargetHudRenderer.hpColorAnimationProgress + normalizedDelta * COLOR_ANIM_SPEED / 2) % 1.0f;
        headAnimation.update(normalizedDelta);
        TargetHudRenderer.ticks += 0.1f * normalizedDelta;

        if (hudTimer > 0) {
            hudScale = hudScale + normalizedDelta * SCALE_SPEED * (1.0f - hudScale);
            hudTimer -= deltaTime;
            if (hudTimer < 0) hudTimer = 0;
        } else {
            hudScale = hudScale + normalizedDelta * SCALE_SPEED * (0.0f - hudScale);
        }

        for (Particle2D p : new ArrayList<>(TargetHudRenderer.particles)) {
            p.updatePosition(normalizedDelta);
            if (p.opacity < 1) TargetHudRenderer.particles.remove(p);
        }

        if (lastTarget != null && (lastTarget.isDead ||
                (mc.theWorld != null && mc.theWorld.getEntityByID(lastTarget.getEntityId()) == null))) {
            hudTimer = 0;
            lastTarget = null;
        }

        if (hudScale <= 0 || !(lastTarget instanceof EntityPlayer)) return;

        int sw = event.resolution.getScaledWidth();
        int sh = event.resolution.getScaledHeight();

        int targetScreenX = sw / 2 + Scout.config.targetHudOffsetX;
        int targetScreenY = sh / 2 - Scout.config.targetHudOffsetY;

        smoothedScreenX += SMOOTH_FACTOR * normalizedDelta * (targetScreenX - smoothedScreenX);
        smoothedScreenY += SMOOTH_FACTOR * normalizedDelta * (targetScreenY - smoothedScreenY);

        Style style = Scout.config.targetHudStyle;
        float centerX, centerY;
        if (style == Style.MINI) {
            centerX = smoothedScreenX + 47.5f;
            centerY = smoothedScreenY + 17.5f;
        } else if (style == Style.NORMAL) {
            centerX = smoothedScreenX + 68.5f;
            centerY = smoothedScreenY + 23.75f;
        } else {
            centerX = smoothedScreenX;
            centerY = smoothedScreenY;
        }

        org.lwjgl.opengl.GL11.glPushMatrix();
        org.lwjgl.opengl.GL11.glTranslatef(centerX, centerY, 0);
        org.lwjgl.opengl.GL11.glScalef(hudScale, hudScale, 1f);
        org.lwjgl.opengl.GL11.glTranslatef(-centerX, -centerY, 0);

        float animFactor = MathUtility.clamp(hudScale, 0f, 1f);
        EntityPlayer ep = (EntityPlayer) lastTarget;
        int sx = (int) smoothedScreenX, sy = (int) smoothedScreenY;

        switch (style) {
            case MINI:   TargetHudRenderer.renderMiniHUD  (normalizedDelta, displayedHealth, animFactor, ep, sx, sy); break;
            case NORMAL: TargetHudRenderer.renderNormalHUD(normalizedDelta, displayedHealth, animFactor, ep, sx, sy); break;
            case ARES:   TargetHudRenderer.renderAresHUD  (normalizedDelta, displayedHealth, animFactor, ep, sx, sy); break;
            case ALT_1:  TargetHudRenderer.renderAlt1HUD  (normalizedDelta, displayedHealth, animFactor, ep, sx, sy); break;
            case TINY:   TargetHudRenderer.renderTinyHUD  (normalizedDelta, displayedHealth, animFactor, ep, sx, sy); break;
        }

        org.lwjgl.opengl.GL11.glPopMatrix();
    }

    private static void updateColors(float progress) {
        Color c1 = Palette.getColor(0f);
        Color c2 = Palette.getColor(0.33f);
        Color c3 = Palette.getColor(0.66f);
        Color c4 = Palette.getColor(1f);

        progress = progress % 1.0f;
        if (progress < 0.25f) {
            float p = progress / 0.25f;
            TargetHudRenderer.topLeft     = lerp(c1, c2, p);
            TargetHudRenderer.topRight    = lerp(c2, c3, p);
            TargetHudRenderer.bottomRight = lerp(c3, c4, p);
            TargetHudRenderer.bottomLeft  = lerp(c4, c1, p);
        } else if (progress < 0.5f) {
            float p = (progress - 0.25f) / 0.25f;
            TargetHudRenderer.topLeft     = lerp(c2, c3, p);
            TargetHudRenderer.topRight    = lerp(c3, c4, p);
            TargetHudRenderer.bottomRight = lerp(c4, c1, p);
            TargetHudRenderer.bottomLeft  = lerp(c1, c2, p);
        } else if (progress < 0.75f) {
            float p = (progress - 0.5f) / 0.25f;
            TargetHudRenderer.topLeft     = lerp(c3, c4, p);
            TargetHudRenderer.topRight    = lerp(c4, c1, p);
            TargetHudRenderer.bottomRight = lerp(c1, c2, p);
            TargetHudRenderer.bottomLeft  = lerp(c2, c3, p);
        } else {
            float p = (progress - 0.75f) / 0.25f;
            TargetHudRenderer.topLeft     = lerp(c4, c1, p);
            TargetHudRenderer.topRight    = lerp(c1, c2, p);
            TargetHudRenderer.bottomRight = lerp(c2, c3, p);
            TargetHudRenderer.bottomLeft  = lerp(c3, c4, p);
        }
    }

    private static void acquireTarget() {
        EntityPlayerSP player = mc.thePlayer;
        if (player == null || mc.theWorld == null) { target = null; return; }

        if (mc.currentScreen != null) {
            if (mc.currentScreen instanceof net.minecraft.client.gui.GuiChat) {
                target = player;
            } else {
                target = null;
            }
            return;
        }

        EntityPlayer nearest = null;
        double nearestDist = Double.MAX_VALUE;

        for (EntityPlayer ep : mc.theWorld.playerEntities) {
            if (ep == player) continue;
            if (ep.isInvisible()) continue;
            double dist = player.getDistanceSqToEntity(ep);
            if (dist < 100.0 && dist < nearestDist && player.canEntityBeSeen(ep)) {
                nearest = ep;
                nearestDist = dist;
            }
        }
        target = nearest;
    }

    public static float getHealth() {
        if (lastTarget == null) return 0f;
        return lastTarget.getHealth() + lastTarget.getAbsorptionAmount();
    }

    private static Color lerp(Color a, Color b, float t) {
        return new Color(
            (int)(a.getRed()   + (b.getRed()   - a.getRed())   * t),
            (int)(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
            (int)(a.getBlue()  + (b.getBlue()  - a.getBlue())  * t),
            (int)(a.getAlpha() + (b.getAlpha() - a.getAlpha()) * t)
        );
    }

    public enum Style { MINI, TINY, NORMAL, ARES, ALT_1 }
}
