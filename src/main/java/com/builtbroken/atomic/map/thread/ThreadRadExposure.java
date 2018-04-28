package com.builtbroken.atomic.map.thread;

import com.builtbroken.atomic.AtomicScience;
import com.builtbroken.atomic.config.ConfigRadiation;
import com.builtbroken.atomic.map.RadiationChunk;
import com.builtbroken.atomic.map.RadiationLayer;
import com.builtbroken.atomic.map.RadiationMap;
import com.builtbroken.atomic.map.RadiationSystem;
import com.builtbroken.jlib.lang.StringHelpers;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Handles updating the radiation map
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 4/28/2018.
 */
public class ThreadRadExposure extends Thread
{
    public boolean shouldRun = true;
    public ConcurrentLinkedQueue<RadChange> changeQueue = new ConcurrentLinkedQueue();
    public ConcurrentLinkedQueue<RadiationChunk> addScanQueue = new ConcurrentLinkedQueue();
    public ConcurrentLinkedQueue<RadiationChunk> removeScanQueue = new ConcurrentLinkedQueue();

    @Override
    public void start()
    {
        shouldRun = true;
        AtomicScience.logger.info("ThreadRadExposure: Starting thread");
        super.start();
    }

    @Override
    public void run()
    {
        while (shouldRun)
        {
            try
            {
                //Cleanup of chunks, remove data from chunk so when re-added later it doesn't spike values
                while (!removeScanQueue.isEmpty())
                {
                    queueRemove(removeScanQueue.poll());
                }

                //New additions, update data for values
                while (!addScanQueue.isEmpty())
                {
                    queueAddition(addScanQueue.poll());
                }

                //Stop looping if we have chunks to scan, only loop if we have something to do
                while (!changeQueue.isEmpty() && addScanQueue.isEmpty() && removeScanQueue.isEmpty())
                {
                    RadChange change = changeQueue.poll();
                    if (change != null)
                    {
                        updateLocation(change);
                    }
                }

                //Nothing left to do, then sleep for 1 second before checking on updates
                sleep(1000);
            }
            catch (Exception e)
            {
                AtomicScience.logger.error("ThreadReadExposure: Unexpected error during operation", e);
            }
        }

        //If stopped, clear all data
        changeQueue.clear();
    }

    /**
     * Called to scan a chunk to add remove calls
     *
     * @param chunk
     */
    protected void queueRemove(RadiationChunk chunk)
    {
        if (chunk != null)
        {
            for (RadiationLayer layer : chunk.getLayers())
            {
                for (int cx = 0; cx < 16; cx++)
                {
                    for (int cz = 0; cz < 16; cz++)
                    {
                        if (layer.getData(cx, cz) > 0)
                        {
                            int x = cx + chunk.xPosition * 16;
                            int z = cz + chunk.xPosition * 16;
                            changeQueue.add(new RadChange(chunk.dimension, x, layer.y_index, z, layer.getData(cx, cz), 0));
                        }
                    }
                }
            }
        }
    }

    /**
     * Called to scan a chunk to add addition calls
     *
     * @param chunk
     */
    protected void queueAddition(RadiationChunk chunk)
    {
        if (chunk != null)
        {
            for (RadiationLayer layer : chunk.getLayers())
            {
                for (int cx = 0; cx < 16; cx++)
                {
                    for (int cz = 0; cz < 16; cz++)
                    {
                        if (layer.getData(cx, cz) > 0)
                        {
                            int x = cx + chunk.xPosition * 16;
                            int z = cz + chunk.xPosition * 16;
                            changeQueue.add(new RadChange(chunk.dimension, x, layer.y_index, z, 0, layer.getData(cx, cz)));
                        }
                    }
                }
            }
        }
    }

    /**
     * Called to update the exposure value at the location
     *
     * @param change
     */
    protected void updateLocation(RadChange change)
    {
        //Get map
        RadiationMap map;
        synchronized (RadiationSystem.INSTANCE)
        {
            map = RadiationSystem.INSTANCE.getExposureMap(change.dim, true);
        }

        long time = System.nanoTime();

        //Clear old values
        removeValue(map, change.old_value, change.x, change.y, change.z);

        //Add new value, completed as a separate step due to range differences
        setValue(map, change.new_value, change.x, change.y, change.z);

        if (AtomicScience.runningAsDev)
        {
            time = System.nanoTime() - time;
            AtomicScience.logger.info(String.format("ThreadRadExposure: %sx %sy %sz | %so %sn | took %s",
                    change.x, change.y, change.z,
                    change.old_value, change.new_value,
                    StringHelpers.formatNanoTime(time)
            ));
        }
    }

    /**
     * Removes the old value from the map
     *
     * @param map       - map to edit
     * @param old_value - old material value
     * @param cx        - change location
     * @param cy        - change location
     * @param cz        - change location
     */
    protected void removeValue(RadiationMap map, int old_value, int cx, int cy, int cz)
    {
        final int rad = getRadFromMaterial(old_value);
        final int edit_range = (int) Math.floor(getDecayRange(rad));
        for (int mx = -edit_range; mx <= edit_range; mx++)
        {
            for (int mz = -edit_range; mz <= edit_range; mz++)
            {
                for (int my = -edit_range; my <= edit_range; my++)
                {
                    int x = cx + mx;
                    int y = cy + my;
                    int z = cz + mz;

                    //Stay inside map
                    if (y >= 0 && y < 256)
                    {
                        //Get data
                        double distance = Math.sqrt(mx * mx + my * my + mz * mz);
                        int current_value = map.getData(x, y, z);

                        //Remove old value
                        current_value -= getRadForDistance(rad, distance);

                        //Save
                        map.setData(x, y, z, Math.max(0, current_value));
                    }
                }
            }
        }
    }

    /**
     * Removes the old value from the map
     *
     * @param map       - map to edit
     * @param old_value - old material value
     * @param cx        - change location
     * @param cy        - change location
     * @param cz        - change location
     */
    protected void setValue(RadiationMap map, int old_value, int cx, int cy, int cz)
    {
        final int rad = getRadFromMaterial(old_value);
        final int edit_range = (int) Math.floor(getDecayRange(rad));
        for (int mx = -edit_range; mx <= edit_range; mx++)
        {
            for (int mz = -edit_range; mz <= edit_range; mz++)
            {
                for (int my = -edit_range; my <= edit_range; my++)
                {
                    int x = cx + mx;
                    int y = cy + my;
                    int z = cz + mz;

                    //Stay inside map
                    if (y >= 0 && y < 256)
                    {
                        //Get data
                        double distance = Math.sqrt(mx * mx + my * my + mz * mz);
                        int current_value = map.getData(x, y, z);

                        //Update value
                        current_value += getRadForDistance(rad, distance);

                        //Save
                        map.setData(x, y, z, Math.max(0, current_value));
                    }
                }
            }
        }
    }

    /**
     * Converts material value to rad
     *
     * @param material_amount - amount of material
     * @return rad value
     */
    protected int getRadFromMaterial(int material_amount)
    {
        return (int) Math.ceil(material_amount * ConfigRadiation.MAP_VALUE_TO_MILI_RAD);
    }

    /**
     * Gets radiation value for the given distance
     *
     * @param power    - ordinal power at 1 meter
     * @param distance - distance to get current
     * @return distance reduced value, if less than 1 will return full
     */
    protected int getRadForDistance(int power, double distance)
    {
        if (distance < 1)
        {
            return power;
        }
        return (int) Math.ceil(power * (1 / (distance * distance))); //its assumed power is measured at 1 meter from source
    }

    /**
     * At what point does radiation power drop below 1
     *
     * @param value - starting value
     * @return distance
     */
    protected double getDecayRange(int value)
    {
        double power = value;
        double distance = 1;
        while (power > 1)
        {
            distance += 0.5;
            power = getRadForDistance(value, distance);
        }
        return distance;
    }

    public void kill()
    {
        shouldRun = false;
        AtomicScience.logger.info("ThreadRadExposure: Stopping thread");
    }

    public void removeChunk(RadiationChunk chunk)
    {

    }

    public void addChunk(RadiationChunk chunk)
    {

    }
}