package com.dodge.furnace;

import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import okhttp3.Authenticator;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

public class FurnaceReader {

    
    public FurnaceReading read() throws IOException, ParseException {
	 final String  auth = "Bearer c.y6AjoulS2lSmx4HaoQtJ6EcC2ZPvBt3x7v1LByikgp1fkLVctJTv0DvK5KtoQtbdIPshRV8lxKrQ9Ec2eW0dsF7WhEM8bX53UhphVt2RQnzvOVgZdZbBmEwrfbqeZ5hVmHwyLHZ5ObO4YMSX"; // Update with your token
         FurnaceReading result =null;
         OkHttpClient client = new OkHttpClient.Builder()
     	    .authenticator(new Authenticator() {
     		@Override public Request authenticate(Route route, Response response) throws IOException {
     		    return response.request().newBuilder()
     			    .header("Authorization", auth)
     			    .build();
     		}
     	    })
     	    .followRedirects(true)
     	    .followSslRedirects(true)
     	    .build();
         Request request = new Request.Builder()
     	    .url("https://developer-api.nest.com/devices/thermostats/5eIvFvulWkh6TS60WF1h-N5ENbzhcr_J/")
     	    .get()
     	    .addHeader("content-type", "application/json; charset=UTF-8")
     	    .addHeader("authorization", auth)
     	    .build();
         Response response;
	
	    response = client.newCall(request).execute();
	    String body = response.body().string();
	    
	    JSONParser parser = new JSONParser();
	    JSONObject res = (JSONObject) parser.parse(body);
	    
	    String state = (String) res.get("hvac_state");
	    Long humidity = (Long) res.get("humidity");
	    Long target = (Long) res.get("target_temperature_f");
	    Long ambient = (Long) res.get("ambient_temperature_f");
	    result = new FurnaceReading();
	    
	    result.setState(state);
	    result.setHumidity(humidity);
	    result.setTarget(target);
	    result.setAmbient(ambient);
	    
	    result.setOutside(Math.round(outsideTemp()));
	    
	    //
	
	return result;
    }
    
    public Double outsideTemp() throws IOException, ParseException {
	OkHttpClient client = new OkHttpClient.Builder()
	     	    .followRedirects(true)
	     	    .followSslRedirects(true)
	     	    .build();
	         Request request = new Request.Builder()
	     	    .url("https://node.windy.com/pois/stations/44.9262/-89.6266")
	     	    .get()
	     	   // .addHeader("accept", "application/geo+json")
	     	    .build();
	         Response response;
		
		    response = client.newCall(request).execute();
		    String body = response.body().string();
		    
		    JSONParser parser = new JSONParser();
		    JSONArray res = (JSONArray) parser.parse(body);
		    JSONObject auw = (JSONObject) res.get(0);
		    
//		    JSONObject props = (JSONObject) res.get("properties");
		    
		    Object rawTemp = auw.get("temp");
		    Double temp = null;
		    if(rawTemp instanceof Double) {
			temp = (Double) rawTemp;
		    } else {
			Long l = (Long) rawTemp;
			temp = l.doubleValue();
		    }
		    
		    return temp*9/5 + 32;
    }
    
    public static void main(String...strings ) throws IOException, ParseException {
	FurnaceReader r = new FurnaceReader();
	System.out.println(r.read());
	
    }
    
}
