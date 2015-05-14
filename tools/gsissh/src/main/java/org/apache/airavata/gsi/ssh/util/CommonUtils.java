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
package org.apache.airavata.gsi.ssh.util;

import org.apache.airavata.gsi.ssh.api.job.*;
import org.apache.airavata.gsi.ssh.impl.JobStatus;

public class CommonUtils {
    /**
     * This returns true if the give job is finished
     * otherwise false
     *
     * @param job
     * @return
     */
    public static boolean isJobFinished(JobDescriptor job) {
        if (JobStatus.C.toString().equals(job.getStatus())) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * This will read
     *
     * @param maxWalltime
     * @return
     */
    public static String maxWallTimeCalculator(int maxWalltime) {
        if (maxWalltime < 60) {
            return "00:" + maxWalltime + ":00";
        } else {
            int minutes = maxWalltime % 60;
            int hours = maxWalltime / 60;
            return hours + ":" + minutes + ":00";
        }
    }
    public static String maxWallTimeCalculatorForLSF(int maxWalltime) {
        if (maxWalltime < 60) {
            return "00:" + maxWalltime;
        } else {
            int minutes = maxWalltime % 60;
            int hours = maxWalltime / 60;
            return hours + ":" + minutes;
        }
    }
    public static JobManagerConfiguration getPBSJobManager(String installedPath) {
        return new PBSJobConfiguration("PBSTemplate.xslt",".pbs", installedPath, new PBSOutputParser());
    }

    public static JobManagerConfiguration getSLURMJobManager(String installedPath) {
        return new SlurmJobConfiguration("SLURMTemplate.xslt", ".slurm", installedPath, new SlurmOutputParser());
    }

     public static JobManagerConfiguration getSGEJobManager(String installedPath) {
        return new UGEJobConfiguration("UGETemplate.xslt", ".pbs", installedPath, new UGEOutputParser());
    }

    public static JobManagerConfiguration getLSFJobManager(String installedPath) {
        return new LSFJobConfiguration("LSFTemplate.xslt", ".lsf", installedPath, new LSFOutputParser());
    }
}
