package com.dodge.thermocouple.reader.pojo;

import java.util.List;

public class LoggerReading {

	private MetaData md = null;

	public MetaData getMetadata() {
		return md;
	}

	public List<TemperatureReading> getTempList() {
		return tr;
	}

	private List<TemperatureReading> tr = null;

	public LoggerReading(MetaData m, List<TemperatureReading> r) {
		md = m;
		tr = r;
	}

	public String toString() {

		StringBuffer buf = new StringBuffer();

		buf.append(md.toString());
		for (TemperatureReading r : tr) {
			buf.append(r + "\n");
		}
		return buf.toString();
	}

	public String toXML() {

		StringBuffer buf = new StringBuffer();

		buf.append(md.toString());
		for (TemperatureReading r : tr) {
			buf.append(r + "\n");
		}
		return buf.toString();
	}
}
