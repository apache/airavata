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
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.job.monitor.MonitorID;
import org.apache.airavata.job.monitor.exception.AiravataMonitorException;
import org.apache.airavata.job.monitor.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class UnRegisterThread extends Thread {
    private final static Logger logger = LoggerFactory.getLogger(UnRegisterThread.class);
    private BlockingQueue<MonitorID> finishQueue;
    private Map<String, Channel> availableChannels;

    public UnRegisterThread(BlockingQueue<MonitorID> monitor, Map<String, Channel> channels) {
        this.finishQueue = monitor;
        this.availableChannels = channels;
    }

    public void run() {
        while (!ServerSettings.isStopAllThreads()) {
            try {
                MonitorID monitorID = this.finishQueue.take();
                unRegisterListener(monitorID);
            //
            } catch (AiravataMonitorException e) {
                logger.error(e.getLocalizedMessage());
            } catch (InterruptedException e) {
                logger.error(e.getLocalizedMessage());
            }
        }
    }

    private boolean unRegisterListener(MonitorID monitorID) throws AiravataMonitorException {
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
}

