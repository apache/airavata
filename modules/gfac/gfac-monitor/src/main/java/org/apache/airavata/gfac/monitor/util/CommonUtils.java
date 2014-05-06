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
package org.apache.airavata.gfac.monitor.util;

import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.core.context.JobExecutionContext;
import org.apache.airavata.gfac.core.handler.GFacHandler;
import org.apache.airavata.gfac.core.handler.GFacHandlerConfig;
import org.apache.airavata.gfac.core.monitor.MonitorID;
import org.apache.airavata.gfac.monitor.HostMonitorData;
import org.apache.airavata.gfac.monitor.UserMonitorData;
import org.apache.airavata.gfac.monitor.exception.AiravataMonitorException;
import org.apache.airavata.schemas.gfac.GsisshHostType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class CommonUtils {
    private final static Logger logger = LoggerFactory.getLogger(CommonUtils.class);

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
    public static boolean isSGE(HostDescription host){
        if("sge".equals(((GsisshHostType)host.getType()).getJobManager())){
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

    public static String getChannelID(String userName,String hostAddress) {
        return userName + "-" + hostAddress;
    }

    public static String getRoutingKey(String userName,String hostAddress) {
        return "*." + userName + "." + hostAddress;
    }

    public static void addMonitortoQueue(BlockingQueue<UserMonitorData> queue, MonitorID monitorID) throws AiravataMonitorException {
        Iterator<UserMonitorData> iterator = queue.iterator();
        while (iterator.hasNext()) {
            UserMonitorData next = iterator.next();
            if (next.getUserName().equals(monitorID.getUserName())) {
                // then this is the right place to update
                List<HostMonitorData> monitorIDs = next.getHostMonitorData();
                for (HostMonitorData host : monitorIDs) {
                    if (host.getHost().equals(monitorID.getHost())) {
                        // ok we found right place to add this monitorID
                        host.addMonitorIDForHost(monitorID);
                        return;
                    }
                }
                // there is a userMonitor object for this user name but no Hosts for this host
                // so we have to create new Hosts
                HostMonitorData hostMonitorData = new HostMonitorData(monitorID.getHost());
                hostMonitorData.addMonitorIDForHost(monitorID);
                next.addHostMonitorData(hostMonitorData);
                return;
            }
        }
        HostMonitorData hostMonitorData = new HostMonitorData(monitorID.getHost());
        hostMonitorData.addMonitorIDForHost(monitorID);

        UserMonitorData userMonitorData = new UserMonitorData(monitorID.getUserName());
        userMonitorData.addHostMonitorData(hostMonitorData);
        try {
            queue.put(userMonitorData);
        } catch (InterruptedException e) {
            throw new AiravataMonitorException(e);
        }
    }
    public static boolean isTheLastJobInQueue(BlockingQueue<MonitorID> queue,MonitorID monitorID){
        Iterator<MonitorID> iterator = queue.iterator();
        while(iterator.hasNext()){
            MonitorID next = iterator.next();
            if(monitorID.getUserName().equals(next.getUserName()) && CommonUtils.isEqual(monitorID.getHost(), next.getHost())){
                return false;
            }
        }
        return true;
    }
    public static void removeMonitorFromQueue(BlockingQueue<UserMonitorData> queue,MonitorID monitorID) throws AiravataMonitorException {
        Iterator<UserMonitorData> iterator = queue.iterator();
        while(iterator.hasNext()){
            UserMonitorData next = iterator.next();
            if(next.getUserName().equals(monitorID.getUserName())){
                // then this is the right place to update
                List<HostMonitorData> hostMonitorData = next.getHostMonitorData();
                for(HostMonitorData iHostMonitorID:hostMonitorData){
                    if(iHostMonitorID.getHost().equals(monitorID.getHost())) {
                        List<MonitorID> monitorIDs = iHostMonitorID.getMonitorIDs();
                        for(MonitorID iMonitorID:monitorIDs){
                            if(iMonitorID.getJobID().equals(monitorID.getJobID())) {
                                // OK we found the object, we cannot do list.remove(object) states of two objects
                                // could be different, thats why we check the jobID
                                monitorIDs.remove(iMonitorID);
                                if(monitorIDs.size()==0) {
                                    hostMonitorData.remove(iHostMonitorID);
                                    if (hostMonitorData.size() == 0) {
                                        // no useful data so we have to remove the element from the queue
                                        queue.remove(next);
                                    }
                                }
                                return;
                            }
                        }
                    }
                }
            }
        }
        throw new AiravataMonitorException("Cannot find the given MonitorID in the queue with userName " +
                monitorID.getUserName() + "  and jobID " + monitorID.getJobID());

    }

    public static boolean isEqual(HostDescription host1,HostDescription host2) {
        if ((host1.getType() instanceof GsisshHostType) && (host2.getType() instanceof GsisshHostType)) {
            GsisshHostType hostType1 = (GsisshHostType)host1.getType();
            GsisshHostType hostType2 = (GsisshHostType)host2.getType();
            if(hostType1.getHostAddress().equals(hostType2.getHostAddress())
                    && hostType1.getJobManager().equals(hostType2.getJobManager())
                    && (hostType1.getPort() == hostType2.getPort())
                    && hostType1.getMonitorMode().equals(hostType2.getMonitorMode())){
                return true;
            }
        } else {
            logger.error("This method is only impmlemented to handle Gsissh host types");
        }
        return false;
    }

    public static void invokeOutFlowHandlers(JobExecutionContext jobExecutionContext) throws GFacException {
        List<GFacHandlerConfig> handlers = jobExecutionContext.getGFacConfiguration().getOutHandlers();

        for (GFacHandlerConfig handlerClassName : handlers) {
            Class<? extends GFacHandler> handlerClass;
            GFacHandler handler;
            try {
                handlerClass = Class.forName(handlerClassName.getClassName().trim()).asSubclass(GFacHandler.class);
                handler = handlerClass.newInstance();
                handler.initProperties(handlerClassName.getProperties());
            } catch (ClassNotFoundException e) {
                logger.error(e.getMessage());
                throw new GFacException("Cannot load handler class " + handlerClassName, e);
            } catch (InstantiationException e) {
                logger.error(e.getMessage());
                throw new GFacException("Cannot instantiate handler class " + handlerClassName, e);
            } catch (IllegalAccessException e) {
                logger.error(e.getMessage());
                throw new GFacException("Cannot instantiate handler class " + handlerClassName, e);
            }
            try {
                handler.invoke(jobExecutionContext);
            } catch (Exception e) {
                // TODO: Better error reporting.
                throw new GFacException("Error Executing a OutFlow Handler", e);
            }
        }
    }
}
