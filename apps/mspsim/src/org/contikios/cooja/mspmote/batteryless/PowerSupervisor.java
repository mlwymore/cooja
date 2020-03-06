package org.contikios.cooja.mspmote.batteryless;

public abstract class PowerSupervisor {
    // the discrete interval of power charging, in u second.
    public final static int CHARGING_EXECUTE_DURATION_US = 1;

    public final double MAX_ENERGY; // in J


    // the unit of this variable is dependent on its subclass
    public double currentEnergy = 0;

    public double deathThreshold;
    public double restartThrshold;

    public PowerSupervisor(int maxEnergy, int deathThreshold, int restartThrshold, int curEnergy){
        this.MAX_ENERGY = maxEnergy;
        this.deathThreshold = deathThreshold;
        this.restartThrshold = restartThrshold;
        this.currentEnergy = curEnergy;
    }

    public boolean isTimeToRestart(){
        if(currentEnergy >= restartThrshold){
            return true;
        }else{
            return false;
        }
    }

    public boolean isTimeToDie(){
        if(currentEnergy <= deathThreshold){
            return true;
        }else{
            return false;
        }
    }

    // keep charging for a time interval with length "chargingInterval"
    public abstract void keepHarvestingEnergy();

    public abstract void keepConsumingEnergy();
}