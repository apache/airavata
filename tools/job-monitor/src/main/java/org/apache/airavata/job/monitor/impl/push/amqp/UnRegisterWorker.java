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

import com.google.common.eventbus.Subscribe;
import com.rabbitmq.client.Channel;
import org.apache.airavata.job.monitor.MonitorID;
import org.apache.airavata.job.monitor.exception.AiravataMonitorException;
import org.apache.airavata.job.monitor.state.JobStatus;
import org.apache.airavata.job.monitor.util.CommonUtils;
import org.apache.airavata.model.workspace.experiment.JobState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public class UnRegisterWorker{
    private final static Logger logger = LoggerFactory.getLogger(UnRegisterWorker.class);
    private Map<String, Channel> availableChannels;

    public UnRegisterWorker(Map<String, Channel> channels) {
        this.availableChannels = channels;
    }

    @Subscribe
    private boolean unRegisterListener(JobStatus jobStatus) throws AiravataMonitorException {
        MonitorID monitorID = jobStatus.getMonitorID();
        String channelID = CommonUtils.getChannelID(monitorID);
        if (JobState.FAILED.equals(jobStatus.getState()) || JobState.COMPLETE.equals(jobStatus.getState())){
            Channel channel = availableChannels.get(channelID);
            if (channel == null) {
                logger.error("Already Unregistered the listener");
                throw new AiravataMonitorException("Already Unregistered the listener");
            } else {
                try {
                    channel.queueUnbind(channel.queueDeclare().getQueue(), "glue2.computing_activity", CommonUtils.getRoutingKey(monitorID));
                    channel.close();
                    channel.getConnection().close();
                    availableChannels.remove(channelID);
                } catch (IOException e) {
                    logger.error("Error unregistering the listener");
                    throw new AiravataMonitorException("Error unregistering the listener");
                }
            }
        }
        return true;
    }
}

