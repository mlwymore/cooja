package org.contikios.cooja.mspmote.batteryless;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.jdom.Element;
import java.org.contikios.cooja.ContikiError;
import org.contikios.cooja.Cooja;
import org.contikios.cooja.Mote;
import org.contikios.cooja.MoteInterface;
import org.contikios.cooja.MoteInterfaceHandler;
import org.contikios.cooja.MoteType;
import org.contikios.cooja.Simulation;
import org.contikios.cooja.Watchpoint;
import org.contikios.cooja.WatchpointMote;
import org.contikios.cooja.interfaces.IPAddress;
import org.contikios.cooja.mote.memory.MemoryInterface;
import org.contikios.cooja.motes.AbstractEmulatedMote;
import org.contikios.cooja.mspmote.interfaces.Msp802154Radio;
import org.contikios.cooja.mspmote.interfaces.MspSerial;
import org.contikios.cooja.mspmote.plugins.CodeVisualizerSkin;
import org.contikios.cooja.mspmote.plugins.MspBreakpoint;
import org.contikios.cooja.plugins.Visualizer;


import se.sics.mspsim.cli.CommandContext;
import se.sics.mspsim.cli.CommandHandler;
import se.sics.mspsim.cli.LineListener;
import se.sics.mspsim.cli.LineOutputStream;
import se.sics.mspsim.core.EmulationException;
import se.sics.mspsim.core.LogListener;
import se.sics.mspsim.core.Loggable;
import se.sics.mspsim.core.MSP430;
import se.sics.mspsim.core.EmulationLogger.WarningType;
import se.sics.mspsim.platform.GenericNode;
import se.sics.mspsim.ui.ManagedWindow;
import se.sics.mspsim.ui.WindowManager;
import se.sics.mspsim.util.ComponentRegistry;
import se.sics.mspsim.util.ConfigManager;
import se.sics.mspsim.util.DebugInfo;
import se.sics.mspsim.util.ELF;
import se.sics.mspsim.util.MapEntry;
import se.sics.mspsim.util.MapTable;
import se.sics.mspsim.profiler.SimpleProfiler;

import org.contikios.cooja.mspmote.interfaces.MspClock;


public abstract class BatterylessMspMote extends MspMote {
    // the discrete interval of power charging, in u second.
    public final static int CHARGING_EXECUTE_DURATION_US = 1;

    private static Logger logger = Logger.getLogger(BatterylessMspMote.class);

    public PowerSupervisor ps;
    public boolean flagDeath = false;

    public BatterylessMspMote(MspMoteType moteType, Simulation simulation, PowerSupervisor powerSupervisor) {
        super(moteType, simulation);
        this.ps = powerSupervisor;
    }

    @override
    public void execute(long t, int duration) {
        MspClock clock = ((MspClock) (myMoteInterfaceHandler.getClock()));
        double deviation = clock.getDeviation();
        long drift = clock.getDrift();

        /* Wait until mote boots */
        if (!booted && clock.getTime() < 0) {
            scheduleNextWakeup(t - clock.getTime());
            return;
        }
        booted = true;

        if (stopNextInstruction) {
            stopNextInstruction = false;
            scheduleNextWakeup(t);
            throw new RuntimeException("MSPSim requested simulation stop");
        }

        if (lastExecute < 0) {
            /* Always execute one microsecond the first time */
            lastExecute = t;
        }
        if (t < lastExecute) {
            throw new RuntimeException("Bad event ordering: " + lastExecute + " < " + t);
        }

        // Shen: this part seems to rectify the timeline due to "clock deviation"
        if (((1-deviation) * executed) > skipped) {
            lastExecute = lastExecute + duration; // (t+duration) - (t-lastExecute);
            nextExecute = t+duration;
            skipped += duration;
            scheduleNextWakeup(nextExecute);
        }

        /**
         * This following part is added by Shen. At here we add the power supervisor to check some additional power conditions
         * and deterimine whether to put off the execution of mote simulatiom.
         * */
        // --------------------------------------------------------------------------
        if(flagDeath){
            if(ps.isTimeToRestart()){
                restart();
            }else{
                keepDeathAndHarvestEnergy(t);
                return;
            }
            // return; ?
        }

        // if have power
        if (ps.isTimeToDie()){
            flagDeath = true;
            keepDeathAndHarvestEnergy(t);
            return;
        }

        // if have power and not death. Then let the power supervisor consume energy
        // and execute the mote normally as below
        ps.keepConsumingEnergy();

        // ---------------------------------------------------------------------------

        /* Execute MSPSim-based mote */
        /* TODO Try-catch overhead */
        try {
            nextExecute = myCpu.stepMicros(Math.max(0, t-lastExecute), duration) + t + duration;
            lastExecute = t;
        } catch (EmulationException e) {
            String trace = e.getMessage() + "\n\n" + getStackTrace();
            throw (ContikiError)
                    new ContikiError(trace).initCause(e);
        }

        /* Schedule wakeup */
        if (nextExecute < t) {
            throw new RuntimeException(t + ": MSPSim requested early wakeup: " + nextExecute);
        }

        /*logger.debug(t + ": Schedule next wakeup at " + nextExecute);*/
        executed += duration;
        scheduleNextWakeup(nextExecute);

        if (stopNextInstruction) {
            stopNextInstruction = false;
            throw new RuntimeException("MSPSim requested simulation stop");
        }
    }

    // to simulate the situation where a node dies without power
    public void keepDeathAndHarvestEnergy(long t){
        // clear all the previously scheduled execute events
        if (executeMoteEvent.isScheduled()) {
            /* Reschedule wakeup mote event */
            /*logger.info("Rescheduled wakeup from " + executeMoteEvent.getTime() + " to " + time);*/
            executeMoteEvent.remove();
        }

        // make the powerSupervisor keep chargning for "CHARGING_EXECUTE_DURATION_US" time
        ps.keepHarvestingEnergy();

        // make the cpu and other relevant interfaces die and simulate their properties under charging
        // modify the clock?
        // ...

        // schedule next event to excute. Before that, the node keep death and charging.
        nextExecute = t + ps.CHARGING_EXECUTE_DURATION_US;
        lastExecute = t;
        executed += ps.CHARGING_EXECUTE_DURATION_US; // not sure whether we need this line
        scheduleNextWakeup(nextExecute);


        // alternative method, but i think it will stop the whole simulation rather than just the mote.
        // stopNextInstruction();
    }

    public void restart(){
        // get current time
        long curTime = executeMoteEvent.getTime();

        flagDeath = false;
        requestImmediateWakeup();

    }



//    // key line inside 'scheduleNextWakeup' method
//        simulation.scheduleEvent(executeMoteEvent, curTime);



}