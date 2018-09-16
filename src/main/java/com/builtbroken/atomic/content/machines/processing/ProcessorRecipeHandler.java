package com.builtbroken.atomic.content.machines.processing;

import com.builtbroken.atomic.config.ConfigRecipe;
import com.builtbroken.atomic.content.ASFluids;
import com.builtbroken.atomic.content.ASItems;
import com.builtbroken.atomic.content.machines.processing.boiler.TileEntityChemBoiler;
import com.builtbroken.atomic.content.machines.processing.boiler.RecipeChemBoiler;
import com.builtbroken.atomic.content.machines.processing.centrifuge.TileEntityChemCentrifuge;
import com.builtbroken.atomic.content.machines.processing.centrifuge.recipe.RecipeConWater;
import com.builtbroken.atomic.content.machines.processing.centrifuge.recipe.RecipeUraniumPellet;
import com.builtbroken.atomic.content.machines.processing.extractor.TileEntityChemExtractor;
import com.builtbroken.atomic.content.machines.processing.extractor.recipe.DustLootTable;
import com.builtbroken.atomic.content.machines.processing.extractor.recipe.RecipeWasteExtracting;
import com.builtbroken.atomic.content.machines.processing.extractor.recipe.RecipeYellowcake;
import com.builtbroken.atomic.content.machines.processing.recipes.ProcessingRecipe;
import com.builtbroken.atomic.content.machines.processing.recipes.ProcessingRecipeList;
import com.builtbroken.atomic.proxy.ProxyLoader;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 5/25/2018.
 */
public final class ProcessorRecipeHandler extends ProxyLoader
{
    public final ProcessingRecipeList<TileEntityChemExtractor, ProcessingRecipe<TileEntityChemExtractor>> chemExtractorProcessingRecipe;
    public final ProcessingRecipeList<TileEntityChemBoiler, RecipeChemBoiler> chemBoilerProcessingRecipe;
    public final ProcessingRecipeList<TileEntityChemCentrifuge, ProcessingRecipe<TileEntityChemCentrifuge>> chemCentrifugeProcessingRecipe;

    public static final ProcessorRecipeHandler INSTANCE = new ProcessorRecipeHandler();

    private ProcessorRecipeHandler()
    {
        super("processing.machines");
        add(DustLootTable.INSTANCE);
        add(chemExtractorProcessingRecipe = new ProcessingRecipeList("chem.extractor.recipes"));
        add(chemBoilerProcessingRecipe = new ProcessingRecipeList("chem.boiler.recipes"));
        add(chemCentrifugeProcessingRecipe = new ProcessingRecipeList("chem.centrifuge.recipes"));
    }

    @Override
    public void init()
    {
        //Extractor
        chemExtractorProcessingRecipe.add(new RecipeWasteExtracting());
        chemExtractorProcessingRecipe.add(new RecipeYellowcake());

        //Recipe sludge -> mineral water + waste
        chemBoilerProcessingRecipe.add(new RecipeChemBoiler(ItemStack.EMPTY,
                new ItemStack(ASItems.itemProcessingWaste, ConfigRecipe.LIQUID_WASTE_SOLID_WASTE, 0),
                new FluidStack(ASFluids.LIQUID_MINERAL_WASTE.fluid,
                        ConfigRecipe.LIQUID_WASTE_CONSUMED_PER_BOIL),
                new FluidStack(ASFluids.CONTAMINATED_MINERAL_WATER.fluid,
                        ConfigRecipe.LIQUID_WASTE_CONSUMED_PER_BOIL * ConfigRecipe.LIQUID_WASTE_PRODUCED_TO_WATER), null));

        //Uranium Ore -> Uranium hex
        chemBoilerProcessingRecipe.add(new RecipeChemBoiler("oreUranium",
                new ItemStack(ASItems.itemProcessingWaste, ConfigRecipe.SOLID_WASTE_URANIUM_ORE, 0),
                new FluidStack(FluidRegistry.WATER, ConfigRecipe.WATER_BOIL_URANIUM_ORE),
                new FluidStack(ASFluids.CONTAMINATED_MINERAL_WATER.fluid, ConfigRecipe.CON_WATER_URANIUM_ORE),
                new FluidStack(ASFluids.URANIUM_HEXAFLOURIDE.fluid, ConfigRecipe.HEX_OUT_URANIUM_ORE)));

        //Yellow cake -> Uranium Hex
        chemBoilerProcessingRecipe.add(new RecipeChemBoiler( new ItemStack(ASItems.itemYellowCake, 1, 0),
                new ItemStack(ASItems.itemProcessingWaste, ConfigRecipe.SOLID_WASTE_YELLOWCAKE, 0),
                new FluidStack(FluidRegistry.WATER, ConfigRecipe.WATER_BOIL_YELLOWCAKE),
                new FluidStack(ASFluids.CONTAMINATED_MINERAL_WATER.fluid,  ConfigRecipe.CON_WATER_YELLOWCAKE),
                new FluidStack(ASFluids.URANIUM_HEXAFLOURIDE.fluid, ConfigRecipe.HEX_OUT_YELLOWCAKE)));

        //Centrifuge
        chemCentrifugeProcessingRecipe.add(new RecipeConWater());
        chemCentrifugeProcessingRecipe.add(new RecipeUraniumPellet());
    }
}
