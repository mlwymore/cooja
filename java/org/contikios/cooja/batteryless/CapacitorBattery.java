package org.contikios.cooja.betteryless;

import java.util.Collection;

import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.jdom.Element;

import org.contikios.cooja.ClassDescription;
import org.contikios.cooja.Mote;
import org.contikios.cooja.MoteInterface;


/**
 * A Battery represents the energy source for a mote.
 * The previous battery mote interface implementation was removed,
 * awaiting a new more powerful design connected to Contiki's power profiler.
 * The current code does not monitor the energy consumption of simulated motes.
 *
 * @see MoteInterface
 *
 * @author Fredrik Osterlind
 */
@ClassDescription("CapacitorBattery")
public class CapacitorBattery extends Battery {
    private static Logger logger = Logger.getLogger(CapacitorBattery.class);

    protected int energy = 0;
    protected final int CAPACITOR;
    protected int valtage; // in V
    protected int q = 0; // quantity of electric charge

    /**
     * @param mote Mote
     */
    public CapacitorBattery(Mote mote) {
    }

    public int getRemainingEnergy(){
        return this.energy;
    }

    public int getQuantityOfE(){
        return this.q
    }

}