package lumaceon.mods.aeonicraft.client.model;

import lumaceon.mods.aeonicraft.client.model.temporal_hourglass.ModelTemporalHourglass;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;

@SuppressWarnings("NullableProblems")
public class AeonicraftModelLoader implements ICustomModelLoader
{
    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {

    }

    @Override
    public boolean accepts(ResourceLocation modelLocation) {
        return modelLocation.getResourcePath().endsWith("temporal_hourglass");
    }

    @Override
    public IModel loadModel(ResourceLocation modelLocation) {
        return new ModelTemporalHourglass();
    }
}
