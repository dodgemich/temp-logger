package com.dodge.thermocouple.daemon;

import java.io.IOException;
import java.text.DecimalFormat;

import org.apache.commons.io.IOUtils;

import com.dodge.furnace.Pump;
import com.dodge.thermocouple.persistence.CloudwatchPersister;
import com.dodge.thermocouple.reader.SequentThermoreaderManager;
import com.dodge.thermocouple.reader.pojo.TemperatureReading;

import com.pi4j.component.lcd.LCDTextAlignment;
import com.pi4j.component.lcd.impl.GpioLcdDisplay;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.RaspiPin;

public class LoggerDaemon implements Runnable {

	private static final DecimalFormat FORMAT = new DecimalFormat("0.0");

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

	private Double getReading(String id) throws IOException {
		Process p = new ProcessBuilder("/usr/local/bin/smtc", "0", "read", id).start();
		String stdout = IOUtils.toString(p.getInputStream());
		Double c = Double.parseDouble(stdout);
		Double f = (c * 9 / 5) + 32.0;
		if (id.equals("4")) {
			// house temp is off
			f = f * .97;
		}
		return Double.parseDouble(FORMAT.format(f));

	}

	public void updateDisplay() {


        System.out.println("16X2 LCD Example with Raspberry Pi using Pi4J and JAVA");

        // create gpio controller
        final GpioController gpio = GpioFactory.getInstance();

        // initialize LCD
        final GpioLcdDisplay lcd = new GpioLcdDisplay(2,    // number of row supported by LCD
                                                16,       // number of columns supported by LCD
                                                RaspiPin.GPIO_23,  // LCD RS pin
                                                RaspiPin.GPIO_24,  // LCD strobe pin
                                                RaspiPin.GPIO_04,  // LCD data bit D4
                                                RaspiPin.GPIO_17,  // LCD data bit D5
                                                RaspiPin.GPIO_27,  // LCD data bit D6
                                                RaspiPin.GPIO_22); // LCD data bit D7

        lcd.clear();
        Thread.sleep(1000);

        lcd.write(LCD_ROW_1, "WeArGenius");
        lcd.write(LCD_ROW_2, " ???");

        // Thread.sleep(2000);
        // for (String ipAddress : NetworkInfo.getIPAddresses()){
        //     System.out.println("IP Addresses      :  " + ipAddress);
        //     lcd.writeln(LCD_ROW_2,ipAddress,LCDTextAlignment.ALIGN_CENTER);
        // }

        gpio.shutdown();
	}

	public static void main(String[] args) {
		LoggerDaemon d = new LoggerDaemon();
		if (args.length == 0) {
			d.run();
			System.exit(0);
		} else {
			d.updateDisplay();
		}
	}

}
