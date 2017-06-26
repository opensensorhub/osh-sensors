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

import datasimulation.PointSource;
import org.sensorhub.api.config.DisplayInfo;
import org.sensorhub.api.config.DisplayInfo.Required;
import org.sensorhub.api.sensor.PositionConfig.LLALocation;
import org.sensorhub.api.sensor.SensorConfig;

/**
 * Created by Ianpa on 5/2/2017.
 */
public class SimCBRNConfig extends SensorConfig
{
    @Required
    @DisplayInfo(desc = "Serial number of the sensor used to generate its unique ID")
    public String serialNumber = "BIR012345";

    /*@DisplayInfo(desc = "Sensor Location")
    public LLALocation location = new LLALocation();*/

    @DisplayInfo(desc = "Point Source 1")
    public PointSource source1 = new PointSource(34.8308, -86.7228, 0, 600, "VX");

    @DisplayInfo(desc = "Number of Sources")
    public int numSources = 1;


    public SimCBRNConfig()
    {
        /*location.lat = 34.8308;
        location.lon = -86.7228;
        location.alt = 0.000;*/
    }

    public String googleApiUrl = "http://maps.googleapis.com/maps/api/directions/json";

    // use these to add specific start and stop locations
    public Double startLatitude = null;  // in degrees
    public Double startLongitude = null;  // in degrees
    public Double stopLatitude = null;  // in degrees
    public Double stopLongitude = null;  // in degrees

    // otherwise use these to generate random start and stop locations
    public double centerLatitude = 34.7300; // in deg
    public double centerLongitude = -86.5850; // in deg
    public double areaSize = 0.1; // in deg

    public double vehicleSpeed = 40; // km/h
    public boolean walkingMode = false;


    /*@Override
    public  LLALocation getLocation(){return location;}*/
}
