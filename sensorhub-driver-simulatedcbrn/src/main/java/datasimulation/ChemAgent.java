package datasimulation;


import java.util.Random;

/**
 * Created by Ian Patterson on 5/9/2017.
 */

//TODO: move all threat level detection into the CBRN simulated data class
public class ChemAgent
{
	private Random rand = new Random();
	private String agentID;
	private String agentClass;
	private final double minRange = 0.0;
	private final double maxRange = 10.0;
	private final double noThreat = 0.0;


	public void setAgentID(String agentID) {
		this.agentID = agentID;
	}

	public void setAgentClass(String agentClass) {
		this.agentClass = agentClass;
	}

	public void setThreatLevel(double threatLevel)
	{
		this.threatLevel = threatLevel;
		if(this.threatLevel > maxRange)
		{
			this.threatLevel = maxRange;
		}
		else if(this.threatLevel < minRange)
		{
			this.threatLevel = minRange;
		}
	}

	//final double tf_med = 1.0;
	private final double tc_med = 6.67;
	private final double tc_high = 10.0;
	private double threatLevel = 0;


	public ChemAgent(String type)
	{
		agentID = type;
		threatLevel = Math.random() * 10;
		if(type.contains("g") || type.contains("G"))
		{
			agentClass = "G_Agent";
		}
		else if (type.contains("h") || type.contains("H"))
		{
			agentClass = "H_Agent";
		}
		else if (type.contains("v") || type.contains("V"))
		{
			agentClass = "V_Agent";
		}
		else if (type.contains("AC"))
		{
			agentClass = "BloodTIC";
		}
		else
		{
			agentClass = "undefined";
		}

	}

	public String getThreat()
	{
		if(threatLevel == noThreat)
		{
			return "NONE";
		}
		else if(threatLevel > noThreat && threatLevel <= tc_med)
		{
			return "MEDIUM";
		}
		else if(threatLevel > tc_med && threatLevel <= tc_high)
		{
			return "HIGH";
		}
		else
		{
			return null;
		}
	}


	public double getThreatLevel()
	{
		return threatLevel;
	}


	public void update()
	{
		threatLevel += variation(threatLevel, 0.0, 0.1, 0.5);
		if(threatLevel < minRange)
		{
			threatLevel = minRange;
		}
		if (threatLevel > maxRange)
		{
			threatLevel = maxRange;
		}
	}


	public int getBars()
	{
		if(threatLevel == 0) return 0;
		else if (threatLevel > 0.00 && threatLevel <= 1.67) return 1;
		else if (threatLevel > 1.67 && threatLevel <= 3.34) return 2;
		else if (threatLevel > 3.34 && threatLevel <= 5.00) return 3;
		else if (threatLevel > 5.00 && threatLevel <= 6.67) return 4;
		else if (threatLevel > 6.67 && threatLevel <= 8.35) return 5;
		else if (threatLevel > 8.35 && threatLevel <= 10.0) return 6;
		else return -1;
	}


	public String getAgentID()
	{
		return agentID;
	}


	public String getAgentClass()
	{
		return agentClass;
	}


	private double variation(double val, double ref, double dampingCoef, double noiseSigma)
	{
		return -dampingCoef*(val - ref) + noiseSigma*rand.nextGaussian();
	}
}
