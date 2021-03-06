package com.builtbroken.atomic.content.effects.effects;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 *
 * Created by Dark(DarkGuardsman, Robert) on 9/30/2018.
 */
public class REOPotion extends RadiationEffectOutcome
{
    private BiFunction<EntityLivingBase, Float, Float> chance;
    private Function<EntityLivingBase, PotionEffect> effectFactory;

    public REOPotion(FloatSupplier radiationStartPoint, BiFunction<EntityLivingBase, Float, Float> chance, Function<EntityLivingBase, PotionEffect> effectFactory)
    {
        super(radiationStartPoint);
        this.chance = chance;
        this.effectFactory = effectFactory;
    }

    @Override
    public void applyEffects(EntityLivingBase entity, float currentRems, float environmentalRems)
    {
        super.applyEffects(entity, currentRems, environmentalRems);
        //Check chance
        if (chance.apply(entity, currentRems) > entity.getEntityWorld().rand.nextFloat())
        {
            //Get effect
            PotionEffect effect = effectFactory.apply(entity);

            //Ensure effect is valid
            if (effect != null && effect.getPotion() != null &&
                    //Check if effect should be applied
                    (entity.getActivePotionEffect(effect.getPotion()) == null || entity.getActivePotionEffect(effect.getPotion()).getDuration() < 10))
            {
                entity.addPotionEffect(effect);
            }
        }
    }
}
