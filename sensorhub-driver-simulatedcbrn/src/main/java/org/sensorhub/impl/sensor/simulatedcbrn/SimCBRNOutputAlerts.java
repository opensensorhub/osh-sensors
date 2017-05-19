/***************************** BEGIN LICENSE BLOCK ***************************

 The contents of this file are subject to the Mozilla Public License, v. 2.0.
 If a copy of the MPL was not distributed with this file, You can obtain one
 at http://mozilla.org/MPL/2.0/.

 Software distributed under the License is distributed on an "AS IS" basis,
 WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 for the specific language governing rights and limitations under the License.

 The Initial Developer is Botts Innovative Research Inc. Portions created by the Initial
 Developer are Copyright (C) 2014 the Initial Developer. All Rights Reserved.

 ******************************* END LICENSE BLOCK ***************************/
package org.sensorhub.impl.sensor.simulatedcbrn;

import net.opengis.swe.v20.*;
import org.sensorhub.impl.sensor.AbstractSensorOutput;
import org.sensorhub.impl.sensor.simulatedcbrn.SimCBRNOutputAlerts;
import org.sensorhub.impl.sensor.simulatedcbrn.SimCBRNSensor;
import org.sensorhub.api.sensor.SensorDataEvent;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.vast.swe.SWEHelper;

public class SimCBRNOutputAlerts extends AbstractSensorOutput<SimCBRNSensor>
{
    DataComponent cbrnAlertData;
    DataEncoding cbrnEncoding;
    Timer timer;
    Random rand = new Random();

    // Reference values used as a basis for building randomized vals
    double tempRef = 20.0;

    // "Sensor" variables (what gets output as the sensor data
    double temp = tempRef;
    String ID = "testID";
    String eventStatus = "NONE";
    String agentClassStatus = "G_Agent";
    String agentIDStatus = "GA";
    int levelStatus = 0;
    String units = "BARS";
    String hazardLevelStatus = "NONE";

    public SimCBRNOutputAlerts(SimCBRNSensor parentSensor)
    {
        super(parentSensor);
    }


    @Override
    public String getName()
    {
        return "ALERTS";
    }


    protected void init()
    {
        SWEHelper fac = new SWEHelper();

        // Build SWE Common record structure
        cbrnAlertData = fac.newDataRecord(9);
        cbrnAlertData.setName(getName());
        cbrnAlertData.setDefinition("http://sensorml.com/ont/swe/property/ToxicAgent");
        cbrnAlertData.setDescription("CBRN measurements");


        // Add fields
        cbrnAlertData.addComponent("time", fac.newTimeStampIsoUTC());
        cbrnAlertData.addComponent("id", fac.newCategory("http://sensorml.com/ont/swe/property/SensorID",null,null,"http://sensorml.com/ont/swe/property/sensorRegistry"));

        // To set up the event alert, must add set of allowed tokens
        // TODO: Alert_Event does NOT generate an error
        Category alert_Event = fac.newCategory("http://sensorml.com/ont/swe/property/AlertEvent", null, null, null);
        AllowedTokens allowedEvents = fac.newAllowedTokens();
        allowedEvents.addValue("ALERT");
        allowedEvents.addValue("DE-ALERT");
        allowedEvents.addValue("WARN");
        allowedEvents.addValue("DE-WARN");
        allowedEvents.addValue("NONE");
        alert_Event.setConstraint(allowedEvents);
        cbrnAlertData.addComponent("event", alert_Event);
        // TODO: The Alert outputs below give errors when looking for RAW format data from the SOS Service
        // Agent Classes
        Category alert_AgentClass = fac.newCategory("http://sensorml.com/ont/swe/property/ChemicalAgentClass", null, null,null );
        AllowedTokens allowedAgentClasses = fac.newAllowedTokens();
        allowedAgentClasses.addValue("G_Agent");
        allowedAgentClasses.addValue("H_Agent");
        allowedAgentClasses.addValue("BloodTIC");
        alert_AgentClass.setConstraint(allowedAgentClasses);
        cbrnAlertData.addComponent("agent_class", alert_AgentClass);

        // Agent IDs (Specific Identification codes for Toxic Agents)
        Category alert_AgentID = fac.newCategory("http://sensorml.com/ont/swe/property/ChemicalAgentID", null, null, null);
        AllowedTokens allowedAgentIDs = fac.newAllowedTokens();
        allowedAgentIDs.addValue("GA");
        allowedAgentIDs.addValue("GB");
        allowedAgentIDs.addValue("GD");
        allowedAgentIDs.addValue("VX");
        allowedAgentIDs.addValue("HN");
        allowedAgentIDs.addValue("HD");
        allowedAgentIDs.addValue("L");
        alert_AgentID.setConstraint(allowedAgentIDs);
        cbrnAlertData.addComponent("agent_ID", alert_AgentID);

        // Alert Levels
        Quantity alert_Level = fac.newQuantity("http://sensorml.com/ont/swe/property/Level", null, null,"http://www.opengis.net/def/uom/0/instrument_BAR");
        AllowedValues levelValue = fac.newAllowedValues();
        levelValue.addInterval(new double[] {0,6});
        alert_Level.setConstraint(levelValue);
        cbrnAlertData.addComponent("alert_level", alert_Level);

        // Alert Units (of measurement)
        Category alert_Units = fac.newCategory("http://sensorml.com/ont/swe/property/UnitOfMeasure", null, null, null);
        AllowedTokens unitToken = fac.newAllowedTokens();
        unitToken.addValue("BARS");
        alert_Units.setConstraint(unitToken);
        cbrnAlertData.addComponent("alert_units", alert_Units);

        // Hazard Level (severity)
        Category alerts_HazardLevel = fac.newCategory("http://sensorml.com/ont/swe/property/HazardLevel",null, null, null);
        AllowedTokens  hazardLevels = fac.newAllowedTokens();
        hazardLevels.addValue("None");
        hazardLevels.addValue("Medium");
        hazardLevels.addValue("High");
        alerts_HazardLevel.setConstraint(hazardLevels);
        cbrnAlertData.addComponent("hazard_level", alerts_HazardLevel);

        // Temperature
        cbrnAlertData.addComponent("temp", fac.newQuantity("http://sensorml.com/ont/swe/property/Temperature", null, null, "Cel"));

        cbrnEncoding = fac.newTextEncoding(",", "\n");
    }

    // will need to do some of the simulation here save for later
    private void sendMeasurement()
    {
        double time = System.currentTimeMillis()/1000;
        CBRNSimulatedData.getInstance().update();

        // Temperature sim (copied from FakeWeatherOutput)
        temp += variation(temp, tempRef, 0.001, 0.1);
        eventStatus = CBRNSimulatedData.getInstance().getWarnStatus();
        agentClassStatus = CBRNSimulatedData.getInstance().getMaxThreatAgent().getAgentClass();
        agentIDStatus = CBRNSimulatedData.getInstance().getMaxThreatAgent().getAgentID();
        levelStatus = CBRNSimulatedData.getInstance().getMaxThreatAgent().getBars();
        hazardLevelStatus = CBRNSimulatedData.getInstance().getMaxThreatAgent().getThreat();

        // Build DataBlock
        DataBlock dataBlock = cbrnAlertData.createDataBlock();
        dataBlock.setDoubleValue(0, time);
        dataBlock.setStringValue(1, ID);
        dataBlock.setStringValue(2, eventStatus);
        dataBlock.setStringValue(3, agentClassStatus);
        dataBlock.setStringValue(4, agentIDStatus);
        dataBlock.setIntValue(5, levelStatus);
        dataBlock.setStringValue(6, units);
        dataBlock.setStringValue(7, hazardLevelStatus);
        dataBlock.setDoubleValue(8, temp);

        //this method call is required to push data
        latestRecord = dataBlock;
        latestRecordTime = System.currentTimeMillis();
        eventHandler.publishEvent(new SensorDataEvent(latestRecordTime, SimCBRNOutputAlerts.this, dataBlock));
    }


    private double variation(double val, double ref, double dampingCoef, double noiseSigma)
    {
        return -dampingCoef*(val - ref) + noiseSigma*rand.nextGaussian();
    }


    protected void start()
    {
        if (timer != null)
            return;
        timer = new Timer();

        // start main measurement generation thread
        TimerTask task = new TimerTask() {
            public void run()
            {
                sendMeasurement();
            }
        };

        timer.scheduleAtFixedRate(task, 0, (long)(getAverageSamplingPeriod()*1000));
    }


    @Override
    protected void stop()
    {
        if (timer != null)
        {
            timer.cancel();
            timer = null;
        }
    }


    @Override
    public double getAverageSamplingPeriod()
    {
        // sample every 1 second
        return 1.0;
    }


    @Override
    public DataComponent getRecordDescription()
    {
        return cbrnAlertData;
    }


    @Override
    public DataEncoding getRecommendedEncoding()
    {
        return cbrnEncoding;
    }
}

