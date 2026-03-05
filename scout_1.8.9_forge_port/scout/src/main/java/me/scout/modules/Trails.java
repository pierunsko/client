package me.scout.modules;

import me.scout.Scout;
import me.scout.render.TargetHudRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;
import java.awt.Color;
import java.util.*;

public class Trails {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final Map<Integer, List<TrailSegment>> trailMap = new HashMap<>();

    public static void onTick() {
        if (!Scout.config.trailsEnabled) { trailMap.clear(); return; }
        if (mc.theWorld == null) return;
        int lifetime = Scout.config.trailsLength;
        for (EntityPlayer player : mc.theWorld.playerEntities) {
            Vec3 prev = new Vec3(player.prevPosX, player.prevPosY, player.prevPosZ);
            Vec3 curr = new Vec3(player.posX, player.posY, player.posZ);
            if (prev.distanceTo(curr) > 0.001f)
                getTrails(player.getEntityId()).add(new TrailSegment(prev, curr, lifetime));
            getTrails(player.getEntityId()).removeIf(TrailSegment::update);
        }
        Set<Integer> alive = new HashSet<>();
        for (EntityPlayer p : mc.theWorld.playerEntities) alive.add(p.getEntityId());
        trailMap.keySet().retainAll(alive);
    }

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
            if (entity == mc.thePlayer && mc.gameSettings.thirdPersonView == 0 && !Scout.config.trailsFirstPerson) continue;
            if (!mc.thePlayer.canEntityBeSeen(entity)) continue;
            List<TrailSegment> segs = entry.getValue();
            if (segs.size() < 2) continue;
            float height = entity.height * (Scout.config.trailsHeight / 100f);
            for (int i = 0; i < segs.size() - 1; i++) {
                TrailSegment cur = segs.get(i), next = segs.get(i + 1);
                Vec3 cp = cur.interpolate(pt, cx, cy, cz);
                Vec3 np = next.interpolate(pt, cx, cy, cz);
                float ca = (float)(cur.animation(pt) * 255);
                float na = (float)(next.animation(pt) * 255);
                Color cc = getAnimatedColor(cur.getProgress(pt), 1 - cur.getProgress(pt));
                Color nc = getAnimatedColor(next.getProgress(pt), 1 - next.getProgress(pt));
                int cbA, cmA, ctA, nbA, nmA, ntA;
                Style style = Scout.config.trailsStyle;
                if (style == Style.FADED) {
                    cbA=(int)(fadedAlpha(0,height)*ca/255f); cmA=(int)(fadedAlpha(height/2f,height)*ca/255f); ctA=Scout.config.trailsRenderHalf?0:(int)(fadedAlpha(height,height)*ca/255f);
                    nbA=(int)(fadedAlpha(0,height)*na/255f); nmA=(int)(fadedAlpha(height/2f,height)*na/255f); ntA=Scout.config.trailsRenderHalf?0:(int)(fadedAlpha(height,height)*na/255f);
                } else if (style == Style.FADED_INVERT) {
                    cbA=(int)(fadedInvert(0,height)*ca/255f); cmA=(int)(fadedInvert(height/2f,height)*ca/255f); ctA=Scout.config.trailsRenderHalf?0:(int)(fadedInvert(height,height)*ca/255f);
                    nbA=(int)(fadedInvert(0,height)*na/255f); nmA=(int)(fadedInvert(height/2f,height)*na/255f); ntA=Scout.config.trailsRenderHalf?0:(int)(fadedInvert(height,height)*na/255f);
                } else {
                    cbA=cmA=(int)ca; ctA=Scout.config.trailsRenderHalf?0:(int)ca;
                    nbA=nmA=(int)na; ntA=Scout.config.trailsRenderHalf?0:(int)na;
                }
                float x1=(float)cp.xCoord, y1=(float)cp.yCoord, z1=(float)cp.zCoord;
                float x2=(float)np.xCoord, y2=(float)np.yCoord, z2=(float)np.zCoord;
                wr.pos(x1,y1,z1).color(cc.getRed(),cc.getGreen(),cc.getBlue(),cbA).endVertex();
                wr.pos(x2,y2,z2).color(nc.getRed(),nc.getGreen(),nc.getBlue(),nbA).endVertex();
                wr.pos(x2,y2+height/2f,z2).color(nc.getRed(),nc.getGreen(),nc.getBlue(),nmA).endVertex();
                wr.pos(x1,y1+height/2f,z1).color(cc.getRed(),cc.getGreen(),cc.getBlue(),cmA).endVertex();
                if (!Scout.config.trailsRenderHalf) {
                    wr.pos(x1,y1+height/2f,z1).color(cc.getRed(),cc.getGreen(),cc.getBlue(),cmA).endVertex();
                    wr.pos(x2,y2+height/2f,z2).color(nc.getRed(),nc.getGreen(),nc.getBlue(),nmA).endVertex();
                    wr.pos(x2,y2+height,z2).color(nc.getRed(),nc.getGreen(),nc.getBlue(),ntA).endVertex();
                    wr.pos(x1,y1+height,z1).color(cc.getRed(),cc.getGreen(),cc.getBlue(),ctA).endVertex();
                }
            }
        }
        tess.draw();
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
    }

    private static List<TrailSegment> getTrails(int id) { return trailMap.computeIfAbsent(id, k -> new ArrayList<>()); }
    private static int fadedAlpha(float y, float h) { float r=y/h; return r<=0.5f?(int)((1f-r/0.5f)*255):(int)(((r-0.5f)/0.5f)*255); }
    private static int fadedInvert(float y, float h) { float r=y/h; int af=(int)(255*(Scout.config.trailsAlphaFactor/100f)); return r<=0.5f?(int)(af+(r/0.5f)*(255-af)):(int)(255-((r-0.5f)/0.5f)*(255-af)); }
    public static Color getAnimatedColor(float x, float y) {
        Color c1=TargetHudRenderer.topLeft, c2=TargetHudRenderer.topRight, c3=TargetHudRenderer.bottomRight, c4=TargetHudRenderer.bottomLeft;
        return lerpColor(lerpColor(c1,c2,x), lerpColor(c4,c3,x), y);
    }
    public static Color lerpColor(Color a, Color b, float t) { return new Color((int)(a.getRed()+(b.getRed()-a.getRed())*t),(int)(a.getGreen()+(b.getGreen()-a.getGreen())*t),(int)(a.getBlue()+(b.getBlue()-a.getBlue())*t)); }

    public static class TrailSegment {
        private final Vec3 from, to; private int ticks, prevTicks; private final int maxLifetime;
        public TrailSegment(Vec3 f, Vec3 t, int l) { from=f; to=t; ticks=l; maxLifetime=l; }
        public Vec3 interpolate(float pt, double cx, double cy, double cz) { return new Vec3(from.xCoord+(to.xCoord-from.xCoord)*pt-cx, from.yCoord+(to.yCoord-from.yCoord)*pt-cy, from.zCoord+(to.zCoord-from.zCoord)*pt-cz); }
        public double animation(float pt) { float age=maxLifetime-(prevTicks+(ticks-prevTicks)*pt); return Math.max(0,1-age/maxLifetime); }
        public boolean update() { prevTicks=ticks; return ticks--<=0; }
        public float getProgress(float pt) { return (maxLifetime-(prevTicks+(ticks-prevTicks)*pt))/(float)maxLifetime; }
    }
    public enum Style { FADED, SOLID, FADED_INVERT }
}