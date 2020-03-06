package org.contikios.cooja.mspmote.batteryless;

public interface Chargeable {
    /**
     * Keep charging for a certain amount of time
     * @param t: The time interval the capacitor keep charging for.
     */
    public void chargeFor(long t);
}