package com.dodge.thermocouple.persistence;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.cloudwatch.model.DescribeAlarmsRequest;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import com.amazonaws.services.cloudwatch.model.PutMetricDataResult;
import com.amazonaws.services.cloudwatch.model.StandardUnit;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.dodge.furnace.FurnaceReading;
import com.dodge.thermocouple.reader.pojo.TemperatureReading;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CloudwatchPersister {

	public void persist(TemperatureReading rdg) {

		AmazonCloudWatch cw = AmazonCloudWatchClientBuilder.standard().withRegion(Regions.US_EAST_1).build();

		List<Double> temps = rdg.getTemps();
		Date time = new Date();

		MetricDatum flue = new MetricDatum()
				.withMetricName("Temperature")
				.withDimensions(new Dimension().withName("Resource").withValue("Flue"))
				.withUnit(StandardUnit.None)
				.withValue(temps.get(0))
				.withTimestamp(time);
		
		
		MetricDatum tank = new MetricDatum()
				.withMetricName("Temperature")
				.withDimensions(new Dimension().withName("Resource").withValue("Tank"))
				.withUnit(StandardUnit.None)
				.withValue(temps.get(1))
				.withTimestamp(time);

		MetricDatum manifold = new MetricDatum()
				.withMetricName("Temperature")
				.withDimensions(new Dimension().withName("Resource").withValue("Manifold"))
				.withUnit(StandardUnit.None)
				.withValue(temps.get(2))
				.withTimestamp(time);

		try{
			PutMetricDataResult res = cw
					.putMetricData(new PutMetricDataRequest()
							.withNamespace("Boiler")
							.withMetricData(flue, tank, manifold)
							);
		} catch(Exception e) {
			System.out.println("Boiler " + e.getMessage());
		}

		double heating = temps.get(4) > 100 ? 1.0 : 0.0;
		MetricDatum state = new MetricDatum().withMetricName("Nest")
				.withDimensions(new Dimension().withName("Resource").withValue("State")).withUnit(StandardUnit.None)
				.withValue(heating).withTimestamp(time);
		try {
			cw.putMetricData(new PutMetricDataRequest().withNamespace("Furnace").withMetricData(state));
		} catch (Exception e) {
			System.out.println("Furnace " + e.getMessage());
		}
		//TODO - still needed?  - was -1.5
		long houseTemp = Math.round(temps.get(3));
		MetricDatum ambient = new MetricDatum()
				.withMetricName("Nest")
				.withDimensions(new Dimension().
						withName("Resource")
						.withValue("Ambient"))
				.withUnit(StandardUnit.None)
				.withValue((double)(houseTemp))
				.withTimestamp(time)
				;
		
		MetricDatum outdoor = new MetricDatum().withMetricName("Thermometer")
				.withDimensions(new Dimension().withName("Resource").withValue("outdoor")).withUnit(StandardUnit.None)
				.withValue(outsideTemp()).withTimestamp(time);
		try {
			cw.putMetricData(new PutMetricDataRequest().withNamespace("Environment").withMetricData(ambient, outdoor));
		} catch (Exception e) {
			System.out.println("Env " + e.getMessage());
		}
		cw.shutdown();

	}

	public void persistIot() {

		AmazonCloudWatch cw = AmazonCloudWatchClientBuilder.standard().withRegion(Regions.US_EAST_1).build();

		Date time = new Date();
		MetricDatum load = new MetricDatum().withMetricName("Load").withUnit(StandardUnit.None)
				.withValue(checkIot("boiler-reload")).withTimestamp(time);
		MetricDatum stock = new MetricDatum().withMetricName("Stock").withUnit(StandardUnit.None)
				.withValue(checkIot("boiler-restock")).withTimestamp(time);

		cw.putMetricData(new PutMetricDataRequest().withNamespace("Boiler").withMetricData(load, stock));
		cw.shutdown();

	}

	public void persist(FurnaceReading rdg) {

		AmazonCloudWatch cw = AmazonCloudWatchClientBuilder.standard().withRegion(Regions.US_EAST_1).build();

		Date time = new Date();

		MetricDatum target = new MetricDatum().withMetricName("Nest")
				.withDimensions(new Dimension().withName("Resource").withValue("Target")).withUnit(StandardUnit.None)
				.withValue(rdg.getTarget().doubleValue()).withTimestamp(time);

		double heating = "heating".equals(rdg.getState()) ? 1.0 : 0.0;
		MetricDatum state = new MetricDatum().withMetricName("Nest")
				.withDimensions(new Dimension().withName("Resource").withValue("State")).withUnit(StandardUnit.None)
				.withValue(heating).withTimestamp(time);

		cw.putMetricData(new PutMetricDataRequest().withNamespace("Furnace").withMetricData(target, state));

		MetricDatum humidity = new MetricDatum().withMetricName("Nest")
				.withDimensions(new Dimension().withName("Resource").withValue("Humidity")).withUnit(StandardUnit.None)
				.withValue(rdg.getHumidity().doubleValue()).withTimestamp(time);

		MetricDatum ambient = new MetricDatum().withMetricName("Nest")
				.withDimensions(new Dimension().withName("Resource").withValue("Ambient")).withUnit(StandardUnit.None)
				.withValue(rdg.getAmbient().doubleValue()).withTimestamp(time);
		MetricDatum outdoor = new MetricDatum().withMetricName("Thermometer")
				.withDimensions(new Dimension().withName("Resource").withValue("outdoor")).withUnit(StandardUnit.None)
				.withValue(rdg.getOutside().doubleValue()).withTimestamp(time);

		cw.putMetricData(
				new PutMetricDataRequest().withNamespace("Environment").withMetricData(humidity, ambient, outdoor));
		cw.shutdown();
	}

	public double checkIot(String item) {
		AmazonSQS sqs = AmazonSQSClientBuilder.standard().withRegion(Regions.US_EAST_1).build();
		String url = "https://sqs.us-east-1.amazonaws.com/658544419615/" + item;
		ReceiveMessageResult res = sqs
				.receiveMessage(new ReceiveMessageRequest().withQueueUrl(url).withMaxNumberOfMessages(1));
		if (res.getMessages().size() > 0) {
			sqs.deleteMessage(new DeleteMessageRequest().withQueueUrl(url)
					.withReceiptHandle(res.getMessages().get(0).getReceiptHandle()));
		}
		return res.getMessages().size() > 0 ? 0.5 : 0.0;
	}




	public Double outsideTemp()  {
		OkHttpClient client = new OkHttpClient.Builder().followRedirects(true).followSslRedirects(true).build();
		String token = System.getProperty("TEMPEST_TOKEN");
		Request request = new Request.Builder().url("https://swd.weatherflow.com/swd/rest/observations/?device_id=235627&token="+token).get()
				// .addHeader("accept", "application/geo+json")
				.build();
		Response response;

		try {
			response = client.newCall(request).execute();
			String body = response.body().string();
			
			JSONParser parser = new JSONParser();
			JSONObject res = (JSONObject) parser.parse(body);
			JSONArray obs = (JSONArray) res.get("obs");
			JSONArray obsInner = (JSONArray) obs.get(0);

			Object rawTemp = obsInner.get(7);
			Double temp = null;
			if (rawTemp instanceof Double) {
				temp = (Double) rawTemp;
			} else {
				Long l = (Long) rawTemp;
				temp = l.doubleValue();
			}
			
			return temp * 9 / 5 + 32;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0.0;

	}




	public Double outsideTempWindy()  {
		OkHttpClient client = new OkHttpClient.Builder().followRedirects(true).followSslRedirects(true).build();
		Request request = new Request.Builder().url("https://node.windy.com/pois/stations/44.9262/-89.6266").get()
				// .addHeader("accept", "application/geo+json")
				.build();
		Response response;

		try {
			response = client.newCall(request).execute();
			String body = response.body().string();
			
			JSONParser parser = new JSONParser();
			JSONArray res = (JSONArray) parser.parse(body);
			JSONObject auw = (JSONObject) res.get(0);
//		    JSONObject props = (JSONObject) res.get("properties");
			
			Object rawTemp = auw.get("temp");
			Double temp = null;
			if (rawTemp instanceof Double) {
				temp = (Double) rawTemp;
			} else {
				Long l = (Long) rawTemp;
				temp = l.doubleValue();
			}
			
			return temp * 9 / 5 + 32;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0.0;

	}

	public static void main(String... strings) {
		CloudwatchPersister p = new CloudwatchPersister();
		 TemperatureReading r = new TemperatureReading();
		 r.addTemperature(420.0);
		 r.addTemperature(150.0);
		 r.addTemperature(146.0);
		 r.addTemperature(71.4);
		 r.addTemperature(75.5);
		 r.setTime(new Date());
		 p.persist(r);
	}

}
