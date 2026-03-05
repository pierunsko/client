package me.scout.modules;

import me.scout.Scout;
import me.scout.render.TargetHudRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.util.*;

public class Trails {

    private static final Minecraft mc = Minecraft.getMinecraft();

    // Per-entity trail storage: entityId → list of segments
    private static final Map<Integer, List<TrailSegment>> trailMap = new HashMap<>();

    // ── Tick ──────────────────────────────────────────────────────────────────

    public static void onTick() {
        if (!Scout.config.trailsEnabled) { trailMap.clear(); return; }
        if (mc.theWorld == null) return;

        int lifetime = Scout.config.trailsLength;
        float minStep = 0.001f;

        for (EntityPlayer player : mc.theWorld.playerEntities) {
            Vec3 prev = new Vec3(player.prevPosX, player.prevPosY, player.prevPosZ);
            Vec3 curr = new Vec3(player.posX, player.posY, player.posZ);
            if (prev.distanceTo(curr) > minStep) {
                getTrails(player.getEntityId()).add(new TrailSegment(prev, curr, lifetime));
            }
            getTrails(player.getEntityId()).removeIf(TrailSegment::update);
        }

        if (Scout.config.trailsForGliders) {
            for (Object obj : mc.theWorld.loadedEntityList) {
                if (!(obj instanceof EntityLivingBase)) continue;
                EntityLivingBase e = (EntityLivingBase) obj;
                if (e == mc.thePlayer || e instanceof EntityPlayer) continue;
                // 1.8.9 has no isGliding — skip elytra logic, just track living entities
                Vec3 prev = new Vec3(e.prevPosX, e.prevPosY, e.prevPosZ);
                Vec3 curr = new Vec3(e.posX, e.posY, e.posZ);
                float yOff = e.height / 2f;
                if (prev.distanceTo(curr) > minStep) {
                    getTrails(e.getEntityId()).add(new TrailSegment(
                        new Vec3(prev.xCoord, prev.yCoord - yOff, prev.zCoord),
                        new Vec3(curr.xCoord, curr.yCoord - yOff, curr.zCoord),
                        lifetime));
                }
                getTrails(e.getEntityId()).removeIf(TrailSegment::update);
            }
        }

        // Prune entities no longer in world
        Set<Integer> alive = new HashSet<>();
        for (EntityPlayer p : mc.theWorld.playerEntities) alive.add(p.getEntityId());
        trailMap.keySet().retainAll(alive);
    }

    // ── Render ────────────────────────────────────────────────────────────────

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (!Scout.config.trailsEnabled) return;
        if (mc.theWorld == null || mc.thePlayer == null) return;

        float pt = event.partialTicks;
        double cx = mc.getRenderManager().viewerPosX;
        double cy = mc.getRenderManager().viewerPosY;
        double cz = mc.getRenderManager().viewerPosZ;

        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);

        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr = tess.getWorldRenderer();
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        for (Map.Entry<Integer, List<TrailSegment>> entry : trailMap.entrySet()) {
            Entity entity = mc.theWorld.getEntityByID(entry.getKey());
            if (entity == null) continue;
            if (entity instanceof EntityPlayer && entity == mc.thePlayer
                    && mc.gameSettings.thirdPersonView == 0
                    && !Scout.config.trailsFirstPerson) continue;
            if (!mc.thePlayer.canEntityBeSeen(entity)) continue;

            List<TrailSegment> segs = entry.getValue();
            if (segs.size() < 2) continue;

            float height = entity.height * (Scout.config.trailsHeight / 100f);

            for (int i = 0; i < segs.size() - 1; i++) {
                TrailSegment cur  = segs.get(i);
                TrailSegment next = segs.get(i + 1);

                Vec3 cp = cur.interpolate(pt, cx, cy, cz);
                Vec3 np = next.interpolate(pt, cx, cy, cz);

                float curAlpha  = (float)(cur.animation(pt)  * 255);
                float nextAlpha = (float)(next.animation(pt) * 255);

                Color curColor  = getAnimatedColor(cur.getProgress(pt),  1 - cur.getProgress(pt));
                Color nextColor = getAnimatedColor(next.getProgress(pt), 1 - next.getProgress(pt));

                int cbA, cmA, ctA, nbA, nmA, ntA;
                Style style = Scout.config.trailsStyle;
                if (style == Style.FADED) {
                    cbA = (int)(computeFadedAlpha(0,          height) * curAlpha  / 255f);
                    cmA = (int)(computeFadedAlpha(height/2f,  height) * curAlpha  / 255f);
                    ctA = Scout.config.trailsRenderHalf ? 0 : (int)(computeFadedAlpha(height, height) * curAlpha  / 255f);
                    nbA = (int)(computeFadedAlpha(0,          height) * nextAlpha / 255f);
                    nmA = (int)(computeFadedAlpha(height/2f,  height) * nextAlpha / 255f);
                    ntA = Scout.config.trailsRenderHalf ? 0 : (int)(computeFadedAlpha(height, height) * nextAlpha / 255f);
                } else if (style == Style.FADED_INVERT) {
                    cbA = (int)(computeFadedAlphaInvert(0,         height) * curAlpha  / 255f);
                    cmA = (int)(computeFadedAlphaInvert(height/2f, height) * curAlpha  / 255f);
                    ctA = Scout.config.trailsRenderHalf ? 0 : (int)(computeFadedAlphaInvert(height, height) * curAlpha  / 255f);
                    nbA = (int)(computeFadedAlphaInvert(0,         height) * nextAlpha / 255f);
                    nmA = (int)(computeFadedAlphaInvert(height/2f, height) * nextAlpha / 255f);
                    ntA = Scout.config.trailsRenderHalf ? 0 : (int)(computeFadedAlphaInvert(height, height) * nextAlpha / 255f);
                } else {
                    cbA = cmA = (int)curAlpha;  ctA = Scout.config.trailsRenderHalf ? 0 : (int)curAlpha;
                    nbA = nmA = (int)nextAlpha; ntA = Scout.config.trailsRenderHalf ? 0 : (int)nextAlpha;
                }

                float x1=(float)cp.xCoord, y1=(float)cp.yCoord, z1=(float)cp.zCoord;
                float x2=(float)np.xCoord, y2=(float)np.yCoord, z2=(float)np.zCoord;

                // Bottom quad
                wr.pos(x1, y1,          z1).color(curColor.getRed(),  curColor.getGreen(),  curColor.getBlue(),  cbA).endVertex();
                wr.pos(x2, y2,          z2).color(nextColor.getRed(), nextColor.getGreen(), nextColor.getBlue(), nbA).endVertex();
                wr.pos(x2, y2+height/2f,z2).color(nextColor.getRed(), nextColor.getGreen(), nextColor.getBlue(), nmA).endVertex();
                wr.pos(x1, y1+height/2f,z1).color(curColor.getRed(),  curColor.getGreen(),  curColor.getBlue(),  cmA).endVertex();

                // Top quad
                if (!Scout.config.trailsRenderHalf) {
                    wr.pos(x1, y1+height/2f, z1).color(curColor.getRed(),  curColor.getGreen(),  curColor.getBlue(),  cmA).endVertex();
                    wr.pos(x2, y2+height/2f, z2).color(nextColor.getRed(), nextColor.getGreen(), nextColor.getBlue(), nmA).endVertex();
                    wr.pos(x2, y2+height,    z2).color(nextColor.getRed(), nextColor.getGreen(), nextColor.getBlue(), ntA).endVertex();
                    wr.pos(x1, y1+height,    z1).color(curColor.getRed(),  curColor.getGreen(),  curColor.getBlue(),  ctA).endVertex();
                }
            }
        }

        tess.draw();
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static List<TrailSegment> getTrails(int id) {
        return trailMap.computeIfAbsent(id, k -> new ArrayList<>());
    }

    private static int computeFadedAlpha(float y, float h) {
        float rel = y / h;
        if (rel <= 0.5f) return (int)((1f - rel / 0.5f) * 255);
        return (int)(((rel - 0.5f) / 0.5f) * 255);
    }

    private static int computeFadedAlphaInvert(float y, float h) {
        float rel = y / h;
        int af = (int)(255 * (Scout.config.trailsAlphaFactor / 100f));
        if (rel <= 0.5f) return (int)(af + (rel / 0.5f) * (255 - af));
        return (int)(255 - ((rel - 0.5f) / 0.5f) * (255 - af));
    }

    public static Color getAnimatedColor(float x, float y) {
        Color c1 = TargetHudRenderer.topLeft,  c2 = TargetHudRenderer.topRight;
        Color c3 = TargetHudRenderer.bottomRight, c4 = TargetHudRenderer.bottomLeft;
        Color top    = lerpColor(c1, c2, x);
        Color bottom = lerpColor(c4, c3, x);
        return lerpColor(top, bottom, y);
    }

    public static Color lerpColor(Color a, Color b, float t) {
        return new Color(
            (int)(a.getRed()   + (b.getRed()   - a.getRed())   * t),
            (int)(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
            (int)(a.getBlue()  + (b.getBlue()  - a.getBlue())  * t)
        );
    }

    // ── TrailSegment ──────────────────────────────────────────────────────────

    public static class TrailSegment {
        private final Vec3 from, to;
        private int ticks, prevTicks;
        private final int maxLifetime;

        public TrailSegment(Vec3 from, Vec3 to, int lifetime) {
            this.from = from; this.to = to;
            this.ticks = lifetime; this.maxLifetime = lifetime;
        }

        public Vec3 interpolate(float pt, double cx, double cy, double cz) {
            double x = from.xCoord + (to.xCoord - from.xCoord) * pt - cx;
            double y = from.yCoord + (to.yCoord - from.yCoord) * pt - cy;
            double z = from.zCoord + (to.zCoord - from.zCoord) * pt - cz;
            return new Vec3(x, y, z);
        }

        public double animation(float pt) {
            float age = maxLifetime - (prevTicks + (ticks - prevTicks) * pt);
            return Math.max(0, 1 - age / maxLifetime);
        }

        public boolean update() {
            prevTicks = ticks;
            return ticks-- <= 0;
        }

        public float getProgress(float pt) {
            return (maxLifetime - (prevTicks + (ticks - prevTicks) * pt)) / (float)maxLifetime;
        }
    }

    public enum Style  { FADED, SOLID, FADED_INVERT }
    public enum Targets { PLAYERS, PROJECTILES, BOTH }
}
