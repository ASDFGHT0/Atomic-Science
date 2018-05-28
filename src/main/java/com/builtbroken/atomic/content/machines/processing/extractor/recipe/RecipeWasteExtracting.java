package com.builtbroken.atomic.content.machines.processing.extractor.recipe;

import com.builtbroken.atomic.content.ASItems;
import com.builtbroken.atomic.content.machines.processing.extractor.TileEntityChemExtractor;
import com.builtbroken.atomic.content.machines.processing.recipes.ProcessingRecipe;
import net.minecraft.item.ItemStack;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 5/24/2018.
 */
public class RecipeWasteExtracting extends ProcessingRecipe<TileEntityChemExtractor>
{
    @Override
    public boolean matches(TileEntityChemExtractor machine)
    {
        ItemStack stack = machine.getStackInSlot(TileEntityChemExtractor.SLOT_ITEM_INPUT);
        return stack != null && ASItems.itemProcessingWaste == stack.getItem();
    }

    @Override
    public boolean applyRecipe(TileEntityChemExtractor machine)
    {
        ItemStack inputItem = machine.getStackInSlot(TileEntityChemExtractor.SLOT_ITEM_INPUT);
        if (inputItem != null && ASItems.itemProcessingWaste == inputItem.getItem())
        {
            ItemStack outputStack;

            if (Math.random() > 0.4) //TODO switch over to progress bar/tank so output always contains toxic waste dust
            {
                outputStack = new ItemStack(ASItems.itemToxicWaste, 1, 0);
            }
            else //TODO add stone dust with high drop rate
            {
                outputStack = RecipeWasteExtracting.getRandomDust();
            }

            if (machine.hasSpaceInOutput(outputStack, TileEntityChemExtractor.SLOT_ITEM_OUTPUT))
            {
                machine.decrStackSize(TileEntityChemExtractor.SLOT_ITEM_INPUT, 1);
                machine.addToOutput(outputStack, TileEntityChemExtractor.SLOT_ITEM_OUTPUT);
            }
            return true;
        }
        return false;
    }


    /**
     * Gets a random dust from the loot table
     *
     * @return random entry from the table, or null if ran out of entries to loop (rare)
     */
    public static ItemStack getRandomDust()
    {
        return DustLootTable.INSTANCE.getRandomItemStack();
    }
}
