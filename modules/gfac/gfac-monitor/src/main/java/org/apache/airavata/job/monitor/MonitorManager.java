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
package org.apache.airavata.job.monitor;

import com.google.common.eventbus.EventBus;
import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.job.monitor.core.Monitor;
import org.apache.airavata.job.monitor.core.PullMonitor;
import org.apache.airavata.job.monitor.core.PushMonitor;
import org.apache.airavata.job.monitor.event.MonitorPublisher;
import org.apache.airavata.job.monitor.exception.AiravataMonitorException;
import org.apache.airavata.job.monitor.impl.LocalJobMonitor;
import org.apache.airavata.job.monitor.impl.pull.qstat.QstatMonitor;
import org.apache.airavata.job.monitor.impl.push.amqp.AMQPMonitor;
import org.apache.airavata.job.monitor.util.CommonUtils;
import org.apache.airavata.persistance.registry.jpa.impl.RegistryImpl;
import org.apache.airavata.registry.cpi.Registry;
import org.apache.airavata.schemas.gfac.GlobusHostType;
import org.apache.airavata.schemas.gfac.GsisshHostType;
import org.apache.airavata.schemas.gfac.SSHHostType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/*
this is the manager class for monitoring system of airavata,
This simply handle the monitoring flow of the system.
Keeps available jobs to monitor in a queue and once they are done
remove them from the queue, this will be done by AiravataJobUpdator.
 */
public class MonitorManager {
    private final static Logger logger = LoggerFactory.getLogger(MonitorManager.class);

    private List<PullMonitor> pullMonitors;    //todo though we have a List we only support one at a time

    private List<PushMonitor> pushMonitors;   //todo we need to support multiple monitors dynamically

    private BlockingQueue<UserMonitorData> pullQueue;

    private BlockingQueue<MonitorID> pushQueue;

    private BlockingQueue<MonitorID> localJobQueue;

    private BlockingQueue<MonitorID> finishQueue;

    private MonitorPublisher monitorPublisher;

    private Monitor localJobMonitor;


    /**
     * This will initialize the major monitoring system.
     */
    public MonitorManager() {
        pullMonitors = new ArrayList<PullMonitor>();
        pushMonitors = new ArrayList<PushMonitor>();
        pullQueue = new LinkedBlockingQueue<UserMonitorData>();
        pushQueue = new LinkedBlockingQueue<MonitorID>();
        finishQueue = new LinkedBlockingQueue<MonitorID>();
        localJobQueue = new LinkedBlockingQueue<MonitorID>();
        monitorPublisher = new MonitorPublisher(new EventBus());
        registerListener(new AiravataJobStatusUpdator(new RegistryImpl(), getFinishQueue()));
    }

    public MonitorManager(Registry registry) {
        pullMonitors = new ArrayList<PullMonitor>();
        pushMonitors = new ArrayList<PushMonitor>();
        pullQueue = new LinkedBlockingQueue<UserMonitorData>();
        pushQueue = new LinkedBlockingQueue<MonitorID>();
        finishQueue = new LinkedBlockingQueue<MonitorID>();
        localJobQueue = new LinkedBlockingQueue<MonitorID>();
        monitorPublisher = new MonitorPublisher(new EventBus());
        registerListener(new AiravataJobStatusUpdator(registry, getFinishQueue()));
    }
    /**
     * This can be use to add an empty AMQPMonitor object to the monitor system
     * and tihs method will take care of the initialization
     * todo may be we need to move this to some other class
     * @param monitor
     */
    public void addAMQPMonitor(AMQPMonitor monitor) {
        monitor.setPublisher(this.getMonitorPublisher());
        monitor.setFinishQueue(this.getFinishQueue());
        monitor.setRunningQueue(this.getPushQueue());
        addPushMonitor(monitor);
    }


    /**
     * This can be use to add an empty AMQPMonitor object to the monitor system
     * and tihs method will take care of the initialization
     * todo may be we need to move this to some other class
     * @param monitor
     */
    public void addLocalMonitor(LocalJobMonitor monitor) {
        monitor.setPublisher(this.getMonitorPublisher());
        monitor.setJobQueue(this.getLocalJobQueue());
        localJobMonitor = monitor;
    }

    /**
     * This can be used to adda a QstatMonitor and it will take care of
     * the initialization of QstatMonitor
     * //todo may be we need to move this to some other class
     * @param qstatMonitor
     */
    public void addQstatMonitor(QstatMonitor qstatMonitor) {
        qstatMonitor.setPublisher(this.getMonitorPublisher());
        qstatMonitor.setQueue(this.getPullQueue());
        addPullMonitor(qstatMonitor);

    }

    /**
     * To deal with the statuses users can write their own listener and implement their own logic
     *
     * @param listener Any class can be written and if you want the JobStatus object to be taken from the bus, just
     *                 have to put @subscribe as an annotation to your method to recieve the JobStatus object from the bus.
     */
    public void registerListener(Object listener) {
        monitorPublisher.registerListener(listener);
    }

    /**
     * todo write
     *
     * @param monitor
     */
    public void addPushMonitor(PushMonitor monitor) {
        pushMonitors.add(monitor);
    }

    /**
     * todo write
     *
     * @param monitor
     */
    public void addPullMonitor(PullMonitor monitor) {
        pullMonitors.add(monitor);
    }

    /**
     * Adding this method will trigger the thread in launchMonitor and notify it
     * This is going to be useful during the startup of the launching process
     *
     * @param monitorID
     * @throws AiravataMonitorException
     */
    public void addAJobToMonitor(MonitorID monitorID) throws AiravataMonitorException, InterruptedException {

        if (monitorID.getHost().getType() instanceof GsisshHostType) {
            GsisshHostType host = (GsisshHostType) monitorID.getHost().getType();
            if ("".equals(host.getMonitorMode()) || host.getMonitorMode() == null
                    || Constants.PULL.equals(host.getMonitorMode())) {
                CommonUtils.addMonitortoQueue(pullQueue, monitorID);
            } else if (Constants.PUSH.equals(host.getMonitorMode())) {
                pushQueue.put(monitorID);
                finishQueue.put(monitorID);
            }
        } else if(monitorID.getHost().getType() instanceof GlobusHostType){
            logger.error("Monitoring does not support GlubusHostType resources");
        } else if(monitorID.getHost().getType() instanceof SSHHostType) {
            logger.error("Monitoring does not support GlubusHostType resources");
            localJobQueue.add(monitorID);
        } else {
            // we assume this is a type of localJobtype
            localJobQueue.add(monitorID);
        }
    }

    /**
     * This method should be invoked before adding any elements to monitorQueue
     * In this method we assume that we give higher preference to Push
     * Monitorig mechanism if there's any configured, otherwise Pull
     * monitoring will be launched.
     * Ex: If there's a reasource which doesn't support Push, we have
     * to live with Pull MOnitoring.
     *
     * @throws AiravataMonitorException
     */
    public void launchMonitor() throws AiravataMonitorException {
        //no push monitor is configured so we launch pull monitor
        int index = 0;
        if(localJobMonitor != null){
            (new Thread(localJobMonitor)).start();
        }

        for (PullMonitor monitor : pullMonitors) {
            (new Thread(monitor)).start();
        }

        //todo fix this
        for (PushMonitor monitor : pushMonitors) {
            (new Thread(monitor)).start();
        }
    }

    /**
     * This method should be invoked before adding any elements to monitorQueue
     * In this method we assume that we give higher preference to Push
     * Monitorig mechanism if there's any configured, otherwise Pull
     * monitoring will be launched.
     * Ex: If there's a reasource which doesn't support Push, we have
     * to live with Pull MOnitoring.
     *
     * @throws AiravataMonitorException
     */
    public void stopMonitor() throws AiravataMonitorException {
        //no push monitor is configured so we launch pull monitor
        int index = 0;
        if(localJobMonitor != null){
            (new Thread(localJobMonitor)).stop();
        }

        for (PullMonitor monitor : pullMonitors) {
            (new Thread(monitor)).stop();
        }

        //todo fix this
        for (PushMonitor monitor : pushMonitors) {
            (new Thread(monitor)).stop();
        }
    }
    /* getter setters for the private variables */

    public List<PullMonitor> getPullMonitors() {
        return pullMonitors;
    }

    public void setPullMonitors(List<PullMonitor> pullMonitors) {
        this.pullMonitors = pullMonitors;
    }

    public List<PushMonitor> getPushMonitors() {
        return pushMonitors;
    }

    public void setPushMonitors(List<PushMonitor> pushMonitors) {
        this.pushMonitors = pushMonitors;
    }

    public BlockingQueue<UserMonitorData> getPullQueue() {
        return pullQueue;
    }

    public void setPullQueue(BlockingQueue<UserMonitorData> pullQueue) {
        this.pullQueue = pullQueue;
    }

    public MonitorPublisher getMonitorPublisher() {
        return monitorPublisher;
    }

    public void setMonitorPublisher(MonitorPublisher monitorPublisher) {
        this.monitorPublisher = monitorPublisher;
    }

    public BlockingQueue<MonitorID> getFinishQueue() {
        return finishQueue;
    }

    public void setFinishQueue(BlockingQueue<MonitorID> finishQueue) {
        this.finishQueue = finishQueue;
    }

    public BlockingQueue<MonitorID> getPushQueue() {
        return pushQueue;
    }

    public void setPushQueue(BlockingQueue<MonitorID> pushQueue) {
        this.pushQueue = pushQueue;
    }

    public BlockingQueue<MonitorID> getLocalJobQueue() {
        return localJobQueue;
    }

    public void setLocalJobQueue(BlockingQueue<MonitorID> localJobQueue) {
        this.localJobQueue = localJobQueue;
    }
}
