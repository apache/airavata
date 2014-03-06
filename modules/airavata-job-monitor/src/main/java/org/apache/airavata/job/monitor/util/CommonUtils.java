/*
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
 *
*/
package org.apache.airavata.job.monitor.util;

import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.job.monitor.MonitorID;
import org.apache.airavata.schemas.gfac.GsisshHostType;

public class CommonUtils {
    public static boolean isPBSHost(HostDescription host){
        if("pbs".equals(((GsisshHostType)host.getType()).getJobManager()) ||
                "".equals(((GsisshHostType)host.getType()).getJobManager())){
         return true;
        }else{
            // default is pbs so we return true
            return false;
        }
    }
    public static boolean isSlurm(HostDescription host){
        if("slurm".equals(((GsisshHostType)host.getType()).getJobManager())){
         return true;
        }else{
            // default is pbs so we return true
            return false;
        }
    }
    public static String getChannelID(MonitorID monitorID) {
        return monitorID.getUserName() + "-" + monitorID.getHost().getType().getHostName();
    }

    public static String getRoutingKey(MonitorID monitorID) {
        return "*." + monitorID.getUserName() + "." + monitorID.getHost().getType().getHostAddress();
    }
}
