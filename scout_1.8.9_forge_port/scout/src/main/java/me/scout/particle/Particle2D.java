package me.scout.particle;

import me.scout.render.RenderHelper2D;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.Color;

/**
 * Particle2D — ported from SoupAPI Particle2D.java.
 * Renders a small colored dot that drifts and fades.
 */
public class Particle2D {

    public float localX, localY;   // position relative to HUD anchor at spawn
    public float velX,   velY;
    public float opacity = 255f;
    private int  lifetime;
    private Color color;
    private boolean follow;         // true = anchored to world-space HUD

    public void init(float relX, float relY, float velX, float velY,
                     int lifetime, Color color, boolean follow) {
        this.localX   = relX;
        this.localY   = relY;
        this.velX     = velX;
        this.velY     = velY;
        this.lifetime = lifetime;
        this.opacity  = 255f;
        this.color    = color;
        this.follow   = follow;
    }

    public void updatePosition(float normalizedDelta) {
        localX += velX * normalizedDelta;
        localY += velY * normalizedDelta;
        opacity -= (255f / lifetime) * normalizedDelta;
        velX *= Math.pow(0.95, normalizedDelta);
        velY *= Math.pow(0.95, normalizedDelta);
    }

    /** Render at screen position = anchor + localOffset. */
    public void render2D(float anchorX, float anchorY, float depthFactor) {
        if (opacity <= 0) return;
        float wx = anchorX + localX * depthFactor;
        float wy = anchorY + localY * depthFactor;
        int a = Math.max(0, Math.min(255, (int)opacity));
        Color c = new Color(color.getRed(), color.getGreen(), color.getBlue(), a);
        RenderHelper2D.drawRect(wx - 1, wy - 1, 2, 2, c);
    }

    public static Color mixColors(Color a, Color b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int r = (int)(a.getRed()   + (b.getRed()   - a.getRed())   * t);
        int g = (int)(a.getGreen() + (b.getGreen() - a.getGreen()) * t);
        int bl = (int)(a.getBlue() + (b.getBlue()  - a.getBlue())  * t);
        return new Color(r, g, bl);
    }
}
