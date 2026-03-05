package me.scout.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.awt.Color;

/**
 * RenderHelper2D — 1.8.9 Forge equivalent of SoupAPI's Render2D.
 *
 * Key API differences from 1.21.4:
 *  - No MatrixStack  → use GL11 push/pop + translate/scale
 *  - No DrawContext  → draw directly via Tessellator
 *  - Rounded corners drawn by triangle fan with hand-rolled arc segments
 *  - Blurred shadow is approximated by layered semi-transparent quads (no BufferedImage needed)
 */
public class RenderHelper2D {

    private static final Minecraft mc = Minecraft.getMinecraft();

    // ─── Basic rect ───────────────────────────────────────────────────────────

    public static void drawRect(float x, float y, float w, float h, Color color) {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableTexture2D();
        GlStateManager.color(color.getRed() / 255f, color.getGreen() / 255f,
                color.getBlue() / 255f, color.getAlpha() / 255f);
        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr = tess.getWorldRenderer();
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        wr.pos(x,     y + h, 0).endVertex();
        wr.pos(x + w, y + h, 0).endVertex();
        wr.pos(x + w, y,     0).endVertex();
        wr.pos(x,     y,     0).endVertex();
        tess.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    // ─── Gradient rect (4 corner colors) ─────────────────────────────────────

    public static void drawGradientRect(float x, float y, float w, float h,
                                        Color tl, Color tr, Color br, Color bl) {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableTexture2D();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr = tess.getWorldRenderer();
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        wr.pos(x,     y + h, 0).color(bl.getRed(), bl.getGreen(), bl.getBlue(), bl.getAlpha()).endVertex();
        wr.pos(x + w, y + h, 0).color(br.getRed(), br.getGreen(), br.getBlue(), br.getAlpha()).endVertex();
        wr.pos(x + w, y,     0).color(tr.getRed(), tr.getGreen(), tr.getBlue(), tr.getAlpha()).endVertex();
        wr.pos(x,     y,     0).color(tl.getRed(), tl.getGreen(), tl.getBlue(), tl.getAlpha()).endVertex();
        tess.draw();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    // ─── Rounded rect (triangle fan) ─────────────────────────────────────────

    public static void drawRoundedRect(float x, float y, float w, float h,
                                       float radius, Color color) {
        drawRoundedRectGradient(x, y, w, h, radius, color, color, color, color);
    }

    /**
     * Draws a rounded rectangle with per-corner gradient.
     * Corner order: topLeft, topRight, bottomRight, bottomLeft.
     */
    public static void drawRoundedRectGradient(float x, float y, float w, float h,
                                               float radius, Color tl, Color tr, Color br, Color bl) {
        radius = Math.min(radius, Math.min(w, h) / 2f);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableTexture2D();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr = tess.getWorldRenderer();
        wr.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);

        // Center vertex (average color)
        Color center = avgColor(tl, tr, br, bl);
        wr.pos(x + w / 2f, y + h / 2f, 0)
          .color(center.getRed(), center.getGreen(), center.getBlue(), center.getAlpha())
          .endVertex();

        int segments = 8; // per corner
        // BL corner
        addArc(wr, x + radius,         y + h - radius, radius, 180, 270, segments, bl);
        // BR corner
        addArc(wr, x + w - radius,     y + h - radius, radius, 270, 360, segments, br);
        // TR corner
        addArc(wr, x + w - radius,     y + radius,     radius,   0,  90, segments, tr);
        // TL corner
        addArc(wr, x + radius,         y + radius,     radius,  90, 180, segments, tl);

        // close fan back to BL start
        double rad = Math.toRadians(180);
        wr.pos((float)(x + radius + Math.cos(rad) * radius),
               (float)(y + h - radius + Math.sin(rad) * radius), 0)
          .color(bl.getRed(), bl.getGreen(), bl.getBlue(), bl.getAlpha())
          .endVertex();

        tess.draw();

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    private static void addArc(WorldRenderer wr, float cx, float cy, float r,
                                int startDeg, int endDeg, int segments, Color c) {
        for (int i = 0; i <= segments; i++) {
            double angle = Math.toRadians(startDeg + (endDeg - startDeg) * i / (double)segments);
            wr.pos(cx + Math.cos(angle) * r, cy + Math.sin(angle) * r, 0)
              .color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha())
              .endVertex();
        }
    }

    // ─── HP bar ──────────────────────────────────────────────────────────────

    /**
     * Background trough (dark, rounded) + filled bar (gradient, rounded).
     * Matches SoupAPI pattern: drawGradientRound + renderRoundedGradientRect.
     */
    public static void drawHealthBar(float x, float y, float totalW, float filledW,
                                     float h, float radius,
                                     Color bgColor,
                                     Color barLeft, Color barRight) {
        // Background
        drawRoundedRect(x, y, totalW, h, radius, bgColor);
        // Filled portion
        if (filledW > 0) {
            drawRoundedRectGradient(x, y, filledW, h, radius, barLeft, barRight, barRight, barLeft);
        }
    }

    // ─── Glow / blurred shadow (layered quads approximation) ─────────────────

    /**
     * Approximate a coloured bloom shadow by drawing several expanding, fading rects.
     * Matches the visual intent of SoupAPI's drawGradientBlurredShadow1.
     */
    public static void drawGlowShadow(float x, float y, float w, float h,
                                      int layers, Color color) {
        if (layers <= 0) return;
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GlStateManager.disableTexture2D();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        for (int i = layers; i >= 1; i--) {
            float expand = i * 1.2f;
            float alpha  = (color.getAlpha() / 255f) * (1f - i / (float)(layers + 1)) * 0.35f;
            Color c = new Color(color.getRed() / 255f, color.getGreen() / 255f,
                                color.getBlue() / 255f, alpha);
            drawRoundedRect(x - expand, y - expand, w + expand * 2, h + expand * 2, 4, c);
        }

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableTexture2D();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableBlend();
    }

    // ─── Player head rendering ────────────────────────────────────────────────

    /**
     * Renders the player's head (face layer + hat layer) inside a rounded clip region.
     * In 1.8.9 we use the standard skin texture with UV offsets.
     */
    public static void drawPlayerHead(net.minecraft.client.network.NetworkPlayerInfo info,
                                      float x, float y, float size, float radius,
                                      float hurtFlash) {
        if (info == null) return;
        net.minecraft.util.ResourceLocation skin = info.getLocationSkin();

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableTexture2D();
        GlStateManager.color(1f, 1f - hurtFlash * 0.5f, 1f - hurtFlash * 0.5f, 1f);

        mc.getTextureManager().bindTexture(skin);

        // Face layer  (u=8,v=8 in 64x64 atlas → uv = 8/64 = 0.125)
        drawTexturedRect(x, y, size, size, 8, 8, 8, 8, 64, 64);
        // Hat layer   (u=40,v=8)
        drawTexturedRect(x, y, size, size, 40, 8, 8, 8, 64, 64);

        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.disableBlend();
    }

    // ─── Texture quad ─────────────────────────────────────────────────────────

    public static void drawTexturedRect(float x, float y, float w, float h,
                                        float u, float v,
                                        float regionW, float regionH,
                                        float texW, float texH) {
        float u0 = u / texW,        v0 = v / texH;
        float u1 = (u + regionW) / texW, v1 = (v + regionH) / texH;
        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr = tess.getWorldRenderer();
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        wr.pos(x,     y + h, 0).tex(u0, v1).endVertex();
        wr.pos(x + w, y + h, 0).tex(u1, v1).endVertex();
        wr.pos(x + w, y,     0).tex(u1, v0).endVertex();
        wr.pos(x,     y,     0).tex(u0, v0).endVertex();
        tess.draw();
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    public static Color applyOpacity(Color c, float opacity) {
        opacity = Math.max(0f, Math.min(1f, opacity));
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), (int)(c.getAlpha() * opacity));
    }

    public static Color fromARGB(int argb) { return new Color(argb, true); }

    private static Color avgColor(Color a, Color b, Color c, Color d) {
        return new Color(
            (a.getRed()   + b.getRed()   + c.getRed()   + d.getRed())   / 4,
            (a.getGreen() + b.getGreen() + c.getGreen() + d.getGreen()) / 4,
            (a.getBlue()  + b.getBlue()  + c.getBlue()  + d.getBlue())  / 4,
            (a.getAlpha() + b.getAlpha() + c.getAlpha() + d.getAlpha()) / 4
        );
    }

    public static float lerp(float a, float b, float t) { return a + (b - a) * t; }

    public static Color interpolateColor(Color start, Color end, float t) {
        t = Math.max(0f, Math.min(1f, t));
        return new Color(
            (int)(start.getRed()   + (end.getRed()   - start.getRed())   * t),
            (int)(start.getGreen() + (end.getGreen() - start.getGreen()) * t),
            (int)(start.getBlue()  + (end.getBlue()  - start.getBlue())  * t),
            (int)(start.getAlpha() + (end.getAlpha() - start.getAlpha()) * t)
        );
    }
}
