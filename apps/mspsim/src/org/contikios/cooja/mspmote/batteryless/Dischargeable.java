package org.contikios.cooja.mspmote.batteryless;

public interface Dischargeable {
    /**
     * Keep discharging for a certain amount of time
     * @param t: The time interval the capacitor keep discharging for.
     */
    public void dischargeFor(long t);
}