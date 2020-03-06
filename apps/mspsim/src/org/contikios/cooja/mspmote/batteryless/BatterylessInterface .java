package org.contikios.cooja.mspmote.batteryless;

public interface BatteylessInterface {
    public void keepDeathAndHarvestEnergy(long t);

    public void restart();
}