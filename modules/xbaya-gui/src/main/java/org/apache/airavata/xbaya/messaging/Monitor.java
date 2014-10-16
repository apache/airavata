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

package org.apache.airavata.xbaya.messaging;

import org.apache.airavata.common.utils.ThriftUtils;
import org.apache.airavata.model.messaging.event.ExperimentStatusChangeEvent;
import org.apache.airavata.model.messaging.event.JobIdentifier;
import org.apache.airavata.model.messaging.event.JobStatusChangeEvent;
import org.apache.airavata.model.messaging.event.Message;
import org.apache.airavata.model.messaging.event.MessageType;
import org.apache.airavata.model.messaging.event.TaskIdentifier;
import org.apache.airavata.model.messaging.event.TaskStatusChangeEvent;
import org.apache.airavata.model.messaging.event.WorkflowIdentifier;
import org.apache.airavata.model.messaging.event.WorkflowNodeStatusChangeEvent;
import org.apache.airavata.model.workspace.experiment.ExperimentState;
import org.apache.airavata.workflow.model.exceptions.WorkflowException;
import org.apache.airavata.xbaya.messaging.event.Event;
import org.apache.airavata.xbaya.messaging.event.EventProducer;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
//import org.xmlpull.infoset.XmlElement;

public class Monitor extends EventProducer {

    protected static final Logger logger = LoggerFactory.getLogger(Monitor.class);

    protected static final String DEFAULT_MODEL_KEY = "_DEFAULT_MODEL_KEY";

    protected Map<String, EventDataRepository> eventDataMap = new HashMap<String, EventDataRepository>();

    protected MessageClient messageClient;

    protected boolean printRawMessages;

    protected long messagePullTimeout = 20000L;

    protected boolean monitoring = false;

    private boolean monitoringCompleted=false;

    private boolean monitoringFailed=false;

    private String lastTerminatedWorkflowExecutionId=null;

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
        this.messageClient = new MessageClient(this);
        setMonitoring(true);
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
					e.printStackTrace();
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

    /**
     * @param event
     */
    protected synchronized void handleNotification(Message event) {
        EventData eventData = null;
        boolean unsubscribeConsumer = false;
        try {
            eventData = new EventData(event);
        } catch (TException e) {
            logger.error("Error while adding new message event", e);
            System.out.println("Error while adding new message event");
            return;
        }
        Set<String> keys = this.eventDataMap.keySet();
        // Remove everthing leaving only the last one
        if(printRawMessages) {
            try {
                if (event.getMessageType() == MessageType.EXPERIMENT) {
                    ExperimentStatusChangeEvent experimentStatusChangeEvent = new ExperimentStatusChangeEvent();
                    ThriftUtils.createThriftFromBytes(event.getEvent(), experimentStatusChangeEvent);
                    logger.info("Received experiment event , expId : {} , status : {} ",
                            experimentStatusChangeEvent.getExperimentId(), experimentStatusChangeEvent.getState().toString());
                    System.out.println("Received experiment event");

                }   else if (event.getMessageType() == MessageType.WORKFLOWNODE) {
                    WorkflowNodeStatusChangeEvent wfnStatusChangeEvent = new WorkflowNodeStatusChangeEvent();
                    ThriftUtils.createThriftFromBytes(event.getEvent(), wfnStatusChangeEvent);
                    WorkflowIdentifier wfIdentifier = wfnStatusChangeEvent.getWorkflowNodeIdentity();
                    logger.info("Received workflow status change event, expId : {}, nodeId : {}, status : {} ",
                            new String[]{wfIdentifier.getExperimentId(), wfIdentifier.getWorkflowNodeId(),
                                    wfnStatusChangeEvent.getState().toString()});
                    System.out.println("Received a workflow change event");
                }else if (event.getMessageType() == MessageType.TASK) {
                    TaskStatusChangeEvent taskStatusChangeEvent = new TaskStatusChangeEvent();
                    ThriftUtils.createThriftFromBytes(event.getEvent(), taskStatusChangeEvent);
                    TaskIdentifier taskIdentifier = taskStatusChangeEvent.getTaskIdentity();
                    logger.info("Received task event , expId : {} ,taskId : {}, wfNodeId : {}, status : {} ",
                            new String[]{taskIdentifier.getExperimentId(), taskIdentifier.getTaskId(),
                                    taskIdentifier.getWorkflowNodeId(), taskStatusChangeEvent.getState().toString()});
                    System.out.printf("Received a task change event");
                } else if (event.getMessageType() == MessageType.JOB) {
                    JobStatusChangeEvent jobStatusChangeEvent = new JobStatusChangeEvent();
                    ThriftUtils.createThriftFromBytes(event.getEvent(), jobStatusChangeEvent);
                    JobIdentifier jobIdentifier = jobStatusChangeEvent.getJobIdentity();
                    logger.info("Received job event , expId : {}, taskId : {}, jobId : {}, wfNodeId : {}, status : {} ",
                            new String[]{jobIdentifier.getExperimentId(), jobIdentifier.getTaskId(), jobIdentifier.getJobId(),
                                    jobIdentifier.getWorkflowNodeId(), jobStatusChangeEvent.getState().toString()});
                    System.out.println("Received a job change event");
                } else {
                    logger.info("Received UNKNOWN event");
                    System.out.println("Received an UNKOWN event");
                }
            } catch (TException e) {
                logger.error("Error while printing thrift message ");
                System.out.println("Error while printing thrift message");
            }
        }
        for (String key : keys) {
            this.eventDataMap.get(key).addEvent(eventData);
        }
        if (eventData.getType() == MessageType.EXPERIMENT && eventData.getStatus().equals(ExperimentState.LAUNCHED.toString())) {
            unsubscribe(eventData.getExperimentId());
        }
    }

    /**
     * Subscribe to the WS Messenger client to pull notifications from the message box
     * @throws MonitorException
     */
    public void subscribe(String experimentID) throws MonitorException {
        messageClient.subscribe(experimentID);
    }

    /**
     * Unsubcribe from the ws messager client
     * @throws MonitorException
     */
    public void unsubscribe(String experimentId){
        // Enable/disable some menu items.
        sendSafeEvent(new Event(Event.Type.MONITOR_STOPED));
        messageClient.unsubscribe(experimentId);
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
				e.printStackTrace();
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