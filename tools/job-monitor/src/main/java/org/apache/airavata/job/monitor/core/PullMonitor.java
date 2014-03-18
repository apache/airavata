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
package org.apache.airavata.job.monitor.core;

import org.apache.airavata.job.monitor.exception.AiravataMonitorException;

/**
 * PullMonitors can implement this interface
 * Since the pull and push based monitoring required different
 * operations, PullMonitor will be useful.
 * This will allow users to program Pull monitors separately
 */
public abstract class PullMonitor extends AiravataAbstractMonitor{

    private int pollingFrequence;
    /**
     * This method will can invoke when PullMonitor needs to start
     * and it has to invoke in the frequency specified below,
     * @return if the start process is successful return true else false
     */
    public abstract boolean startPulling() throws AiravataMonitorException;

    /**
     * This is the method to stop the polling process
     * @return if the stopping process is successful return true else false
     */
    public abstract boolean stopPulling()throws AiravataMonitorException;

    /**
     * this method can be used to set the polling frequencey or otherwise
     * can implement a polling mechanism, and implement how to do
     * @param frequence
     */
    public void setPollingFrequence(int frequence){
        this.pollingFrequence = frequence;
    }

    /**
     * this method can be used to get the polling frequencey or otherwise
     * can implement a polling mechanism, and implement how to do
     * @return
     */
    public int getPollingFrequence(){
        return this.pollingFrequence;
    }
}
