package com.dodge.thermocouple.reader.pojo;

import java.io.BufferedReader;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MetaData {
	
	private static SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yy HH:mm:ss");

	
	private int nbrTCouples;
	private int nbrRecords;
	private Date lastReading;
	private Date readTime;
	public Date getReadTime() {
		return readTime;
	}



	public void setReadTime(Date readTime) {
		this.readTime = readTime;
	}

	private long logInterval;
	
	public MetaData(String raw){
		BufferedReader rdr = new BufferedReader(new StringReader(raw));
		long sysTime = System.currentTimeMillis();
		try{
			while (rdr.ready()){
				String s = rdr.readLine();
				if(s==null){
					break;
				}
				if(s.startsWith("LL")){
					lastReading = formatter.parse(s.substring(3, s.length()));
				} else if(s.startsWith("DT")){
					readTime = formatter.parse(s.substring(3, s.length()));
				} else if(s.startsWith("NA")){
					nbrTCouples = Integer.parseInt(s.substring(3, s.length()));
				} else if(s.startsWith("NR")){
					nbrRecords = Integer.parseInt(s.substring(3, s.length()));
				}else if(s.startsWith("LI")){
					String li = s.substring(3, s.length());
					int hours = Integer.parseInt(li.substring(0,2));
					int mins = Integer.parseInt(li.substring(3,5));
					int secs = Integer.parseInt(li.substring(6,8));
					logInterval = (1000*secs)+(60000*mins)+(3600000*hours);
				}
				
			}
			long clockOffset = sysTime - readTime.getTime();
			adjustForClockDiscrepancy(clockOffset);
			
			//Date firstReading = new Date(lastReading.getTime()-(nbrRecords*interval));
			//System.out.println(firstReading);

		} catch (Exception e){
			System.out.println(e);	
		}
		
	}
	
	
	private void adjustForClockDiscrepancy(long offset){
		lastReading.setTime(lastReading.getTime()+offset);
		readTime.setTime(readTime.getTime()+offset);
	}
	
	
	public int getNumberOfThermocouples(){
		return nbrTCouples;
	}
	
	public Date getFirstReadingTime(){
		int n = nbrRecords-1;
		if(n<0){
			n=0;
		}
		return new Date(lastReading.getTime()-(n*getLogIntervalInMilliseconds()));
	}
	
	public Date getLastReadingTime(){
		return lastReading;
	}
	
	public long getLogIntervalInMilliseconds(){
		return logInterval;
	}
	
	public int getRecordCount(){
		return nbrRecords;
	}
	
	public String toString(){
		StringBuffer buf = new StringBuffer();
		buf.append("=================================================\n");
		buf.append("================= Reading Info  =================\n");
		buf.append("=================================================\n");
		buf.append("First Reading:      " + getFirstReadingTime() +"\n");
		buf.append("Last Reading:       " + getLastReadingTime() +"\n");
		buf.append("Read Time:          " + getReadTime() +"\n");
		buf.append("TCouple Count:      " + getNumberOfThermocouples() +"\n");
		buf.append("Record Count:       " + getRecordCount() +"\n");
		buf.append("Log Interval (ms):  " + getLogIntervalInMilliseconds() +"\n");
		buf.append("=================================================\n");
		return buf.toString();
	}
	
//	VE 02.29
//	PT TV-40 
//	RF 60
//	MO N
//	DT 01/23/11 15:04:21
//	LI 00:01:00
//	DW 05
//	TA +00.0
//	LL 01/23/11 15:03:39
//	NA 02
//	NR 00021
//	CH 01 J  F   +1.0000 +00000 1   NONE    NONE 
//	CH 02 J  F   +1.0000 +00000 1   NONE    NONE 
	
	
//	VE 02.29
//	PT TV-40 
//	RF 60
//	MO N
//	DT 01/23/11 15:15:03
//	LI 00:01:00
//	DW 05
//	TA +00.0
//	LL 01/23/11 15:14:16
//	NA 01
//	NR 00001
//	CH 01 J  F   +1.0000 +00000 1   NONE    NONE 

}
