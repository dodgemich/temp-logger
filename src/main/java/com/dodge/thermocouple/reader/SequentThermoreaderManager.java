package com.dodge.thermocouple.reader;

import java.io.IOException;

import org.apache.commons.io.IOUtils;

import com.dodge.thermocouple.reader.pojo.TemperatureReading;

public class SequentThermoreaderManager {
	
	
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
		Process p = new ProcessBuilder("smtc", "0", "read", id).start();
		String stdout = IOUtils.toString(p.getInputStream());
		System.out.println(stdout);
		Double c =  Double.parseDouble(stdout);
		return (c*9/5)+32.0;
	
	}
	
	public static void main(String... strings) throws IOException {
		SequentThermoreaderManager m = new SequentThermoreaderManager();
		m.getTemps();
	}

}
