package lumaceon.mods.aeonicraft.item;

import lumaceon.mods.aeonicraft.Aeonicraft;
import lumaceon.mods.aeonicraft.capability.timelink.CapabilityTimeLink;
import lumaceon.mods.aeonicraft.network.PacketHandler;
import lumaceon.mods.aeonicraft.network.message.MessageHourglassTCUpdate;
import lumaceon.mods.aeonicraft.temporalcompression.ITemporalCompressorLinkableBlock;
import lumaceon.mods.aeonicraft.temporalcompression.TemporalCompressor;
import lumaceon.mods.aeonicraft.util.BlockLoc;
import lumaceon.mods.aeonicraft.util.Colors;
import lumaceon.mods.aeonicraft.util.TimeParser;
import lumaceon.mods.aeonicraft.worlddata.ExtendedWorldData;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("NullableProblems")
public class ItemTemporalHourglass extends ItemAeonicraft
{
    @CapabilityInject(CapabilityTimeLink.ITimeLinkHandler.class)
    private static final Capability<CapabilityTimeLink.ITimeLinkHandler> TIME_LINK = null;

    public ItemTemporalHourglass(int maxStack, int maxDamage, String name) {
        super(maxStack, maxDamage, name);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag)
    {
        CapabilityTimeLink.ITimeLinkHandler cap = stack.getCapability(TIME_LINK, null);
        if(cap != null) {
            tooltip.add(Colors.AQUA + "TC: " + TimeParser.parseTimeValue(cap.getTimeClient(world), 2));
        }
        else {
            tooltip.add(Colors.RED + "Broken");
        }
    }

    public boolean showDurabilityBar(ItemStack stack)
    {
        return true;
    }

    /**
     * Queries the percentage of the 'Durability' bar that should be drawn.
     *
     * @param stack The current ItemStack
     * @return 0.0 for 100% (no damage / full bar), 1.0 for 0% (fully damaged / empty bar)
     */
    public double getDurabilityForDisplay(ItemStack stack)
    {
        CapabilityTimeLink.ITimeLinkHandler cap = stack.getCapability(TIME_LINK, null);
        if(cap != null) {
            return 1.0f - (cap.getTimeClient(Aeonicraft.proxy.getClientWorld()) / 60000.0F);
        }
        return 1.0F;
    }

    @Override
    public void readNBTShareTag(ItemStack stack, @Nullable NBTTagCompound nbt)
    {
        Aeonicraft.logger.info("Hi");
        stack.setTagCompound(nbt);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        Block block = world.getBlockState(pos).getBlock();
        if(block instanceof ITemporalCompressorLinkableBlock)
        {
            if(!world.isRemote) {
                ((ITemporalCompressorLinkableBlock) block).onLinkAttempt(player, world, pos, hand, facing, hitX, hitY, hitZ);
            }
            return EnumActionResult.SUCCESS;
        }

        if(!world.isRemote)
        {
            ExtendedWorldData worldData = ExtendedWorldData.getInstance(world);

            ItemStack hourglassInHand = player.getHeldItem(hand);
            CapabilityTimeLink.ITimeLinkHandler cap = hourglassInHand.getCapability(TIME_LINK, null);
            if(cap != null)
            {
                BlockLoc[] locs = cap.getCompressorLocations();
                for(BlockLoc loc : locs)
                {
                    Aeonicraft.logger.info(worldData.temporalCompressorProcesses.get(loc).getLocation().toString());
                }
            }
        }
        return EnumActionResult.PASS;
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected)
    {
        ExtendedWorldData worldData = ExtendedWorldData.getInstance(world);
        CapabilityTimeLink.ITimeLinkHandler cap = stack.getCapability(TIME_LINK, null);
        if(cap != null && !world.isRemote)
        {
            boolean isDirty = false;

            BlockLoc[] locs = cap.getCompressorLocations();
            ArrayList<TemporalCompressor> tcs = new ArrayList<>();
            for(BlockLoc loc : locs)
            {
                TemporalCompressor tc = worldData.temporalCompressorProcesses.get(loc);
                if(tc != null)
                {
                    tcs.add(tc);
                    if(worldData.updateCompressorAt(loc, world))
                    {
                        isDirty = true;
                    }
                }
                else
                {
                    Aeonicraft.logger.info("Hourglass searched for null compressor at: ("+loc.toString()+")");
                }
            }

            if(isDirty && entity instanceof EntityPlayerMP)
            {
                PacketHandler.INSTANCE.sendTo(new MessageHourglassTCUpdate(tcs.toArray(new TemporalCompressor[0]), itemSlot), (EntityPlayerMP) entity);
            }
        }
    }

    @Override
    public net.minecraftforge.common.capabilities.ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
        return new Provider();
    }


    private static class Provider implements ICapabilitySerializable<NBTTagCompound>
    {
        CapabilityTimeLink.ITimeLinkHandler timeLinkHandler;

        private Provider()
        {
            timeLinkHandler = new CapabilityTimeLink.TimeLinkHandler();
            //Init stuff if necessary...
        }

        @Override
        public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
            return capability == TIME_LINK;
        }

        @Override
        public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing)
        {
            if(capability == TIME_LINK)
            {
                return TIME_LINK.cast(timeLinkHandler);
            }
            return null;
        }

        @Override
        public NBTTagCompound serializeNBT()
        {
            return timeLinkHandler.saveToNBT();
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt)
        {
            timeLinkHandler.loadFromNBT(nbt);
        }
    }
}
