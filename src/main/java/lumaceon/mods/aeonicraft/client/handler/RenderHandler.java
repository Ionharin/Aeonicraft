package lumaceon.mods.aeonicraft.client.handler;

import lumaceon.mods.aeonicraft.Aeonicraft;
import lumaceon.mods.aeonicraft.client.particle.ModParticles;
import lumaceon.mods.aeonicraft.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

@Mod.EventBusSubscriber
public class RenderHandler
{
    private static Minecraft MC = Minecraft.getMinecraft();
    private static ResourceLocation MOD_PARTICLES = new ResourceLocation(Aeonicraft.MOD_ID, "textures/particles.png");

    private static boolean hourglassRenderedLastTick = false;
    private static double previousHourglassPosX = 0.0;
    private static double previousHourglassPosY = 0.0;
    private static double previousHourglassPosZ = 0.0;
    private static float previousHourglassRotationAngle = 0.0F;

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRenderWorld(RenderWorldLastEvent event)
    {
        Entity renderViewEntity = MC.getRenderViewEntity();
        float partialTicks = event.getPartialTicks();

        // Render custom mod particles...
        if(renderViewEntity != null)
        {
            ArrayList<Particle> particleList = ModParticles.getParticleList();

            if(!particleList.isEmpty())
            {
                GL11.glPushMatrix();
                GL11.glPushAttrib(0);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glDisable(GL11.GL_LIGHTING);
                Minecraft.getMinecraft().renderEngine.bindTexture(MOD_PARTICLES);

                GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 0.8F);

                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder bufferbuilder = tessellator.getBuffer();
                bufferbuilder.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
                for(int n = 0; n < particleList.size(); n++)
                {
                    Particle fx = particleList.get(n);
                    if(fx == null || !fx.isAlive())
                    {
                        particleList.remove(n);
                        --n;
                    }
                    else
                    {
                        fx.renderParticle(bufferbuilder, renderViewEntity, event.getPartialTicks(), ActiveRenderInfo.getRotationX(), ActiveRenderInfo.getRotationXZ(), ActiveRenderInfo.getRotationZ(), ActiveRenderInfo.getRotationYZ(), ActiveRenderInfo.getRotationXY());
                    }
                }
                tessellator.draw();
                GL11.glPopAttrib();
                GL11.glPopMatrix();
            }
        }

        EntityPlayer player = Aeonicraft.proxy.getClientPlayer();
        if(player != null)
        {
            boolean hourglassHeld = false;
            ItemStack heldItem = player.inventory.getCurrentItem();
            if(heldItem.getItem().equals(ModItems.temporal_hourglass))
                hourglassHeld = true;
            else
            {
                heldItem = player.inventory.offHandInventory.get(0);
                if(heldItem.getItem().equals(ModItems.temporal_hourglass))
                    hourglassHeld = true;
            }

            if(hourglassHeld)
            {
                /*CapabilityTimeStorage.ITimeStorage cap = player.getCapability(TIME_STORAGE, null);
                if(cap != null)
                {
                    Vec3d look = player.getLookVec();
                    look = look.addVector(0, -look.y, 0);
                    look = look.normalize();
                    double targetX = look.x;
                    double targetY = player.getEyeHeight() + Math.sin(System.currentTimeMillis()/1000.0)*0.075F - (player.rotationPitch*0.01F);
                    double targetZ = look.z;
                    float targetRotAngle = -player.rotationYaw + 180F;

                    if(!hourglassRenderedLastTick)
                    {
                        previousHourglassPosX = targetX;
                        previousHourglassPosY = targetY;
                        previousHourglassPosZ = targetZ;
                        previousHourglassRotationAngle = targetRotAngle;
                        hourglassRenderedLastTick = true;
                    }
                    else if(look.x == 0 && look.z == 0 && MC.gameSettings.thirdPersonView == 0)
                    {
                        targetX = previousHourglassPosX;
                        targetY = previousHourglassPosY;
                        targetZ = previousHourglassPosZ;
                        targetRotAngle = previousHourglassRotationAngle;
                    }
                    else
                    {
                        if(Math.abs(previousHourglassPosX - targetX) > 0.0025F)
                            targetX = previousHourglassPosX - (previousHourglassPosX - targetX) * 0.1F;
                        if(Math.abs(previousHourglassPosY - targetY) > 0.0025F)
                            targetY = previousHourglassPosY - (previousHourglassPosY - targetY) * 0.1F;
                        if(Math.abs(previousHourglassPosZ - targetZ) > 0.0025F)
                            targetZ = previousHourglassPosZ - (previousHourglassPosZ - targetZ) * 0.1F;
                        if(Math.abs(targetRotAngle - previousHourglassRotationAngle) > 0.0025F)
                            targetRotAngle = previousHourglassRotationAngle + (targetRotAngle - previousHourglassRotationAngle) * 0.1F;

                        previousHourglassPosX = targetX;
                        previousHourglassPosY = targetY;
                        previousHourglassPosZ = targetZ;
                        previousHourglassRotationAngle = targetRotAngle;
                    }

                    GlStateManager.pushMatrix();
                    GlStateManager.translate(targetX, targetY, targetZ);
                    GlStateManager.scale(0.5F, 0.5F, 0.5F);
                    GlStateManager.rotate(targetRotAngle, 0, 1, 0);
                    MC.getRenderItem().renderItem(heldItem, player, ItemCameraTransforms.TransformType.FIXED, false);

                    String renderString = TCToRealTime.parseTimeValue(TimeHelper.getTime(player), 2);
                    GlStateManager.scale(0.02F, 0.02F, 0.02F);
                    GlStateManager.translate(-MC.fontRenderer.getStringWidth(renderString) * 0.5F, 25.F, 30);
                    GlStateManager.rotate(180, 1, 0, 0);
                    MC.fontRenderer.drawString(renderString, 0, 0, 0xFFFFFF, false);
                    GlStateManager.popMatrix();
                }
                else*/
                    hourglassRenderedLastTick = false;
            }
            else
                hourglassRenderedLastTick = false;
        }
    }
}
