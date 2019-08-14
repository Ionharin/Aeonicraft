package lumaceon.mods.aeonicraft.init;

import lumaceon.mods.aeonicraft.Aeonicraft;
import lumaceon.mods.aeonicraft.entity.EntityTravelGhost;
import lumaceon.mods.aeonicraft.item.ItemAeonicraftOreDict;
import lumaceon.mods.aeonicraft.item.ItemHourglassFunction;
import lumaceon.mods.aeonicraft.item.ItemTemporalHourglass;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.ArrayList;

@GameRegistry.ObjectHolder(Aeonicraft.MOD_ID)
public class ModItems
{
    private static final ArrayList<Item> ITEMS = new ArrayList<>();
    public static ArrayList<Item> getItems() {
        return ITEMS;
    }

    public static final Item ingotTemporal = init(new ItemAeonicraftOreDict(64, 100, "ingot_temporal", "ingotTemporal"));
    public static final Item ingotBrass = init(new ItemAeonicraftOreDict(64, 100, "ingot_brass", "ingotBrass"));
    public static final Item temporalHourglass = init(new ItemTemporalHourglass(1, 10000, "temporal_hourglass"));
    private static final String exName = "hourglass_function_excavator";
    public static final Item hgFuncExcavation = init(new ItemHourglassFunction(1, 10000, exName,"item/" + exName));
    private static final String fishName = "hourglass_function_fish";
    public static final Item hgFuncFish = init(new ItemHourglassFunction(1, 10000, fishName,"item/" + fishName));
    private static final String animalName = "hourglass_function_animal";
    public static final Item hgFuncAnimal = init(new ItemHourglassFunction(1, 10000, animalName,"item/" + animalName));
    private static final String travelName = "hourglass_function_travel";
    public static final Item hgFuncTravel = init(new ItemHourglassFunction(1, 10000, travelName,"item/" + travelName)
        {
            @Override
            public ActionResult<ItemStack> onHourglassRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
                if(!worldIn.isRemote) {
                    worldIn.spawnEntity(new EntityTravelGhost(worldIn, playerIn));
                }
                return super.onHourglassRightClick(worldIn, playerIn, handIn);
            }
        }
    );
    private static final String generalName = "hourglass_function_general";
    public static final Item hgFuncGeneral = init(new ItemHourglassFunction(1, 10000, generalName,"item/" + generalName));

    private static Item init(Item item) {
        ITEMS.add(item);
        return item;
    }
}
