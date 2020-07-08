package lumaceon.mods.aeonicraft.client.handler;

import lumaceon.mods.aeonicraft.Aeonicraft;
import lumaceon.mods.aeonicraft.client.particle.ModParticles;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod.EventBusSubscriber(modid = Aeonicraft.MOD_ID)
public class TickEventClient
{
    @SubscribeEvent
    public static void onClientUpdateTick(TickEvent.ClientTickEvent event)
    {
        ModParticles.updateParticleList();
    }

    /**@SubscribeEvent
    public static void onRenderTick(TickEvent.RenderTickEvent event)
    {

    }*/
}
