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

import org.apache.airavata.job.monitor.MonitorID;
import org.apache.airavata.job.monitor.exception.AiravataMonitorException;

/**
 * PushMonitors can implement this interface
 * Since the pull and push based monitoring required different
 * operations, PullMonitor will be useful.
 * This interface will allow users to program Push monitors separately
 */
public abstract class PushMonitor extends AiravataAbstractMonitor {
    /**
     * This method can be invoked to register a listener with the
     * remote monitoring system, ideally inside this method users will be
     * writing some client listener code for the remote monitoring system,
     * this will be a simple wrapper around any client for the remote Monitor.
     * @param monitorID
     * @return
     */
    public abstract boolean registerListener(MonitorID monitorID)throws AiravataMonitorException;

    /**
     * This method can be invoked to unregister a listener with the
     * remote monitoring system, ideally inside this method users will be
     * writing some client listener code for the remote monitoring system,
     * this will be a simple wrapper around any client for the remote Monitor.
     * @param monitorID
     * @return
     */
    public abstract boolean unRegisterListener(MonitorID monitorID)throws AiravataMonitorException;

    /**
     * This can be used to stop the registration thread
     * @return
     * @throws AiravataMonitorException
     */
    public abstract boolean stopRegister()throws AiravataMonitorException;

}
