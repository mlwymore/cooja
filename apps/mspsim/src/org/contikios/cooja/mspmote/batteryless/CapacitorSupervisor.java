package org.contikios.cooja.mspmote.batteryless;

public class CapacitorSupervisor extends PowerSupervisor {
    public Capacitor capacitor;

    /**
     *  Update the "currentEnergy" during the next execute period (CHARGING_EXECUTE_DURATION_US)
     */
    public void keepHarvestingEnergy(){
        // update the Energy in "CHARGING_EXECUTE_DURATION_US" interval based on the capacitor model
        capacitor.chargeFor(CHARGING_EXECUTE_DURATION_US);
        currentEnergy = capacitor.getCurrentEnergy();
    }

    public void keepConsumingEnergy(){
        capacitor.dischargeFor(CHARGING_EXECUTE_DURATION_US);
        currentEnergy = capacitor.getCurrentEnergy();

    }
}