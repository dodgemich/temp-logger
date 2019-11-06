package com.dodge.thermocouple.reader.pojo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TemperatureReading {

    private List<Double> values = new ArrayList<Double>();
    private Date readingTime;

 

      

    public void addTemperature(Double d) {
        values.add(d);
    }

    public void setTime(Date d) {
        readingTime = d;
    }

    public List<Double> getTemps() {
        return values;
    }

    public Date getTime() {
        return readingTime;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        if (readingTime != null) {
            buf.append(readingTime.toString() + " : ");
        }
        buf.append(values.toString());
        return buf.toString();
    }
}
