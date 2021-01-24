package com.dodge.thermocouple.reader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.dodge.thermocouple.reader.pojo.LoggerReading;
import com.dodge.thermocouple.reader.pojo.MetaData;
import com.dodge.thermocouple.reader.pojo.TemperatureReading;

import com.fazecast.jSerialComm.SerialPort;

/**
 * A class that handles the details of a serial connection. Reads from one
 * TextArea and writes to a second TextArea. Holds the state of the connection.
 */
public class DataLoggerManager {// implements SerialPortEventListener,
                                // CommPortOwnershipListener {

    // private SerialParameters parameters;
    private OutputStream os;

    public OutputStream getOs() {
        return os;
    }

    private InputStream is;

    public InputStream getIs() {
        return is;
    }

    
    private SerialPort sPort;
    private boolean open;

    public DataLoggerManager() {
        open = false;
    }

    public void openConnection() throws IOException {
        // Enumeration e = CommPortIdentifier.getPortIdentifiers();
        // while(e.hasMoreElements()){
        // CommPortIdentifier i = (CommPortIdentifier) e.nextElement();
        // System.out.println(i.getName());
        // }
        // Obtain a CommPortIdentifier object for the port you want to open.
    	
//    	
    	String port = "/dev/ttyUSB0";
        System.out.println("Getting port");
        if (System.getProperty("serialPort") != null) {
            port = System.getProperty("serialPort");
            System.out.println("Overriding, port is :" + port);
        } 
        
        sPort = SerialPort.getCommPort(port);
        sPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
        sPort.openPort();
        System.out.println(sPort.isOpen());
        System.out.println("Port open");
        os = sPort.getOutputStream();
        is = sPort.getInputStream();

//        int timeout = 4000;
//        if (System.getProperty("serialTimeout") != null) {
//            timeout = Integer.parseInt(System.getProperty("serialTimeout"));
//            System.out.println("Overriding, timeout is :" + timeout);
//        }

//        sPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 30000, 30000);
        open = true;
    }

    public LoggerReading downloadData() {
        try {
            System.out.println("Send Request");
            sendRequest("*B");
  
            MetaData header = getMetaData();

            List<TemperatureReading> data = getBinaryData(header);

            StringBuffer buf = new StringBuffer();
            for (TemperatureReading rd : data) {
                buf.append(rd + "\n");
            }

            getOs().close();
            getIs().close();

            return new LoggerReading(header, data);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;

    }

    private void sendRequest(String req) {
        PrintStream str = new PrintStream(getOs());
        str.print(req + "\r");// str.print("*D\r");
        str.flush();

        try {
            Thread.sleep(1000); // do nothing for 1000 miliseconds (1 second);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void debugBuf() throws IOException {
        char soh = (char) 1;
        char stx = (char) 2;
        StringBuffer buf = new StringBuffer();
        InputStream instr = getIs();
        int val = instr.read();
        while (val > -1) {
            System.out.print((char) (val));
            val = instr.read();
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    private MetaData getMetaData() throws IOException {
        char soh = (char) 1;
        char stx = (char) 2;
        StringBuffer buf = new StringBuffer();
        InputStream instr = getIs();
        int val = instr.read();
        while (val > -1) {
            if (val == stx) {
                break;
            } else if (val == soh) {
                // ignore
            } else {
                buf.append((char) val);
            }
            val = instr.read();
        }

        MetaData result = new MetaData(buf.toString());

        return result;

    }

    private List<TemperatureReading> getData(MetaData md) throws IOException {
        String data = "";
        InputStream instr = getIs();
        int val = instr.read();

        List<TemperatureReading> result = new ArrayList<TemperatureReading>();

        int count = 0;
        TemperatureReading rdg = new TemperatureReading();

        boolean dataComplete = false;
        while (val > -1) {

            dataComplete = dataComplete || (val == 3);

            if (val == '\r' || val == '\n') {

            } else {
                data = data + (char) val;
                if (data.length() == 4 && !dataComplete) {
                    Double d = new Double(Integer.parseInt(data, 16) / 10.0);
                    rdg.addTemperature(d);
                    count++;
                    if (count == md.getNumberOfThermocouples()) {
                        result.add(rdg);
                        rdg = new TemperatureReading();
                        count = 0;
                    }
                    data = "";
                }
            }
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            val = instr.read();
        }

        long startTime = md.getFirstReadingTime().getTime();
        for (int i = 0; i < result.size(); i++) {
            TemperatureReading curRdg = result.get(i);
            curRdg.setTime(new Date(startTime + (i * md.getLogIntervalInMilliseconds())));
        }

        return result;
    }

    private List<TemperatureReading> getBinaryData(MetaData md) throws IOException {
        String data = "";
        InputStream instr = getIs();
        int val = instr.read();

        List<TemperatureReading> result = new ArrayList<TemperatureReading>();

        int count = 0;
        TemperatureReading rdg = new TemperatureReading();

        while (val > -1) {
            if (result.size() < md.getRecordCount()) {
                int major = val * 256;
                val = instr.read();

                Double d = new Double((int) (major + val) / 10.0);

                rdg.addTemperature(d);
                count++;
                if (count == md.getNumberOfThermocouples()) {
                    result.add(rdg);
                    rdg = new TemperatureReading();
                    count = 0;
                }
            } else {

            	break;
            }

            // try {
            // Thread.sleep(5);
            // } catch (InterruptedException e) {
            // // TODO Auto-generated catch block
            // e.printStackTrace();
            // }
            val = instr.read();
        }
        System.out.println("done reading");
        long startTime = md.getFirstReadingTime().getTime();
        for (int i = 0; i < result.size(); i++) {
            TemperatureReading curRdg = result.get(i);
            curRdg.setTime(new Date(startTime + (i * md.getLogIntervalInMilliseconds())));
        }

        return result;
    }

    /**
     * Close the port and clean up associated elements.
     */
    public void closeConnection() {

        if (!open) {
            return;
        }

        if (sPort != null) {
            try {

                // close the i/o streams.
                os.close();
                is.close();
            } catch (IOException e) {
                System.err.println(e);
            }
            sPort.closePort();
            System.out.println("port closed");
        }

        open = false;
    }

    public boolean isOpen() {
        return open;
    }

    public static void main(String[] args) {

        DataLoggerManager con = new DataLoggerManager();
        try {
            con.openConnection();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        System.out.println(con.downloadData());

        con.closeConnection();
    }

}