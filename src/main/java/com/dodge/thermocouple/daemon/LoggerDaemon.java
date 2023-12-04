package com.dodge.thermocouple.daemon;

import java.io.IOException;
import java.text.DecimalFormat;

import org.apache.commons.io.IOUtils;

import com.dodge.furnace.Pump;
import com.dodge.thermocouple.persistence.CloudwatchPersister;
import com.dodge.thermocouple.reader.SequentThermoreaderManager;
import com.dodge.thermocouple.reader.pojo.TemperatureReading;
import com.pi4j.Pi4J;
import com.pi4j.library.pigpio.PiGpio;
import com.pi4j.plugin.pigpio.provider.gpio.digital.PiGpioDigitalInputProvider;
import com.pi4j.plugin.pigpio.provider.gpio.digital.PiGpioDigitalOutputProvider;
import com.pi4j.plugin.raspberrypi.platform.RaspberryPiPlatform;

public class LoggerDaemon implements Runnable {


	public void run() {
		System.out.println("Fetching...");
		long startTime = System.currentTimeMillis();

		// DataLoggerManager mgr = new DataLoggerManager();
		SequentThermoreaderManager mgr = new SequentThermoreaderManager();

		CloudwatchPersister persister = new CloudwatchPersister();
		// FurnaceReader furnace = new FurnaceReader();
		try {

			TemperatureReading tempRd = mgr.getTemps();
			System.out.println(tempRd);
			persister.persist(tempRd);

			System.out.println("Time spent: " + (System.currentTimeMillis() - startTime));
			persister.persistIot();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(e.toString());
			System.out.println("connection closed");
		}

//        try {
//            persister.persist(furnace.read());
//        } catch (Exception e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//            System.out.println(e.toString());
//        }

		try {
			Pump p = new Pump();
			p.managePumpState();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(e.toString());
		}

	}


	public static void main(String[] args) {
		LoggerDaemon d = new LoggerDaemon();
		if (args.length == 0) {
			d.run();
			System.exit(0);
		} else {
			//d.updateDisplay();
		}
	}

}
