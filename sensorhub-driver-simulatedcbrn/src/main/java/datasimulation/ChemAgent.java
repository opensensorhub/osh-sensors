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


	public double getThreatLevel()
	{
		return threatLevel;
	}


	public String getAgentID()
	{
		return agentID;
	}


	public String getAgentClass()
	{
		return agentClass;
	}
}
