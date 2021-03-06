package lumaceon.mods.aeonicraft.handler;

import lumaceon.mods.aeonicraft.Aeonicraft;
import lumaceon.mods.aeonicraft.api.hourglass.HourglassFunction;
import lumaceon.mods.aeonicraft.api.temporal.TC;
import lumaceon.mods.aeonicraft.capability.CapabilityAeonicraftProgression;
import lumaceon.mods.aeonicraft.capability.CapabilityHourglass;
import lumaceon.mods.aeonicraft.entity.EntityTemporalFishHook;
import lumaceon.mods.aeonicraft.lib.TimeCosts;
import lumaceon.mods.aeonicraft.registry.ModHourglassFunctions;
import lumaceon.mods.aeonicraft.registry.ModItems;
import lumaceon.mods.aeonicraft.item.ItemTemporalHourglass;
import lumaceon.mods.aeonicraft.util.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;

@Mod.EventBusSubscriber(modid = Aeonicraft.MOD_ID)
public class PlayerEventHandler
{
    /**
     * Copies data to the new player when respawning after death.
     */
    @SubscribeEvent
    public void onPlayerRespawning(PlayerEvent.Clone event)
    {
        if(event.isWasDeath())
        {
            EntityPlayer original = event.getOriginal();
            EntityPlayer newPlayer = event.getEntityPlayer();
            CapabilityAeonicraftProgression.IAeonicraftProgressionHandler cap = original.getCapability(CapabilityAeonicraftProgression.AEONICRAFT_PROGRESSION_CAPABILITY, null);
            CapabilityAeonicraftProgression.IAeonicraftProgressionHandler capNew = newPlayer.getCapability(CapabilityAeonicraftProgression.AEONICRAFT_PROGRESSION_CAPABILITY, null);
            if(cap != null && capNew != null)
            {
                capNew.loadFromNBT(cap.saveToNBT());
            }
        }
    }

    @SubscribeEvent
    public static void onAdvancementCompleted(AdvancementEvent event)
    {

    }

    @SubscribeEvent
    public static void onBlockAboutToBeBroken(PlayerEvent.BreakSpeed event)
    {
        EntityPlayer player = event.getEntityPlayer();
        if(player != null)
        {
            ItemStack firstHourglass = InventoryHelper.getFirstStackOfTypeInInventory(player.inventory, ModItems.temporal_hourglass);
            if(firstHourglass != null)
            {
                ItemTemporalHourglass hourglass = (ItemTemporalHourglass) firstHourglass.getItem();
                HourglassFunction hourglassFunction = hourglass.getActiveHourglassFunction(firstHourglass);
                if(hourglassFunction != null && hourglassFunction.equals(ModHourglassFunctions.hgf_excavation_overclocker))
                {
                    TC timeToBreakBlock = TimeHelper.getTimeToBreakBlock(player.world, event.getPos(), event.getState(), player, player.inventory.getCurrentItem());
                    if(TimeHelper.getTime(player).getVal() >= timeToBreakBlock.getVal())
                    {
                        int hourglassSlotIndex = -1;
                        for(int i = 0; i < player.inventory.getSizeInventory(); i++)
                        {
                            if(player.inventory.getStackInSlot(i) == firstHourglass)
                            {
                                hourglassSlotIndex = i;
                                break;
                            }
                        }
                        TimeHelper.consumeTime(player, timeToBreakBlock);
                        event.setNewSpeed(Float.MAX_VALUE);

                        SoundHelper.playShortTimeDing(player, player.world, player.posX, player.posY, player.posZ);
                        ParticleHelper.spawnTemporalBurstParticles(new Vec3d(event.getPos()).addVector(0.5, 0.5, 0.5), new Vec3d(1, 1, 1), 10);
                    }
                    else
                    {
                        event.setNewSpeed(0.0F);
                    }
                }
            }
        }
    }

    public static HashMap<EntityPlayer, Integer> updateCount = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerUpdate(TickEvent.PlayerTickEvent event)
    {
        EntityPlayer player = event.player;

        if(player.world.isRemote)
            return;

        int currentUpdateCount = updateCount.getOrDefault(player, -1) + 1;

        // Modify fish hook if the appropriate hourglass module is active...
        if(player.fishEntity != null)
        {
            ItemStack stack = InventoryHelper.getFirstStackOfTypeInInventory(player.inventory, ModItems.temporal_hourglass);
            CapabilityHourglass.IHourglassHandler cap = stack.getCapability(CapabilityHourglass.HOURGLASS, null);
            if(cap != null)
            {
                HourglassFunction func = cap.getActiveFunction();
                if(func != null && func.equals(ModHourglassFunctions.hgf_aquatic_lure_overclocker) && !(player.fishEntity instanceof EntityTemporalFishHook) && TimeHelper.getTime(player).getVal() >= TimeCosts.INSTANT_FISH_MAX.getVal())
                {
                    player.fishEntity.setDead();
                    EntityTemporalFishHook fishHook = new EntityTemporalFishHook(player.world, player);
                    fishHook.setPosition(player.fishEntity.posX, player.fishEntity.posY, player.fishEntity.posZ);
                    player.world.spawnEntity(fishHook);
                    player.fishEntity = fishHook;
                }
            }
        }

        updateCount.put(player, currentUpdateCount);
    }


    @SubscribeEvent
    public static void onPlayerInteractWithEntity(PlayerInteractEvent.EntityInteract event)
    {
        EntityPlayer player = event.getEntityPlayer();
        Entity target = event.getTarget();
        if(player != null && !player.world.isRemote && target != null)
        {
            if(target instanceof EntitySheep)
            {
                EntitySheep sheep = (EntitySheep) target;
                ItemStack stackInHand = player.inventory.getCurrentItem();
                ItemStack hourglass = InventoryHelper.getFirstStackOfTypeInInventory(player.inventory, ModItems.temporal_hourglass);
                HourglassFunction func = InventoryHelper.getHourglassFunctionFromHourglass(hourglass);
                BlockPos centralPosition = event.getPos();
                int foundGrass = 0;
                BlockPos firstGrass = null;
                for(int x = -2; x <= 2 && foundGrass < 2; x++)
                {
                    for(int z = -2; z <= 2 && foundGrass < 2; z++)
                    {
                        for(int y = 0; y > -3 && foundGrass < 2; y--)
                        {
                            IBlockState state = sheep.world.getBlockState(centralPosition.add(x, y, z));
                            if(state.getBlock().equals(Blocks.GRASS))
                            {
                                if(firstGrass == null)
                                    firstGrass = centralPosition.add(x, y, z);
                                foundGrass += 1;
                            }
                        }
                    }
                }
                if (
                        func != null
                        && func.equals(ModHourglassFunctions.hgf_livestock_overclocker)
                        && hourglass.getItem() instanceof ItemTemporalHourglass
                        && sheep.isShearable(stackInHand, player.world, event.getPos())
                        && stackInHand.getItem() instanceof ItemShears
                        && foundGrass > 0
                )
                {

                    long availableTime = TimeHelper.getTime(player).getVal();

                    // do we have time to shear a sheep MY WAY...?
                    if(availableTime >= 20000)
                    {
                        long timeToConsume = 0;

                        // we time-shear a floofy sheep on grass...
                        shearDatSheep(sheep, stackInHand, event.getPos(), player);

                        // also cancel the event to avoid double-shearing...
                        event.setCanceled(true);
                        event.setCancellationResult(EnumActionResult.SUCCESS);

                        // auto-floof sheep...
                        timeToConsume += 20000;
                        sheep.setSheared(false);

                        if(availableTime < 20000 + TC.SECOND.getVal() * 10)
                        {
                            // within range of sheep regrowth, but not grass growth, so RIP grass...
                            sheep.world.setBlockState(firstGrass, Blocks.DIRT.getDefaultState());
                        }
                        else
                        {
                            // time for both sheep AND grass...
                            timeToConsume += TC.SECOND.getVal() * 10;

                            if(foundGrass == 1)
                            {
                                // no nearby grass, so set it to dirt...
                                sheep.world.setBlockState(firstGrass, Blocks.DIRT.getDefaultState());
                            }

                            // either way, consume the time...
                            TimeHelper.consumeTime(player, new TC(timeToConsume));

                            BlockPos pos = event.getPos();

                            SoundHelper.playShortTimeDing(null, player.world, pos.getX(), pos.getY(), pos.getZ());
                            ParticleHelper.spawnTemporalBurstParticles(target.getPositionVector(), new Vec3d(1, 1, 1), 8);
                        }
                    }
                }
            }
        }
    }

    private static void shearDatSheep(EntitySheep sheep, ItemStack stackInHand, BlockPos pos, EntityPlayer player)
    {
        java.util.List<ItemStack> drops = sheep.onSheared(stackInHand, sheep.world, pos,
                net.minecraft.enchantment.EnchantmentHelper.getEnchantmentLevel(net.minecraft.init.Enchantments.FORTUNE, stackInHand));

        java.util.Random rand = new java.util.Random();
        for(ItemStack stack : drops)
        {
            net.minecraft.entity.item.EntityItem ent = sheep.entityDropItem(stack, 1.0F);
            assert ent != null;
            ent.motionY += rand.nextFloat() * 0.05F;
            ent.motionX += (rand.nextFloat() - rand.nextFloat()) * 0.1F;
            ent.motionZ += (rand.nextFloat() - rand.nextFloat()) * 0.1F;
        }
        stackInHand.damageItem(1, player);
    }
}
