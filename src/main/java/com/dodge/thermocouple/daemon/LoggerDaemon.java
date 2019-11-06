package com.dodge.thermocouple.daemon;

import java.util.GregorianCalendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.dodge.furnace.FurnaceReader;
import com.dodge.furnace.Pump;
import com.dodge.thermocouple.persistence.CloudwatchPersister;
import com.dodge.thermocouple.reader.DataLoggerManager;
import com.dodge.thermocouple.reader.pojo.LoggerReading;
import com.dodge.thermocouple.reader.pojo.TemperatureReading;

public class LoggerDaemon implements Runnable {


    public void run() {
        System.out.println("Fetching...");
        long startTime = System.currentTimeMillis();
        DataLoggerManager mgr = new DataLoggerManager();
        CloudwatchPersister persister = new CloudwatchPersister();
        //FurnaceReader furnace = new FurnaceReader();
        try {
            mgr.openConnection();
            LoggerReading lastRead = mgr.downloadData();
            
            TemperatureReading tempRd = lastRead.getTempList().get(lastRead.getTempList().size() - 1);
            System.out.println(tempRd);
            mgr.closeConnection();
            persister.persist(tempRd);
            
            System.out.println("Time spent: " + (System.currentTimeMillis() - startTime));
            persister.persistIot();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println(e.toString());
            mgr.closeConnection();
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
//        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
//        GregorianCalendar cal = new GregorianCalendar();
//        int secs = cal.get(GregorianCalendar.SECOND);
//        System.out.println("Delaying: " +(60-secs));
        LoggerDaemon d = new LoggerDaemon();        
//        executor.scheduleAtFixedRate(d, (60-secs+2), 60, TimeUnit.SECONDS);
        d.run();  
    }

}
