package com.dodge.thermocouple.reader;

import java.io.IOException;
import java.text.DecimalFormat;

import org.apache.commons.io.IOUtils;

import com.dodge.thermocouple.reader.pojo.TemperatureReading;

public class SequentThermoreaderManager {

	private static final DecimalFormat FORMAT = new DecimalFormat("0.0");

	public TemperatureReading getTemps() throws IOException {
		TemperatureReading r = new TemperatureReading();
		r.addTemperature(getReading("1"));
		r.addTemperature(getReading("2"));
		r.addTemperature(getReading("3"));
		r.addTemperature(getReading("4"));
		r.addTemperature(getReading("5"));
		return r;
	}

	private Double getReading(String id) throws IOException {
		Process p = new ProcessBuilder("/usr/local/bin/smtc", "0", "read", id).start();
		String stdout = IOUtils.toString(p.getInputStream());
		Double c = Double.parseDouble(stdout);
		Double f = (c * 9 / 5) + 32.0;
        if(id.equals("4")){
            //house temp is off
            f = f*.98;
        }
		return Double.parseDouble(FORMAT.format(f));

	}

	public static void main(String... strings) throws IOException {
		SequentThermoreaderManager m = new SequentThermoreaderManager();
		m.getTemps();
	}

}
