package com.dodge.thermocouple.daemon;

import com.dodge.furnace.Pump;
import com.dodge.thermocouple.persistence.CloudwatchPersister;
import com.dodge.thermocouple.reader.DataLoggerManager;
import com.dodge.thermocouple.reader.SequentThermoreaderManager;
import com.dodge.thermocouple.reader.pojo.LoggerReading;
import com.dodge.thermocouple.reader.pojo.TemperatureReading;

public class LoggerDaemon implements Runnable {


    public void run() {
        System.out.println("Fetching...");
        long startTime = System.currentTimeMillis();
        
        //DataLoggerManager mgr = new DataLoggerManager();
        SequentThermoreaderManager mgr = new SequentThermoreaderManager();
        
        CloudwatchPersister persister = new CloudwatchPersister();
        //FurnaceReader furnace = new FurnaceReader();
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
//        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
//        GregorianCalendar cal = new GregorianCalendar();
//        int secs = cal.get(GregorianCalendar.SECOND);
//        System.out.println("Delaying: " +(60-secs));
        LoggerDaemon d = new LoggerDaemon();        
//        executor.scheduleAtFixedRate(d, (60-secs+2), 60, TimeUnit.SECONDS);
        d.run();  
        //force exit, serial lib keeps alive
        System.exit(0);
    }

}
