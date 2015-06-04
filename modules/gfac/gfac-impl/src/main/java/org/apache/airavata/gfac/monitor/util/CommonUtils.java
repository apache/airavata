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

import org.apache.airavata.common.logger.AiravataLogger;
import org.apache.airavata.common.logger.AiravataLoggerFactory;
import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.core.context.JobExecutionContext;
import org.apache.airavata.gfac.core.handler.GFacHandler;
import org.apache.airavata.gfac.core.handler.GFacHandlerConfig;
import org.apache.airavata.gfac.core.monitor.MonitorID;
import org.apache.airavata.gfac.monitor.HostMonitorData;
import org.apache.airavata.gfac.monitor.UserMonitorData;
import org.apache.airavata.gfac.monitor.exception.AiravataMonitorException;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class CommonUtils {
    private final static AiravataLogger logger = AiravataLoggerFactory.getLogger(CommonUtils.class);

    public static String getChannelID(MonitorID monitorID) {
        return monitorID.getUserName() + "-" + monitorID.getComputeResourceDescription().getHostName();
    }

    public static String getRoutingKey(MonitorID monitorID) {
        return "*." + monitorID.getUserName() + "." + monitorID.getComputeResourceDescription().getIpAddresses().get(0);
    }

    public static String getChannelID(String userName,String hostAddress) {
        return userName + "-" + hostAddress;
    }

    public static String getRoutingKey(String userName,String hostAddress) {
        return "*." + userName + "." + hostAddress;
    }

    public static void addMonitortoQueue(BlockingQueue<UserMonitorData> queue, MonitorID monitorID, JobExecutionContext jobExecutionContext) throws AiravataMonitorException {
        synchronized (queue) {
            Iterator<UserMonitorData> iterator = queue.iterator();
            while (iterator.hasNext()) {
                UserMonitorData next = iterator.next();
                if (next.getUserName().equals(monitorID.getUserName())) {
                    // then this is the right place to update
                    List<HostMonitorData> monitorIDs = next.getHostMonitorData();
                    for (HostMonitorData host : monitorIDs) {
                        if (isEqual(host.getComputeResourceDescription(), monitorID.getComputeResourceDescription())) {
                            // ok we found right place to add this monitorID
                            host.addMonitorIDForHost(monitorID);
                            logger.debugId(monitorID.getJobID(), "Added new job to the monitoring queue, experiment {}," +
                                    " task {}", monitorID.getExperimentID(), monitorID.getTaskID());
                            return;
                        }
                    }
                    // there is a userMonitor object for this user name but no Hosts for this host
                    // so we have to create new Hosts
                    HostMonitorData hostMonitorData = new HostMonitorData(jobExecutionContext);
                    hostMonitorData.addMonitorIDForHost(monitorID);
                    next.addHostMonitorData(hostMonitorData);
                    logger.debugId(monitorID.getJobID(), "Added new job to the monitoring queue, experiment {}," +
                            " task {}", monitorID.getExperimentID(), monitorID.getTaskID());
                    return;
                }
            }
            HostMonitorData hostMonitorData = new HostMonitorData(jobExecutionContext);
            hostMonitorData.addMonitorIDForHost(monitorID);

            UserMonitorData userMonitorData = new UserMonitorData(monitorID.getUserName());
            userMonitorData.addHostMonitorData(hostMonitorData);
            try {
                queue.put(userMonitorData);
                logger.debugId(monitorID.getJobID(), "Added new job to the monitoring queue, experiment {}," +
                        " task {}", monitorID.getExperimentID(), monitorID.getTaskID());
            } catch (InterruptedException e) {
                throw new AiravataMonitorException(e);
            }
        }
    }

    private static boolean isEqual(ComputeResourceDescription comRes_1, ComputeResourceDescription comRes_2) {
        return comRes_1.getComputeResourceId().equals(comRes_2.getComputeResourceId()) &&
                comRes_1.getHostName().equals(comRes_2.getHostName());
    }

    public static boolean isTheLastJobInQueue(BlockingQueue<MonitorID> queue,MonitorID monitorID){
        Iterator<MonitorID> iterator = queue.iterator();
        while(iterator.hasNext()){
            MonitorID next = iterator.next();
            if (monitorID.getUserName().equals(next.getUserName()) &&
                    CommonUtils.isEqual(monitorID.getComputeResourceDescription(), next.getComputeResourceDescription())) {
                return false;
            }
        }
        return true;
    }

    /**
     * This method doesn't have to be synchronized because it will be invoked by HPCPullMonitor which already synchronized
     * @param monitorID
     * @throws AiravataMonitorException
     */
    public static void removeMonitorFromQueue(UserMonitorData userMonitorData, MonitorID monitorID) throws AiravataMonitorException {
                if (userMonitorData.getUserName().equals(monitorID.getUserName())) {
                    // then this is the right place to update
                    List<HostMonitorData> hostMonitorData = userMonitorData.getHostMonitorData();
                    Iterator<HostMonitorData> iterator1 = hostMonitorData.iterator();
                    while (iterator1.hasNext()) {
                        HostMonitorData iHostMonitorID = iterator1.next();
                        if (isEqual(iHostMonitorID.getComputeResourceDescription(), monitorID.getComputeResourceDescription())) {
                            Iterator<MonitorID> iterator2 = iHostMonitorID.getMonitorIDs().iterator();
                            while (iterator2.hasNext()) {
                                MonitorID iMonitorID = iterator2.next();
                                if (iMonitorID.getJobID().equals(monitorID.getJobID())
                                        || iMonitorID.getJobName().equals(monitorID.getJobName())) {
                                    // OK we found the object, we cannot do list.remove(object) states of two objects
                                    // could be different, thats why we check the jobID
                                    iterator2.remove();
                                    logger.infoId(monitorID.getJobID(), "Removed the jobId: {} JobName: {} from monitoring last " +
                                            "status:{}", monitorID.getJobID(),monitorID.getJobName(), monitorID.getStatus().toString());

                                    return;
                                }
                            }
                        }
                    }
                }
        logger.info("Cannot find the given MonitorID in the queue with userName " +
                monitorID.getUserName() + "  and jobID " + monitorID.getJobID());
        logger.info("This might not be an error because someone else removed this job from the queue");
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

        /**
         *  Update job count for a given set of paths.
         * @param curatorClient - CuratorFramework instance
         * @param changeCountMap - map of change job count with relevant path
         * @param isAdd - Should add or reduce existing job count by the given job count.
         */
    public static void updateZkWithJobCount(CuratorFramework curatorClient, final Map<String, Integer> changeCountMap, boolean isAdd) {
        StringBuilder changeZNodePaths = new StringBuilder();
        try {
            for (String path : changeCountMap.keySet()) {
                if (isAdd) {
                    CommonUtils.checkAndCreateZNode(curatorClient, path);
                }
                byte[] byteData = curatorClient.getData().forPath(path);
                String nodeData;
                if (byteData == null) {
                    if (isAdd) {
                        curatorClient.setData().withVersion(-1).forPath(path, String.valueOf(changeCountMap.get(path)).getBytes());
                    } else {
                        // This is not possible, but we handle in case there any data zookeeper communication failure
                        logger.warn("Couldn't reduce job count in " + path + " as it returns null data. Hence reset the job count to 0");
                        curatorClient.setData().withVersion(-1).forPath(path, "0".getBytes());
                    }
                } else {
                    nodeData = new String(byteData);
                    if (isAdd) {
                        curatorClient.setData().withVersion(-1).forPath(path,
                                String.valueOf(changeCountMap.get(path) + Integer.parseInt(nodeData)).getBytes());
                    } else {
                        int previousCount = Integer.parseInt(nodeData);
                        int removeCount = changeCountMap.get(path);
                        if (previousCount >= removeCount) {
                            curatorClient.setData().withVersion(-1).forPath(path,
                                    String.valueOf(previousCount - removeCount).getBytes());
                        } else {
                            // This is not possible, do we need to reset the job count to 0 ?
                            logger.error("Requested remove job count is " + removeCount +
                                    " which is higher than the existing job count " + previousCount
                                    + " in  " + path + " path.");
                        }
                    }
                }
                changeZNodePaths.append(path).append(":");
            }

            // update stat node to trigger orchestrator watchers
            if (changeCountMap.size() > 0) {
                changeZNodePaths.deleteCharAt(changeZNodePaths.length() - 1);
                curatorClient.setData().withVersion(-1).forPath("/" + Constants.STAT, changeZNodePaths.toString().getBytes());
            }
        } catch (Exception e) {
            logger.error("Error while writing job count to zookeeper", e);
        }

    }

    /**
     * Increase job count by one and update the zookeeper
     * @param monitorID - Job monitorId
     */
    public static void increaseZkJobCount(MonitorID monitorID) {
        Map<String, Integer> addMap = new HashMap<String, Integer>();
        addMap.put(CommonUtils.getJobCountUpdatePath(monitorID), 1);
        updateZkWithJobCount(monitorID.getJobExecutionContext().getCuratorClient(), addMap, true);
    }

    /**
     * Construct and return the path for a given MonitorID , eg: /stat/{username}/{resourceName}/job
     * @param monitorID - Job monitorId
     * @return
     */
    public static String getJobCountUpdatePath(MonitorID monitorID){
        return new StringBuilder("/").append(Constants.STAT).append("/").append(monitorID.getUserName())
                .append("/").append(monitorID.getComputeResourceDescription().getHostName()).append("/").append(Constants.JOB).toString();
    }

    /**
     * Check whether znode is exist in given path if not create a new znode
     * @param curatorClient - zookeeper instance
     * @param path - path to check znode
     * @throws KeeperException
     * @throws InterruptedException
     */
    private static void checkAndCreateZNode(CuratorFramework curatorClient , String path) throws Exception {
        if (curatorClient.checkExists().forPath(path) == null) { // if znode doesn't exist
            if (path.lastIndexOf("/") > 1) {  // recursively traverse to parent znode and check parent exist
                checkAndCreateZNode(curatorClient, (path.substring(0, path.lastIndexOf("/"))));
            }
            curatorClient.create().withMode(CreateMode.PERSISTENT).withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE).forPath(path);
        }
    }
}
