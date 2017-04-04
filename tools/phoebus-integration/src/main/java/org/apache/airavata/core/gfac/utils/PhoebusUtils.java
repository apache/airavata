/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.core.gfac.utils;

import org.apache.airavata.common.exception.UnspecifiedApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;

public class PhoebusUtils {
	private static String PHOEBUS_DC_XIO_DRIVERS="dc.phoebus.xio_driver_configurations";

    public static boolean isPhoebusDriverConfigurationsDefined(String hostAddress) throws Exception{
        try {
            return getPhoebusDataChannelXIODriverParameters(hostAddress)!=null;
        } catch (UnspecifiedApplicationSettingsException e) {
            return false;
        }
    }

    public static String getPhoebusDataChannelXIODriverParameters(String hostAddress) throws Exception{
        String driverString = ServerSettings.getSetting(PHOEBUS_DC_XIO_DRIVERS);
        String[] hostList = driverString.split(";");
        for (String hostString : hostList) {
            String[] driverData = hostString.split("=");
            if (driverData.length!=2){
                throw new Exception("Invalid Phoebus XIO drivers settings!!!");
            }
            if (hostAddress.equalsIgnoreCase(driverData[0])){
                return driverData[1];
            }
        }
        throw new Exception("Phoebus XIO drivers not defined for "+hostAddress);
    }

}
