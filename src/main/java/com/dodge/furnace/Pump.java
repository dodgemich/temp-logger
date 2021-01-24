package com.dodge.furnace;

import java.io.IOException;
import java.util.List;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.cloudwatch.model.DescribeAlarmsRequest;
import com.amazonaws.services.cloudwatch.model.MetricAlarm;

public class Pump {
	public void managePumpState() {
		AmazonCloudWatch cw = AmazonCloudWatchClientBuilder.standard().withRegion(Regions.US_EAST_1).build();
		List<MetricAlarm> alarms = cw
				.describeAlarms(new DescribeAlarmsRequest().withAlarmNames("CallForHeat", "TankReady"))
				.getMetricAlarms();

		MetricAlarm override = cw.describeAlarms(new DescribeAlarmsRequest().withAlarmNames("HighTankOverride"))
				.getMetricAlarms().get(0);

		if (!"OK".equals(override.getStateValue())) {
			System.out.println("tank hot, overriding");
			pumpOn();
			return;
		}

		boolean pumpOn = true;
		for (MetricAlarm alarm : alarms) {
			System.out.println(alarm.getAlarmName() + " - " + alarm.getStateValue());
			pumpOn = pumpOn && "OK".equals(alarm.getStateValue());
		}
		if (pumpOn) {
			System.out.println("Pump to ON");
			pumpOn();
		} else {
			System.out.println("Pump to OFF");
			pumpOff();
		}

	}

	private void pumpOff() {
		try {
			Process p = Runtime.getRuntime().exec("/home/pi/temp-logger/off.sh");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void pumpOn() {
		try {
			Process p = Runtime.getRuntime().exec("/home/pi/temp-logger/on.sh");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
