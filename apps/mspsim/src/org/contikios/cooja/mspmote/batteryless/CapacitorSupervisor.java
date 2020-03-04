package org.contikios.cooja.mspmote.batteryless;

public class CapacitorSupervisor extends PowerSupervisor {
    public final int CAPACITOR_VALUE;

    public void keepHarvestingEnergy(){
        // compute delta Energy in "CHARGING_EXECUTE_DURATION_US" interval based on the capacitor model
        int deltaEnergy = 0;
        //...

        currentEnergy += deltaEnergy;
    }

    public void keepConsumingEnergy(){

    }
}