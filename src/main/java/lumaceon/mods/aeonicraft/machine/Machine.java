package lumaceon.mods.aeonicraft.machine;

import lumaceon.mods.aeonicraft.api.clockwork.EnergyStorageModular;
import lumaceon.mods.aeonicraft.util.TickInterval;
import net.minecraft.nbt.NBTTagCompound;

import java.util.function.Function;

public class Machine
{
    protected EnergyStorageModular energyStorage;

    /**
     * Tick interval is how many ingame ticks are required for a machine tick, which may possibly be configurable.
     * In some cases, certain checks may be performed based on game ticks instead of machine ticks.
     */
    protected TickInterval tickInterval;

    protected float currentProgress = 0.0F;
    protected float progressPerGameTick = 1.0F;
    protected float progressCostPerAction = 100.0F;
    protected float energyCostPerProgress = 1.0F;

    /**
     * Output the maximum number of actions that could be taken when called.
     */
    protected Function<Machine, Integer> getMaxActions;

    /**
     * Maximum number of allowed actions as input; amount actually taken as output. Take as many as possible up to max.
     */
    protected Function<Integer, Integer> takeActions;


    public Machine(EnergyStorageModular energyStorage,
                   TickInterval tickInterval,
                   float progressCostPerAction,
                   float progressGainPerGameTick,
                   float energyCostPerProgress,
                   Function<Machine, Integer> getMaxActions,
                   Function<Integer, Integer> takeActions) {
        this.energyStorage = energyStorage;
        this.tickInterval = tickInterval;
        this.progressCostPerAction = progressCostPerAction;
        this.progressPerGameTick = progressGainPerGameTick;
        this.energyCostPerProgress = energyCostPerProgress;
        this.getMaxActions = getMaxActions;
        this.takeActions = takeActions;
    }

    /**
     * To be called every available update tick.
     * @return True if important data has changed, false otherwise.
     */
    public boolean gameTick()
    {
        if(tickInterval.receiveTick())
            return machineTick();
        return false;
    }

    /**
     * Called internally every internal tick interval.
     * @return True if important data has changed, false otherwise.
     */
    protected boolean machineTick()
    {
        boolean ret = progressTick();

        int actions = this.takeActions.apply((int) (this.currentProgress / this.progressCostPerAction));
        this.currentProgress -= (actions * this.progressCostPerAction);

        if(actions != 0)
            ret = true;

        return ret;
    }

    /**
     * Called every internal tick interval; specifically handles progress.
     * @return True if important data has changed, false otherwise.
     */
    protected boolean progressTick()
    {
        float progressToAdd = tickInterval.getTickInterval() * this.progressPerGameTick;
        progressToAdd = Math.min(progressToAdd, getMaxProgressFromEnergy());
        progressToAdd = Math.min(progressToAdd, getMaxActions.apply(this) * progressCostPerAction - currentProgress);

        if(this.energyStorage != null) {
            this.energyStorage.extractEnergy((int) (currentProgress * energyCostPerProgress), false);
        }

        currentProgress += progressToAdd;

        return progressToAdd != 0;
    }

    /**
     * @return The maximum progress that can be gained from the currently available energy.
     */
    protected float getMaxProgressFromEnergy() {
        if(this.energyCostPerProgress <= 0.000000001F) return Float.MAX_VALUE;
        if(energyStorage == null) return 0.0F;
        return energyStorage.extractEnergy(Integer.MAX_VALUE, true) / energyCostPerProgress;
    }

    /**
     * @return The number of game ticks it takes to complete one machine action.
     */
    protected float getGameTicksPerAction() {
        return this.progressCostPerAction / this.progressPerGameTick;
    }


    // *** OPTIONAL NBT FUNCTIONS *** //

    public NBTTagCompound serializeNBT()
    {
        NBTTagCompound nbt = new NBTTagCompound();

        nbt.setTag("tick_interval_stats", tickInterval.serializeNBT());

        nbt.setInteger("energy", energyStorage.getEnergyStored());
        nbt.setInteger("max_energy", energyStorage.getMaxEnergyStored());

        nbt.setFloat("current_progress", this.currentProgress);
        nbt.setFloat("progress_per_game_tick", this.progressPerGameTick);
        nbt.setFloat("progress_per_action", this.progressCostPerAction);
        nbt.setFloat("energy_per_progress", this.energyCostPerProgress);

        return nbt;
    }

    public void deserializeNBT(NBTTagCompound nbt)
    {
        this.tickInterval = new TickInterval(nbt.getCompoundTag("tick_interval_stats"));

        int maxEnergy = nbt.getInteger("max_energy");
        this.energyStorage = new EnergyStorageModular(maxEnergy, maxEnergy, maxEnergy, nbt.getInteger("energy"));

        this.currentProgress = nbt.getFloat("current_progress");
        this.progressPerGameTick = nbt.getFloat("progress_per_game_tick");
        this.progressCostPerAction = nbt.getFloat("progress_per_action");
        this.energyCostPerProgress = nbt.getFloat("energy_per_progress");
    }


    // *** GENERATED MUTATORS AND ACCESSORS *** //

    public EnergyStorageModular getEnergyStorage() { return energyStorage; }
    public TickInterval getTickInterval() {
        return tickInterval;
    }
    public float getCurrentProgress() {
        return currentProgress;
    }
    public void setCurrentProgress(float currentProgress) {
        this.currentProgress = currentProgress;
    }
    public float getProgressPerGameTick() {
        return progressPerGameTick;
    }
    public void setProgressPerGameTick(float progressPerGameTick) {
        this.progressPerGameTick = progressPerGameTick;
    }
    public float getProgressCostPerAction() {
        return progressCostPerAction;
    }
    public void setProgressCostPerAction(float progressCostPerAction) {
        this.progressCostPerAction = progressCostPerAction;
    }
    public float getEnergyCostPerProgress() {
        return energyCostPerProgress;
    }
    public void setEnergyCostPerProgress(float energyCostPerProgress) {
        this.energyCostPerProgress = energyCostPerProgress;
    }
}
