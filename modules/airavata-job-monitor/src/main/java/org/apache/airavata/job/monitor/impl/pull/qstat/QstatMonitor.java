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
package org.apache.airavata.job.monitor.impl.pull.qstat;

import org.apache.airavata.job.monitor.core.PullMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This monitor is based on qstat command which can be run
 * in grid resources and retrieve the job status.
 */
public class QstatMonitor extends PullMonitor {
   private final static Logger logger = LoggerFactory.getLogger(QstatMonitor.class);
 /**
     * This method will can invoke when PullMonitor needs to start
     * and it has to invoke in the frequency specified below,
     * @return if the start process is successful return true else false
     */
    public boolean startPulling(){
        return true;
    }

    /**
     * This is the method to stop the polling process
     * @return if the stopping process is successful return true else false
     */
    public boolean stopPulling(){
         return true;
    }

    public boolean authenticate() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
