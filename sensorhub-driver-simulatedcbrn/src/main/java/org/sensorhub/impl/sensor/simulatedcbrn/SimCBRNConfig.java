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

    @DisplayInfo(desc = "Sensor Location")
    public LLALocation location = new LLALocation();


    public SimCBRNConfig()
    {
        location.lat = 34.8308;
        location.lon = -86.7228;
        location.alt = 0.000;
    }


    @Override
    public  LLALocation getLocation(){return location;}
}
