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
    private static Logger logger = Logger.getLogger(BatterylessMspMote.class);
    public notEnoughPower = false;
    public int energy = 0;

    public int deathThreshold;
    public int restartThrshold;


    // to simulate the situation where a node dies without power
    public void die(){
        // method 1
        stopNextInstruction();

        // method 2
        if (executeMoteEvent.isScheduled()) {
            /* Reschedule wakeup mote event */
            /*logger.info("Rescheduled wakeup from " + executeMoteEvent.getTime() + " to " + time);*/
            executeMoteEvent.remove();
        }
    }

    public void restart(){

        requestImmediateWakeup();
        simulation.scheduleEvent(executeMoteEvent, time);
    }

    public void harvestEnergy(){

    }

    public void checkWhetherRestart(){
        if(energy >= restartThrshold){
            restart();
        }


    }



}