package com.dodge.display;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.Duration;

import org.apache.commons.io.IOUtils;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.library.pigpio.PiGpio;
import com.pi4j.plugin.linuxfs.provider.i2c.LinuxFsI2CProvider;
import com.pi4j.plugin.pigpio.provider.gpio.digital.PiGpioDigitalInputProvider;
import com.pi4j.plugin.pigpio.provider.gpio.digital.PiGpioDigitalOutputProvider;
import com.pi4j.plugin.pigpio.provider.pwm.PiGpioPwmProvider;
import com.pi4j.plugin.pigpio.provider.serial.PiGpioSerialProvider;
import com.pi4j.plugin.pigpio.provider.spi.PiGpioSpiProvider;
import com.pi4j.plugin.raspberrypi.platform.RaspberryPiPlatform;

public class ScreenUpdater {
	private Context pi4j;
	private static final DecimalFormat FORMAT = new DecimalFormat("0.0");

	
	public ScreenUpdater() {
		PiGpio piGpio = PiGpio.newNativeInstance();

		pi4j = Pi4J.newContextBuilder()
				.noAutoDetect()
				.add(new RaspberryPiPlatform() {
					@Override
					protected String[] getProviders() {
						return new String[]{};
					}
				})
				.add(PiGpioDigitalInputProvider.newInstance(piGpio),
						PiGpioDigitalOutputProvider.newInstance(piGpio),
						PiGpioPwmProvider.newInstance(piGpio),
						PiGpioSerialProvider.newInstance(piGpio),
						PiGpioSpiProvider.newInstance(piGpio),
						LinuxFsI2CProvider.newInstance()
				)
				.build();
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
	
	
    public void execute() {
        System.out.println("LCD demo started");

        //Create a Component, with amount of ROWS and COLUMNS of the device
        //LcdDisplay lcd = new LcdDisplay(pi4j); //2x16 is default
        LcdDisplay lcd = new LcdDisplay(pi4j, 2, 16);
        lcd.clearDisplay();
        // Write text to specific position
        lcd.displayLineOfText("Hello" , 0);
        lcd.displayLineOfText("World!", 1, 3);



        //lcd.clearDisplay();

        //lcd.centerTextInLine("Hi", 0);


    }

}
