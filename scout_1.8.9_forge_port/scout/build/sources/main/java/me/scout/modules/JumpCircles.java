package me.scout.modules;

import me.scout.Scout;
import me.scout.utils.Palette;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;
import java.awt.Color;
import java.util.*;

public class JumpCircles {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final List<JumpCircle> CIRCLES = new ArrayList<>();
    private static boolean wasOnGround = true;

    public static void onTick() {
        if (!Scout.config.jumpCirclesEnabled) { CIRCLES.clear(); return; }
        EntityPlayer player = mc.thePlayer;
        if (player == null) return;
        boolean onGround = player.onGround;
        if (!onGround && wasOnGround && mc.gameSettings.keyBindJump.isKeyDown())
            CIRCLES.add(new JumpCircle(new Vec3(player.posX, player.posY, player.posZ)));
        wasOnGround = onGround;
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (!Scout.config.jumpCirclesEnabled) return;
        if (mc.theWorld == null) return;
        float pt = event.partialTicks;
        double cx = mc.getRenderManager().viewerPosX;
        double cy = mc.getRenderManager().viewerPosY;
        double cz = mc.getRenderManager().viewerPosZ;
        Iterator<JumpCircle> it = CIRCLES.iterator();
        while (it.hasNext()) {
            JumpCircle c = it.next();
            if (c.isExpired()) { it.remove(); continue; }
            c.render(pt, cx, cy, cz);
        }
    }

    private static class JumpCircle {
        private final Vec3 position;
        private final long startTime;
        private float rotationAngle = 0f;
        private long lastUpdate;
        private final float angularVelocity;
        private final boolean fadeOut;

        JumpCircle(Vec3 pos) {
            position=pos; startTime=System.currentTimeMillis(); lastUpdate=startTime;
            angularVelocity=(float)Math.toRadians(Scout.config.jumpCirclesSpinSpeed);
            fadeOut=Scout.config.jumpCirclesFadeOut;
        }

        boolean isExpired() { return (System.currentTimeMillis()-startTime)/1000f > Scout.config.jumpCirclesLiveTime; }

        void render(float pt, double cx, double cy, double cz) {
            long now=System.currentTimeMillis();
            float dt=Math.min((now-lastUpdate)/1000f,0.1f); lastUpdate=now;
            float liveTime=Scout.config.jumpCirclesLiveTime, elapsed=(now-startTime)/1000f;
            float remaining=MathHelper.clamp_float((liveTime-elapsed)/liveTime,0f,1f);
            float normalDelta=dt/(1f/60f);
            if (remaining>0.3f) rotationAngle-=angularVelocity*normalDelta;
            else rotationAngle+=angularVelocity*normalDelta*(remaining/0.3f);
            float radius=MathHelper.clamp_float((elapsed/(liveTime/3f))*3f,0f,1f)*(Scout.config.jumpCirclesScale/100f);
            if (remaining<0.3f) radius*=remaining/0.3f;
            float colorAnim=fadeOut?1f-remaining:1f;
            int alpha=(int)(Scout.config.jumpCirclesAlpha*2.55f*colorAnim);
            double x=position.xCoord-cx, y=position.yCoord-cy, z=position.zCoord-cz;
            ResourceLocation tex;
            switch (Scout.config.jumpCirclesStyle) {
                case CIRCLE_BOLD: tex=new ResourceLocation("scout","textures/jump_circles/circle_bold.png"); break;
                case HEXAGON:     tex=new ResourceLocation("scout","textures/jump_circles/hexagon.png"); break;
                case PORTAL:      tex=new ResourceLocation("scout","textures/jump_circles/portal.png"); break;
                case SCIFI:       tex=new ResourceLocation("scout","textures/jump_circles/scifi.png"); break;
                default:          tex=new ResourceLocation("scout","textures/jump_circles/circle.png"); break;
            }
            GlStateManager.disableCull();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
            GlStateManager.depthMask(false);
            mc.getTextureManager().bindTexture(tex);
            Color c1=Palette.getColor(0f), c2=Palette.getColor(0.33f), c3=Palette.getColor(0.66f), c4=Palette.getColor(1f);
            GL11.glPushMatrix();
            GL11.glTranslated(x,y,z);
            GL11.glRotatef(90f,1f,0f,0f);
            GL11.glRotatef((float)Math.toDegrees(rotationAngle),0f,0f,1f);
            Tessellator tess=Tessellator.getInstance();
            WorldRenderer wr=tess.getWorldRenderer();
            wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
            float r=radius;
            wr.pos(-r,-r+r*2,0).tex(0,1).color(c1.getRed(),c1.getGreen(),c1.getBlue(),alpha).endVertex();
            wr.pos(-r+r*2,-r+r*2,0).tex(1,1).color(c2.getRed(),c2.getGreen(),c2.getBlue(),alpha).endVertex();
            wr.pos(-r+r*2,-r,0).tex(1,0).color(c3.getRed(),c3.getGreen(),c3.getBlue(),alpha).endVertex();
            wr.pos(-r,-r,0).tex(0,0).color(c4.getRed(),c4.getGreen(),c4.getBlue(),alpha).endVertex();
            tess.draw();
            GL11.glPopMatrix();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GlStateManager.disableBlend();
            GlStateManager.enableCull();
            GlStateManager.depthMask(true);
        }
    }
    public enum Style { CIRCLE, CIRCLE_BOLD, HEXAGON, PORTAL, SCIFI }
}