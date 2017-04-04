/**
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
 */
package org.apache.airavata.xbaya.messaging;

import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.messaging.core.Consumer;
import org.apache.airavata.messaging.core.MessageContext;
import org.apache.airavata.messaging.core.MessageHandler;
import org.apache.airavata.messaging.core.MessagingConstants;
import org.apache.airavata.messaging.core.impl.RabbitMQStatusConsumer;
import org.apache.airavata.model.messaging.event.MessageType;
import org.apache.airavata.model.experiment.ExperimentState;
import org.apache.airavata.workflow.model.exceptions.WorkflowException;
import org.apache.airavata.xbaya.messaging.event.Event;
import org.apache.airavata.xbaya.messaging.event.EventProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
//import org.xmlpull.infoset.XmlElement;

public class Monitor extends EventProducer {

    protected static final Logger logger = LoggerFactory.getLogger(Monitor.class);

    protected static final String DEFAULT_MODEL_KEY = "_DEFAULT_MODEL_KEY";

    protected Map<String, EventDataRepository> eventDataMap = new HashMap<String, EventDataRepository>();

    protected Consumer messageClient;

    protected boolean printRawMessages;

    protected long messagePullTimeout = 20000L;

    protected boolean monitoring = false;

    private boolean monitoringCompleted=false;

    private boolean monitoringFailed=false;

    private String lastTerminatedWorkflowExecutionId=null;

    private Map<String, String> expIdToSubscribers = new HashMap<String, String>();

    public Monitor() {
        // First one keeps all event data & it doesn't have filters
        this.eventDataMap.put(DEFAULT_MODEL_KEY, new EventDataRepository());
    }

    /**
     * Return the event data repository containing all the notifications
     * @return
     */
    public EventDataRepository getEventDataRepository() {
        return this.eventDataMap.get(DEFAULT_MODEL_KEY);
    }

    /**
     * Return the event data repository containing all the notifications for the node
     * @param nodeID
     * @return
     */
    public EventDataRepository getEventDataRepository(String nodeID){
        return this.eventDataMap.get(nodeID);
    }

    /**
     * @throws MonitorException
     */
    public synchronized void start() throws MonitorException {

    	//Make sure currently we are not doing any monitoring
        stop();

        // Reset monitoring variables
    	monitoringCompleted=false;
    	monitoringFailed=false;

    	//Notify listeners that the monitoring is about to start
    	getEventDataRepository().triggerListenerForPreMonitorStart();
        try {
//            AiravataUtils.setExecutionAsServer();
            this.messageClient = new RabbitMQStatusConsumer("amqp://localhost:5672", "airavata_rabbitmq_exchange");
        } catch (AiravataException e) {
            String msg = "Failed to start the consumer";
            logger.error(msg, e);
            throw new MonitorException(msg, e);
        }
        // Enable/disable some menu items and show the monitor panel.
        sendSafeEvent(new Event(Event.Type.MONITOR_STARTED));
        getEventDataRepository().triggerListenerForPostMonitorStart();
    }

    /**
     * Stops monitoring without using a thread.
     *
     * @throws MonitorException
     */
    public synchronized void stop() throws MonitorException {
        try {
			if (this.messageClient != null) {
				getEventDataRepository().triggerListenerForPreMonitorStop();
//			    unsubscribe(this.messageClient);
			    this.messageClient = null;
			    getEventDataRepository().triggerListenerForPostMonitorStop();
			}
		} finally{
	        monitoringCompleted=true;
		}
    }

    /**
     * Start monitoring asynchronously
     */
    public void startMonitoring(){
    	new Thread(){
    		@Override
    		public void run() {
    			try {
    				Monitor.this.start();
				} catch (MonitorException e) {
                    logger.error(e.getMessage(), e);
				}
    		}
    	}.start();
    }

    /**
     * Stop monitoring asynchronously
     */
    public void stopMonitoring(){
    	// Users don't need to know the end of unsubscription.
        new Thread() {
            @Override
            public void run() {
                try {
                	Monitor.this.stop();
                } catch (WorkflowException e) {
                    // Ignore the error in unsubscription.
                    logger.error(e.getMessage(), e);
                }
            }
        }.start();
    }

    /**
     * Resets the graph and clear the monitoring table. Remove all the extra table models available
     */
    public void resetEventData() {
        Set<String> keys = this.eventDataMap.keySet();
        LinkedList<String> keysToBeRemoved = new LinkedList<String>();
        // Remove everthing leaving only the last one
        for (String key : keys) {
            EventDataRepository monitorEventData = this.eventDataMap.get(key);
            monitorEventData.removeAllEvents();
            if (!key.equals(DEFAULT_MODEL_KEY)) {
                keysToBeRemoved.add(key);
            }
        }
        for (String key : keysToBeRemoved) {
            this.eventDataMap.remove(key);
        }
    }

    private class NotificationMessageHandler implements MessageHandler {
        private String experimentId;

        private NotificationMessageHandler(String experimentId) {
            this.experimentId = experimentId;
        }

        public Map<String, Object> getProperties() {
            Map<String, Object> props = new HashMap<String, Object>();
            List<String> routingKeys = new ArrayList<String>();
            routingKeys.add(experimentId);
            routingKeys.add(experimentId + ".*");
            props.put(MessagingConstants.RABBIT_ROUTING_KEY, routingKeys);
            return props;
        }

        public void onMessage(MessageContext message) {
            EventData eventData = null;
            boolean unsubscribeConsumer = false;
            eventData = new EventData(message);
            Set<String> keys = eventDataMap.keySet();
            for (String key : keys) {
                eventDataMap.get(key).addEvent(eventData);
            }
            if (eventData.getType() == MessageType.EXPERIMENT && eventData.getStatus().equals(ExperimentState.COMPLETED.toString())) {
                unsubscribe(eventData.getExperimentId());
            }
        }
    }

    /**
     * Subscribe to the WS Messenger client to pull notifications from the message box
     * @throws MonitorException
     */
    public void subscribe(String experimentID) throws MonitorException {
        try {
            setMonitoring(true);
            String id = messageClient.listen(new NotificationMessageHandler(experimentID));
            expIdToSubscribers.put(experimentID, id);
        } catch (AiravataException e) {
            String msg = "Failed to listen to experiment: " + experimentID;
            logger.error(msg);
            setMonitoring(false);
            throw new MonitorException(msg, e);
        }
    }

    /**
     * Unsubcribe from the ws messager client
     * @throws MonitorException
     */
    public void unsubscribe(String experimentId){
        // Enable/disable some menu items.
        sendSafeEvent(new Event(Event.Type.MONITOR_STOPED));
        String id = expIdToSubscribers.remove(experimentId);
        if (id != null) {
            try {
                messageClient.stopListen(id);
            } catch (AiravataException e) {
                logger.warn("Failed to find the subscriber for experiment id: " + id, e);
            }
        }
        setMonitoring(false);
    }

    /**
     * Wait until the monitoring is completed
     */
    public void waitForCompletion(){
    	while(!monitoringCompleted && !monitoringFailed){
    		try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
			}
    	}
    }

    /**
     * Print the raw notification messages to the console as they arrive
     * @param print - if <code>true</code> raw notifications are printed
     */
    public void printRawMessage(boolean print){
    	this.printRawMessages = print;
    }

    /**
     * Retrieve the timeout in milliseconds for pulling messages
     * @return
     */
    public long getMessagePullTimeout() {
        return messagePullTimeout;
    }

    /**
     * Set the timeout in milliseconds for pulling messages
     * @param timeout
     */
    public void setMessagePullTimeout(long timeout) {
        this.messagePullTimeout = timeout;
    }

    /**
     * is the monitoring active
     * @return
     */
    public boolean isMonitoring() {
		return monitoring;
    }

//	public boolean hasCurrentExecutionTerminatedNotificationReceived() {
//		return getConfiguration().getTopic()!=null && getConfiguration().getTopic().equals(lastTerminatedWorkflowExecutionId);
//	}

    private void setMonitoring(boolean monitoring) {
        this.monitoring = monitoring;
    }

    /**
     * @deprecated - Use <code>getEventDataRepository()</code> instead
     * @return
     */
    public EventDataRepository getEventData(){
    	return getEventDataRepository();
    }

    /**
     * @deprecated - Use <code>getEventDataRepository(...)</code> instead
     * @param nodeID
     * @return
     */
    public EventDataRepository getEventData(String nodeID) {
        return getEventDataRepository(nodeID);
    }

    /**
     * @deprecated - Use <code>printRawMessage(...)</code> instead
     * @param print - if <code>true</code> raw notifications are printed
     */
    public void setPrint(boolean print) {
        this.printRawMessages = print;
    }

    public void fireStartMonitoring(String workflowName) {
        for (EventDataRepository eventDataRepository : eventDataMap.values()) {
            eventDataRepository.fireNewWorkflowStart(workflowName);
        }
    }
}