package org.contikios.cooja.mspmote.batteryless;

public class Capacitor implements Chargeable, Dischargeable {
    public final double CAPACITOR_VALUE; // in pf
    public double q; // quantity of electricity
    public double voltage;

    public void chargeFor(long t){

    }

    public void dischargeFor(long t){

    }

    public double getCurrentEnergy(){
        return 0.5*CAPACITOR_VALUE*voltage*voltage;
    }
}