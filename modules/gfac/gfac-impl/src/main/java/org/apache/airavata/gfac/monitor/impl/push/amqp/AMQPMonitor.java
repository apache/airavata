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
package org.apache.airavata.gfac.monitor.impl.push.amqp;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import org.apache.airavata.common.utils.MonitorPublisher;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.gfac.core.monitor.MonitorID;
import org.apache.airavata.gfac.monitor.core.PushMonitor;
import org.apache.airavata.gfac.monitor.exception.AiravataMonitorException;
import org.apache.airavata.gfac.monitor.util.AMQPConnectionUtil;
import org.apache.airavata.gfac.monitor.util.CommonUtils;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.messaging.event.JobIdentifier;
import org.apache.airavata.model.messaging.event.JobStatusChangeEvent;
import org.apache.airavata.model.workspace.experiment.JobState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

/**
 * This is the implementation for AMQP based finishQueue, this uses
 * rabbitmq client to recieve AMQP based monitoring data from
 * mostly excede resources.
 */
public class AMQPMonitor extends PushMonitor {
    private final static Logger logger = LoggerFactory.getLogger(AMQPMonitor.class);


    /* this will keep all the channels available in the system, we do not create
      channels for all the jobs submitted, but we create channels for each user for each
      host.
    */
    private Map<String, Channel> availableChannels;

    private MonitorPublisher publisher;

    private MonitorPublisher localPublisher;

    private BlockingQueue<MonitorID> runningQueue;

    private BlockingQueue<MonitorID> finishQueue;

    private String connectionName;

    private String proxyPath;

    private List<String> amqpHosts;

    private boolean startRegister;

    public AMQPMonitor(){

    }
    public AMQPMonitor(MonitorPublisher publisher, BlockingQueue<MonitorID> runningQueue,
                       BlockingQueue<MonitorID> finishQueue,
                       String proxyPath,String connectionName,List<String> hosts) {
        this.publisher = publisher;
        this.runningQueue = runningQueue;        // these will be initialized by the MonitorManager
        this.finishQueue = finishQueue;          // these will be initialized by the MonitorManager
        this.availableChannels = new HashMap<String, Channel>();
        this.connectionName = connectionName;
        this.proxyPath = proxyPath;
        this.amqpHosts = hosts;
        this.localPublisher = new MonitorPublisher(new EventBus());
        this.localPublisher.registerListener(this);
    }

    public void initialize(String proxyPath, String connectionName, List<String> hosts) {
        this.availableChannels = new HashMap<String, Channel>();
        this.connectionName = connectionName;
        this.proxyPath = proxyPath;
        this.amqpHosts = hosts;
        this.localPublisher = new MonitorPublisher(new EventBus());
        this.localPublisher.registerListener(this);
    }

    @Override
    public boolean registerListener(MonitorID monitorID) throws AiravataMonitorException {
        // we subscribe to read user-host based subscription
        ComputeResourceDescription computeResourceDescription = monitorID.getComputeResourceDescription();
        if (computeResourceDescription.isSetIpAddresses() && computeResourceDescription.getIpAddresses().size() > 0) {
            // we get first ip address for the moment
            String hostAddress = computeResourceDescription.getIpAddresses().get(0);
            // in amqp case there are no multiple jobs per each host, because once a job is put in to the queue it
            // will be picked by the Monitor, so jobs will not stay in this queueu but jobs will stay in finishQueue
            String channelID = CommonUtils.getChannelID(monitorID);
            if (availableChannels.get(channelID) == null) {
                try {
                    //todo need to fix this rather getting it from a file
                    Connection connection = AMQPConnectionUtil.connect(amqpHosts, connectionName, proxyPath);
                    Channel channel = null;
                    channel = connection.createChannel();
                    availableChannels.put(channelID, channel);
                    String queueName = channel.queueDeclare().getQueue();

                    BasicConsumer consumer = new
                            BasicConsumer(new JSONMessageParser(), localPublisher);          // here we use local publisher
                    channel.basicConsume(queueName, true, consumer);
                    String filterString = CommonUtils.getRoutingKey(monitorID.getUserName(), hostAddress);
                    // here we queuebind to a particular user in a particular machine
                    channel.queueBind(queueName, "glue2.computing_activity", filterString);
                    logger.info("Using filtering string to monitor: " + filterString);
                } catch (IOException e) {
                    logger.error("Error creating the connection to finishQueue the job:" + monitorID.getUserName());
                }
            }
        } else {
            throw new AiravataMonitorException("Couldn't register monitor for jobId :" + monitorID.getJobID() +
                    " , ComputeResourceDescription " + computeResourceDescription.getHostName() + " doesn't has an " +
                    "IpAddress with it");
        }
        return true;
    }

    public void run() {
        // before going to the while true mode we start unregister thread
        startRegister = true; // this will be unset by someone else
        while (startRegister || !ServerSettings.isStopAllThreads()) {
            try {
                MonitorID take = runningQueue.take();
                this.registerListener(take);
            } catch (AiravataMonitorException e) { // catch any exceptino inside the loop
                logger.error(e.getMessage(), e);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            } catch (Exception e){
                logger.error(e.getMessage(), e);
            }
        }
        Set<String> strings = availableChannels.keySet();
        for(String key:strings) {
            Channel channel = availableChannels.get(key);
            try {
                channel.close();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    @Subscribe
    public boolean unRegisterListener(MonitorID monitorID) throws AiravataMonitorException {
        Iterator<MonitorID> iterator = finishQueue.iterator();
        MonitorID next = null;
        while(iterator.hasNext()){
            next = iterator.next();
            if(next.getJobID().endsWith(monitorID.getJobID())){
                break;
            }
        }
        if(next == null) {
            logger.error("Job has removed from the queue, old obsolete message recieved");
            return false;
        }
        String channelID = CommonUtils.getChannelID(next);
        if (JobState.FAILED.equals(monitorID.getStatus()) || JobState.COMPLETE.equals(monitorID.getStatus())) {
            finishQueue.remove(next);

            // if this is the last job in the queue at this point with the same username and same host we
            // close the channel and close the connection and remove it from availableChannels
            if (CommonUtils.isTheLastJobInQueue(finishQueue, next)) {
                logger.info("There are no jobs to monitor for common ChannelID:" + channelID + " , so we unsubscribe it" +
                        ", incase new job created we do subscribe again");
                Channel channel = availableChannels.get(channelID);
                if (channel == null) {
                    logger.error("Already Unregistered the listener");
                    throw new AiravataMonitorException("Already Unregistered the listener");
                } else {
                    try {
                        channel.queueUnbind(channel.queueDeclare().getQueue(), "glue2.computing_activity", CommonUtils.getRoutingKey(next));
                        channel.close();
                        channel.getConnection().close();
                        availableChannels.remove(channelID);
                    } catch (IOException e) {
                        logger.error("Error unregistering the listener");
                        throw new AiravataMonitorException("Error unregistering the listener");
                    }
                }
            }
        }
        next.setStatus(monitorID.getStatus());
        JobIdentifier jobIdentity = new JobIdentifier(next.getJobID(),
                                                     next.getTaskID(),
                                                     next.getWorkflowNodeID(),
                                                     next.getExperimentID(),
                                                     next.getJobExecutionContext().getGatewayID());
        publisher.publish(new JobStatusChangeEvent(next.getStatus(),jobIdentity));
        return true;
    }
    @Override
    public boolean stopRegister() throws AiravataMonitorException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Map<String, Channel> getAvailableChannels() {
        return availableChannels;
    }

    public void setAvailableChannels(Map<String, Channel> availableChannels) {
        this.availableChannels = availableChannels;
    }

    public MonitorPublisher getPublisher() {
        return publisher;
    }

    public void setPublisher(MonitorPublisher publisher) {
        this.publisher = publisher;
    }

    public BlockingQueue<MonitorID> getRunningQueue() {
        return runningQueue;
    }

    public void setRunningQueue(BlockingQueue<MonitorID> runningQueue) {
        this.runningQueue = runningQueue;
    }

    public BlockingQueue<MonitorID> getFinishQueue() {
        return finishQueue;
    }

    public void setFinishQueue(BlockingQueue<MonitorID> finishQueue) {
        this.finishQueue = finishQueue;
    }

    public String getProxyPath() {
        return proxyPath;
    }

    public void setProxyPath(String proxyPath) {
        this.proxyPath = proxyPath;
    }

    public List<String> getAmqpHosts() {
        return amqpHosts;
    }

    public void setAmqpHosts(List<String> amqpHosts) {
        this.amqpHosts = amqpHosts;
    }

    public boolean isStartRegister() {
        return startRegister;
    }

    public void setStartRegister(boolean startRegister) {
        this.startRegister = startRegister;
    }
}
