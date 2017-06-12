package datasimulation;

import java.util.Calendar;
import java.util.Random;
import java.util.Timer;

/**
 * Created by Ian Patterson on 5/4/2017.
 */

// Todo: Apply inverse-square law to toxin severity (Intensity at point = (Severity at source)/(4*pi*r^2)) r = dist from chem source
public class CBRNSimulatedData {

	private static CBRNSimulatedData ourInstance;
	Timer timer;
	long lastUpdateTime;
	Random rand = new Random();

	// Reference Variables
	double tempRef = 20.0;

	// "Actual" Sim Variables to be fed as sensor data
	double temp = tempRef;
	ChemAgent g_Agent;
	ChemAgent h_Agent;
	ChemAgent v_Agent;
	ChemAgent blood_Agent;
	ChemAgent maxThreatAgent = null;
	double preWarnStatus = 0;
	String warnStatus = "NONE";

	public static CBRNSimulatedData getInstance()
	{
		if(ourInstance == null)
		{
			ourInstance = new CBRNSimulatedData();
		}
		return ourInstance;
	}

	private CBRNSimulatedData()
	{
		g_Agent = new ChemAgent("GA");
		h_Agent = new ChemAgent("HN");
		v_Agent = new ChemAgent("VX");
		blood_Agent = new ChemAgent("AC");
		lastUpdateTime = Calendar.getInstance().getTimeInMillis();
		maxThreatAgent = g_Agent;
		update();
		autoSetWarnStatus();
	}


	//need to create several forms of data randomization to supply the sensor with sim data
	public double simTemp()
	{
		// Temperature sim (copied from FakeWeatherOutput)
		temp += variation(temp, tempRef, 0.001, 0.1);
		return temp;
	}


	public double getTemp() {
		return temp;
	}


	public ChemAgent getMaxThreatAgent()

	{
		return maxThreatAgent;
	}

	/**
	 * <p>Reads all chemical agents and sets sensor to display info on only the one
	 * with the highest threat level</p>
	 */
	private void setMaxThreatAgent()
	{
		if (g_Agent.getThreatLevel() > h_Agent.getThreatLevel() && g_Agent.getThreatLevel() > v_Agent.getThreatLevel()
				&& g_Agent.getThreatLevel() > blood_Agent.getThreatLevel())
		{
			maxThreatAgent = g_Agent;
		}
		else if (h_Agent.getThreatLevel() > g_Agent.getThreatLevel() && h_Agent.getThreatLevel() > v_Agent.getThreatLevel()
				&& h_Agent.getThreatLevel() > blood_Agent.getThreatLevel())
		{
			maxThreatAgent = h_Agent;
		}
		else if(v_Agent.getThreatLevel() > g_Agent.getThreatLevel() && v_Agent.getThreatLevel() > h_Agent.getThreatLevel()
				&& v_Agent.getThreatLevel() > blood_Agent.getThreatLevel())
		{
			maxThreatAgent = v_Agent;
		}
		else if(blood_Agent.getThreatLevel() > g_Agent.getThreatLevel() && blood_Agent.getThreatLevel() > h_Agent.getThreatLevel()
				&& blood_Agent.getThreatLevel() > v_Agent.getThreatLevel())
		{
			maxThreatAgent = blood_Agent;
		}
		else
		{
			maxThreatAgent = g_Agent;
		}
	}

	/**
	 * <p>To run through the variables and make all adjustments
	 * recommend calling this every time the sensor makes a call
	 * to its outputs</p>
	 */
	public void update()
	{
		if(Calendar.getInstance().getTimeInMillis() - lastUpdateTime >= 1000)
		{
			simTemp();
			g_Agent.update();
			autoSetWarnStatus();
			lastUpdateTime = Calendar.getInstance().getTimeInMillis();
		}
	}


	public void autoSetWarnStatus()
	{
		if(maxThreatAgent.getThreatLevel() - preWarnStatus > 0.0 && maxThreatAgent.getThreatLevel() > 0.0
				&& maxThreatAgent.getThreatLevel() < 6.67)
		{
			warnStatus = "WARN";
		}

		else if(maxThreatAgent.getThreatLevel() - preWarnStatus > 0.0 && maxThreatAgent.getThreatLevel() >= 6.67)
		{
			warnStatus = "ALERT";
		}
		else if(maxThreatAgent.getThreatLevel() - preWarnStatus < 0.0 && maxThreatAgent.getThreatLevel() < 6.67
				&& warnStatus.equals("ALERT"))
		{
			warnStatus = "DEALERT";
		}
		else if(maxThreatAgent.getThreatLevel() - preWarnStatus < 0.0 && maxThreatAgent.getThreatLevel() == 0.0
				&& warnStatus.equals("WARN"))
		{
			warnStatus = "DEWARN";
		}
		else if(warnStatus.equals("DEALERT") && maxThreatAgent.getThreatLevel() > 0.0
				&& maxThreatAgent.getThreatLevel() < 6.67)
		{
			warnStatus = "WARN";
		}
		else if(warnStatus.equals("DEWARN") && maxThreatAgent.getThreatLevel() == 0.0)
		{
			warnStatus = "NONE";
		}
		else
		{
			warnStatus = warnStatus;
		}

		preWarnStatus = getMaxThreatAgent().getThreatLevel();
	}


	public void setWarnStatus(String newWarnStatus)
	{
		warnStatus = newWarnStatus;
	}


	public String getWarnStatus()
	{
		return warnStatus;
	}


	private double variation(double val, double ref, double dampingCoef, double noiseSigma)
	{
		return -dampingCoef*(val - ref) + noiseSigma*rand.nextGaussian();
	}

	private double getObservedIntensity(double lat, double lon, PointSource source)
	{

		return lat;
	}

}
