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
package org.apache.airavata.job.monitor.impl.push.amqp;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.apache.airavata.job.monitor.MonitorID;
import org.apache.airavata.job.monitor.core.PushMonitor;
import org.apache.airavata.job.monitor.event.MonitorPublisher;
import org.apache.airavata.job.monitor.exception.AiravataMonitorException;
import org.apache.airavata.job.monitor.util.AMQPConnectionUtil;
import org.apache.airavata.job.monitor.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This is the implementation for AMQP based monitor, this uses
 * rabbitmq client to recieve AMQP based monitoring data from
 * mostly excede resources.
 */
public class AMQPMonitor extends PushMonitor implements Runnable {
    private final static Logger logger = LoggerFactory.getLogger(AMQPMonitor.class);


    /* this will keep all the channels available in the system, we do not create
      channels for all the jobs submitted, but we create channels for each user for each
      host.
    */
    private Map<String, Channel> availableChannels;

    private MonitorPublisher publisher;

    private BlockingQueue<MonitorID> runningQueue;

    private BlockingQueue<MonitorID> finishQueue;

    public AMQPMonitor(MonitorPublisher publisher, BlockingQueue runningQueue,BlockingQueue finishQueue) {
        this.publisher = publisher;
        this.runningQueue = runningQueue;
        availableChannels = new HashMap<String, Channel>();
        this.finishQueue = finishQueue;

    }

    public void run() {
        try {
            // before going to the while true mode we start unregister thread
            UnRegisterThread unRegisterThread = new UnRegisterThread(this);
            unRegisterThread.run();

            while (true) {
                runningQueue.wait();
                // we got a new job to do the monitoring
                MonitorID take = runningQueue.take();
                this.registerListener(take);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (AiravataMonitorException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean registerListener(MonitorID monitorID) throws AiravataMonitorException {
        // do initial check before creating a channel, otherwise resources will be waste
        // and channel id will be malformed
        // this check is not implemented in MonitorManager because it depends on
        // the Monitoring implementation (what data is required)
        checkMonitorID(monitorID);
        String channelID = CommonUtils.getChannelID(monitorID);

        // if we already have a channel we do not create one
        if (availableChannels.get(channelID) == null) {
            //todo need to fix this rather getting it from a file
            Connection connection = AMQPConnectionUtil.connect("xsede_private", "/Users/lahirugunathilake/Downloads/x509up_u503876");
            Channel channel = null;
            try {
                channel = connection.createChannel();
                String queueName = channel.queueDeclare().getQueue();

                BasicConsumer consumer = new BasicConsumer(new JSONMessageParser(), publisher, monitorID);
                channel.basicConsume(queueName, true, consumer);
                String filterString = CommonUtils.getRoutingKey(monitorID);
                // here we queuebind to a particular user in a particular machine
                channel.queueBind(queueName, "glue2.computing_activity", filterString);
            } catch (IOException e) {
                logger.error("Error creating the connection to monitor the job:" + monitorID.getJobID());
            }
        }
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    private void checkMonitorID(MonitorID monitorID) throws AiravataMonitorException {
        if (monitorID.getUserName() == null) {
            String error = "Username has to be given for monitoring";
            logger.error(error);
            throw new AiravataMonitorException(error);
        } else if (monitorID.getHost() == null) {
            String error = "Host has to be given for monitoring";
            logger.error(error);
            throw new AiravataMonitorException(error);
        } else if (monitorID.getJobID() == null) {
            String error = "JobID has to be given for monitoring";
            logger.error(error);
            throw new AiravataMonitorException(error);
        }
    }


    @Override
    public boolean unRegisterListener(MonitorID monitorID) throws AiravataMonitorException {
        String channelID = CommonUtils.getChannelID(monitorID);
        Channel channel = availableChannels.get(channelID);
        if (channel == null) {
            logger.error("Already Unregistered the listener");
            throw new AiravataMonitorException("Already Unregistered the listener");
        } else {
            try {
                channel.queueUnbind(channel.queueDeclare().getQueue(), "glue2.computing_activity", CommonUtils.getRoutingKey(monitorID));
            } catch (IOException e) {
                logger.error("Error unregistering the listener");
                throw new AiravataMonitorException("Error unregistering the listener");
            }
        }
        return true;
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

    /**
     * implementing a logic to handle the finished job and unsubscribe
     */

    class UnRegisterThread extends Thread {
        private AMQPMonitor monitor;

        public UnRegisterThread(AMQPMonitor monitor){
            this.monitor = monitor;
        }
        public synchronized void run() {
            while(true){
                try {
                    this.monitor.getFinishQueue().wait();
                    if(this.monitor.getFinishQueue().isEmpty()){
                        logger.error("Error signal came to the UnRegistering thread, so skipping");
                    }else{
                        Iterator<MonitorID> iterator = this.monitor.getFinishQueue().iterator();
                        /* takes all the finished jobs at a time and unregister them */
                        while(iterator.hasNext()){
                            MonitorID next = iterator.next();
                            monitor.unRegisterListener(next);
                        }
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (AiravataMonitorException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }
    }
}
