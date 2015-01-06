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
package org.apache.airavata.gfac.monitor.handlers;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.gfac.core.context.JobExecutionContext;
import org.apache.airavata.gfac.core.cpi.BetterGfacImpl;
import org.apache.airavata.gfac.core.handler.GFacHandlerException;
import org.apache.airavata.gfac.core.handler.ThreadedHandler;
import org.apache.airavata.gfac.core.monitor.MonitorID;
import org.apache.airavata.gfac.monitor.HPCMonitorID;
import org.apache.airavata.gfac.monitor.impl.push.amqp.AMQPMonitor;
import org.apache.airavata.gsi.ssh.api.authentication.AuthenticationInfo;
import org.apache.airavata.gsi.ssh.impl.authentication.MyProxyAuthenticationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *   this handler is responsible monitoring jobs in push mode
 *   and currently this support multiple push monitoring in grid resource
 */
public class GridPushMonitorHandler extends ThreadedHandler {
    private final static Logger logger= LoggerFactory.getLogger(GridPushMonitorHandler.class);

    private AMQPMonitor amqpMonitor;

    private AuthenticationInfo authenticationInfo;

    @Override
    public void initProperties(Properties properties) throws GFacHandlerException {
        String myProxyUser=null;
        try{
            myProxyUser = ServerSettings.getSetting("myproxy.username");
            String myProxyPass = ServerSettings.getSetting("myproxy.password");
            String certPath = ServerSettings.getSetting("trusted.cert.location");
            String myProxyServer = ServerSettings.getSetting("myproxy.server");
            setAuthenticationInfo(new MyProxyAuthenticationInfo(myProxyUser, myProxyPass, myProxyServer,
                    7512, 17280000, certPath));

            String hostList=(String)properties.get("hosts");
            String proxyFilePath = ServerSettings.getSetting("proxy.file.path");
            String connectionName=ServerSettings.getSetting("connection.name");
            LinkedBlockingQueue<MonitorID> pushQueue = new LinkedBlockingQueue<MonitorID>();
            LinkedBlockingQueue<MonitorID> finishQueue = new LinkedBlockingQueue<MonitorID>();
            List<String> hosts= Arrays.asList(hostList.split(","));
            amqpMonitor=new AMQPMonitor(BetterGfacImpl.getMonitorPublisher(),pushQueue,finishQueue,proxyFilePath,connectionName,hosts);
        }catch (ApplicationSettingsException e){
            logger.error(e.getMessage(), e);
            throw new GFacHandlerException(e.getMessage(), e);
        }
    }

    @Override
    public void run() {
        amqpMonitor.run();
    }

    public void invoke(JobExecutionContext jobExecutionContext) throws GFacHandlerException{
        super.invoke(jobExecutionContext);
        MonitorID monitorID=new HPCMonitorID(getAuthenticationInfo(),jobExecutionContext);
        amqpMonitor.getRunningQueue().add(monitorID);
    }

    public AMQPMonitor getAmqpMonitor() {
        return amqpMonitor;
    }

    public void setAmqpMonitor(AMQPMonitor amqpMonitor) {
        this.amqpMonitor = amqpMonitor;
    }

    public AuthenticationInfo getAuthenticationInfo() {
        return authenticationInfo;
    }

    public void setAuthenticationInfo(AuthenticationInfo authenticationInfo) {
        this.authenticationInfo = authenticationInfo;
    }
}
