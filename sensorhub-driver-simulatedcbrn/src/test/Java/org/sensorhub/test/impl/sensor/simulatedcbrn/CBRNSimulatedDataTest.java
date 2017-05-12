package org.sensorhub.test.impl.sensor.simulatedcbrn;

import org.junit.Test;
import org.sensorhub.impl.sensor.simulatedcbrn.CBRNSimulatedData;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Created by Ian Patterson on 5/9/2017.
 */
public class CBRNSimulatedDataTest {
	@Test
	public void getInstance() throws Exception
	{
		assertNotNull(CBRNSimulatedData.getInstance());
	}

	@Test
	public void simTemp() throws Exception {
	}

	@Test
	public void getTemp() throws Exception {
	}

	@Test
	public void getMaxThreatAgent() throws Exception
	{
		assertNotNull(CBRNSimulatedData.getInstance().getMaxThreatAgent());
	}

	@Test
	public void setMaxThreatAgent() throws Exception {
	}

	@Test
	public void update() throws Exception
	{
		double benchmark = CBRNSimulatedData.getInstance().getMaxThreatAgent().getThreatLevel();
		TimeUnit.SECONDS.sleep(1);
		CBRNSimulatedData.getInstance().update();
		assertNotEquals(benchmark, CBRNSimulatedData.getInstance().getMaxThreatAgent().getThreatLevel());
	}

	@Test
	public void getWarnStatus() throws Exception
	{
		CBRNSimulatedData.getInstance().getMaxThreatAgent().setThreatLevel(0.0);
		CBRNSimulatedData.getInstance().autoSetWarnStatus();
		System.out.println(CBRNSimulatedData.getInstance().getWarnStatus());
		for(int i = 0; i< 100; i++)
		{
			CBRNSimulatedData.getInstance().getMaxThreatAgent().setThreatLevel(
					CBRNSimulatedData.getInstance().getMaxThreatAgent().getThreatLevel() + 0.1);
			CBRNSimulatedData.getInstance().autoSetWarnStatus();
			System.out.println(CBRNSimulatedData.getInstance().getWarnStatus());
		}

		for(int i = 0; i< 100; i++)
		{
			CBRNSimulatedData.getInstance().getMaxThreatAgent().setThreatLevel(
					CBRNSimulatedData.getInstance().getMaxThreatAgent().getThreatLevel() - 0.1);
			CBRNSimulatedData.getInstance().autoSetWarnStatus();
			System.out.println(CBRNSimulatedData.getInstance().getWarnStatus());
		}

	}

}