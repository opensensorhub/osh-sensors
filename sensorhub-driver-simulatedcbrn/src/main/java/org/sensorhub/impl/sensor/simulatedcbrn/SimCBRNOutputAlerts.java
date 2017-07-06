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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import datasimulation.ChemAgent;
import net.opengis.swe.v20.*;
import net.opengis.swe.v20.Vector;
import org.sensorhub.api.sensor.SensorDataEvent;
import org.sensorhub.impl.sensor.AbstractSensorOutput;
import org.vast.swe.SWEConstants;
import org.vast.swe.SWEHelper;
import org.vast.swe.helper.GeoPosHelper;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

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
    int numericalLevel = 0;
    String units = "BARS";
    String stringLevel = "NONE";

    // For GPS simulation
    List<double[]> trajPoints;
    static double currentTrackPos;
    double lat, lon, alt;

    // For Intensity calculations
    double preWarnStatus = 0;
    String warnStatus = "NONE";
    double threatLevel;
    double min_threat = 0.0;
    double max_threat = 600.0;
    ChemAgent detectedAgent;

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
        GeoPosHelper fac2 = new GeoPosHelper();
        trajPoints = new ArrayList<double[]>();

        SWEHelper fac = new SWEHelper();

        // Build SWE Common record structure
        cbrnAlertData = fac.newDataRecord(9);
        cbrnAlertData.setName(getName());
        cbrnAlertData.setDefinition("http://sensorml.com/ont/swe/property/ToxicAgent");
        cbrnAlertData.setDescription("CBRN measurements");


        // Add fields
        cbrnAlertData.addComponent("time", fac.newTimeStampIsoUTC());
        //cbrnAlertData.addComponent("id", fac.newCategory("http://sensorml.com/ont/swe/property/SensorIdentifier",null,null,"http://sensorml.com/ont/swe/property/sensorRegistry"));
        cbrnAlertData.addComponent("id", fac.newCategory("http://sensorml.com/ont/swe/property/SensorID",null,null,null));

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
        hazardLevels.addValue("Low");
        hazardLevels.addValue("Medium");
        hazardLevels.addValue("High");
        alerts_HazardLevel.setConstraint(hazardLevels);
        cbrnAlertData.addComponent("hazard_level", alerts_HazardLevel);

        // Continuous Reading
        cbrnAlertData.addComponent("continuous", fac.newQuantity("http://sensorml.com/ont/swe/property/Continuous", null, null, null));


        // Temperature
        cbrnAlertData.addComponent("temp", fac.newQuantity("http://sensorml.com/ont/swe/property/Temperature", null, null, "Cel"));


        //Sim location
        Vector locVector = fac2.newLocationVectorLLA(SWEConstants.DEF_SENSOR_LOC);
        locVector.setLabel("Location");
        locVector.setDescription("Location measured by GPS device");
        cbrnAlertData.addComponent("location", locVector);

        cbrnEncoding = fac.newTextEncoding(",", "\n");
    }

    // will need to do some of the simulation here save for later
    private void sendMeasurement()
    {
        double time = System.currentTimeMillis()/1000;
        simulate();

        // Temperature sim (copied from FakeWeatherOutput)
        temp += variation(temp, tempRef, 0.001, 0.1);
        eventStatus = warnStatus;
        agentClassStatus = detectedAgent.getAgentClass();
        agentIDStatus = detectedAgent.getAgentID();
        numericalLevel = findThreatLevel();
        stringLevel = findThreatString();

        // Build DataBlock
        DataBlock dataBlock = cbrnAlertData.createDataBlock();
        dataBlock.setDoubleValue(0, time);
        dataBlock.setStringValue(1, ID);
        dataBlock.setStringValue(2, eventStatus);
        dataBlock.setStringValue(3, agentClassStatus);
        dataBlock.setStringValue(4, agentIDStatus);
        dataBlock.setIntValue(5, numericalLevel);
        dataBlock.setStringValue(6, units);
        dataBlock.setStringValue(7, stringLevel);
        dataBlock.setDoubleValue(8, threatLevel);
        dataBlock.setDoubleValue(9, temp);
        dataBlock.setDoubleValue(10, lat);
        dataBlock.setDoubleValue(11, lon);
        dataBlock.setDoubleValue(12, alt);

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

    @SuppressWarnings("Duplicates")
    private void simulate()
    {
        SimCBRNConfig config = getParentModule().getConfiguration();
        // Update the sensor's location
        if (trajPoints.isEmpty() || currentTrackPos >= trajPoints.size()-2)
        {
            if (!generateRandomTrajectory())
                return;

            // skip if generated traj is too small
            if (trajPoints.size() < 2)
            {
                trajPoints.clear();
                return;
            }
            //for (double[] p: trajPoints)
            //     System.out.println(Arrays.toString(p));
        }

        // convert speed from km/h to lat/lon deg/s
        double speed = config.vehicleSpeed / 20000 * 180 / 3600;
        int trackIndex = (int)currentTrackPos;
        double ratio = currentTrackPos - trackIndex;
        double[] p0 = trajPoints.get(trackIndex);
        double[] p1 = trajPoints.get(trackIndex+1);
        double dLat = p1[0] - p0[0];
        double dLon = p1[1] - p0[1];
        double dist = Math.sqrt(dLat*dLat + dLon*dLon);

        // compute new position
        double time = System.currentTimeMillis() / 1000.;
        this.lat = p0[0] + dLat*ratio;
        this.lon = p0[1] + dLon*ratio;
        this.alt = 193;

        currentTrackPos += speed / dist;

        // Get the intensity of the detected source
        threatLevel = getObservedIntensity();
        detectedAgent = config.source1.getAgent();
        //detectedAgent = config.source1.getAgent();
    }

    @SuppressWarnings("Duplicates")
    private boolean generateRandomTrajectory()
    {
        SimCBRNConfig config = getParentModule().getConfiguration();
        // used fixed start/end coordinates or generate random ones
        double startLat;
        double startLong;
        double endLat;
        double endLong;

        if (trajPoints.isEmpty())
        {
            startLat = config.centerLatitude + (Math.random()-0.5) * config.areaSize;
            startLong = config.centerLongitude + (Math.random()-0.5) * config.areaSize;

            // if fixed start and end locations not given, pick random values within area
            if (config.startLatitude == null || config.startLongitude == null ||
                    config.stopLatitude == null || config.stopLongitude == null)
            {
                startLat = config.centerLatitude + (Math.random()-0.5) * config.areaSize;
                startLong = config.centerLongitude + (Math.random()-0.5) * config.areaSize;
                endLat = config.centerLatitude + (Math.random()-0.5) * config.areaSize;
                endLong = config.centerLongitude + (Math.random()-0.5) * config.areaSize;
            }

            // else use start/end locations provided in configuration
            else
            {
                startLat = config.startLatitude;
                startLong = config.startLongitude;
                endLat = config.stopLatitude;
                endLong = config.stopLongitude;
            }
        }
        else
        {
            // restart from end of previous track
            double[] lastPoint = trajPoints.get(trajPoints.size()-1);
            startLat = lastPoint[0];
            startLong = lastPoint[1];
            endLat = config.centerLatitude + (Math.random()-0.5) * config.areaSize;
            endLong = config.centerLongitude + (Math.random()-0.5) * config.areaSize;
        }


        try
        {
            // request directions using Google API
            URL dirRequest = new URL(config.googleApiUrl + "?origin=" + startLat + "," + startLong +
                    "&destination=" + endLat + "," + endLong + ((config.walkingMode) ? "&mode=walking" : ""));
            log.debug("Google API request: " + dirRequest);
            InputStream is = new BufferedInputStream(dirRequest.openStream());

            // parse JSON track
            JsonParser reader = new JsonParser();
            JsonElement root = reader.parse(new InputStreamReader(is));
            //System.out.println(root);
            JsonArray routes = root.getAsJsonObject().get("routes").getAsJsonArray();
            if (routes.size() == 0)
                throw new Exception("No route available");

            JsonElement polyline = routes.get(0).getAsJsonObject().get("overview_polyline");
            String encodedData = polyline.getAsJsonObject().get("points").getAsString();

            // decode polyline data
            decodePoly(encodedData);
            currentTrackPos = 0.0;
            parentSensor.clearError();
            return true;
        }
        catch (Exception e)
        {
            parentSensor.reportError("Error while retrieving Google directions", e);
            trajPoints.clear();
            try { Thread.sleep(60000L); }
            catch (InterruptedException e1) {}
            return false;
        }
    }

    @SuppressWarnings("Duplicates")
    private void decodePoly(String encoded)
    {
        int index = 0, len = encoded.length();
        int lat = 0, lon = 0;
        trajPoints.clear();

        while (index < len)
        {
            int b, shift = 0, result = 0;
            do
            {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            }
            while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do
            {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            }
            while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lon += dlng;

            double[] p = new double[] {(double) lat / 1E5, (double) lon / 1E5};
            trajPoints.add(p);
        }
    }


    private double getObservedIntensity()
    {
        SimCBRNConfig config = getParentModule().getConfiguration();
        int numSources = config.numSources;
        double avgIntensity = 0;
        for (int i = 0; i < numSources; i++)
        {
            avgIntensity += getParentModule().getConfiguration().source1.findObservedIntensity(lat,
                    lon, alt);
        }
        return avgIntensity/numSources;
    }


    public int findThreatLevel()
    {
        if(threatLevel == 0) return 0;
        else if (threatLevel > 0.00 && threatLevel <= 100) return 1;
        else if (threatLevel > 100 && threatLevel <= 200) return 2;
        else if (threatLevel > 200 && threatLevel <= 300) return 3;
        else if (threatLevel > 300 && threatLevel <= 400) return 4;
        else if (threatLevel > 400 && threatLevel <= 500) return 5;
        else if (threatLevel > 500) return 6;
        else return -1;
    }

    // TODO: add in low threat level here and as an allowable token
    public String findThreatString()
    {
        if(threatLevel == min_threat)
        {
            return "NONE";
        }
        else if(threatLevel > min_threat && threatLevel <= max_threat / 3)
        {
            return "LOW";
        }
        else if(threatLevel > max_threat/3 && threatLevel <= max_threat * 2 / 3)
        {
            return "MEDIUM";
        }
        else if(threatLevel > max_threat * 2 / 3)
        {
            return "HIGH";
        }
        else
        {
            return null;
        }
    }


    public void autoSetWarnStatus()
    {
        if (findThreatLevel() - preWarnStatus > 0.0 && findThreatLevel() > 0.0
                && findThreatLevel() < 300)
        {
            warnStatus = "WARN";
        }

        else if (findThreatLevel() - preWarnStatus > 0.0 && findThreatLevel() >= 300)
        {
            warnStatus = "ALERT";
        }
        else if (findThreatLevel() - preWarnStatus < 0.0 && findThreatLevel() < 300
                && warnStatus.equals("ALERT"))
        {
            warnStatus = "DEALERT";
        }
        else if (findThreatLevel() - preWarnStatus < 0.0 && findThreatLevel() == 0.0
                && warnStatus.equals("WARN"))
        {
            warnStatus = "DEWARN";
        }
        else if(warnStatus.equals("DEALERT") && findThreatLevel() > 0.0
                && findThreatLevel() < 300)
        {
            warnStatus = "WARN";
        }
        else if(warnStatus.equals("DEWARN") && findThreatLevel() == 0.0)
        {
            warnStatus = "NONE";
        }
        else
        {
            warnStatus = warnStatus;
        }

        preWarnStatus = findThreatLevel();
    }
}

