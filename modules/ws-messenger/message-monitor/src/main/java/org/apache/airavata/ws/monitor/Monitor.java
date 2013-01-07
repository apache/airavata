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

package org.apache.airavata.ws.monitor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.workflow.model.exceptions.WorkflowException;
import org.apache.airavata.ws.monitor.event.Event;
import org.apache.airavata.ws.monitor.event.EventProducer;
import org.apache.airavata.ws.monitor.event.Event.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.infoset.XmlElement;

public class Monitor extends EventProducer {

    protected static final Logger logger = LoggerFactory.getLogger(Monitor.class);

    protected MonitorConfiguration configuration;

    protected static final String DEFAULT_MODEL_KEY = "_DEFAULT_MODEL_KEY";

    protected Map<String, EventDataRepository> eventDataMap = new HashMap<String, EventDataRepository>();

    protected WsmgClient wsmgClient;

    protected boolean printRawMessages;

    protected long messagePullTimeout = 20000L;

    protected boolean monitoring = false;
    
    private boolean monitoringCompleted=false;
    
    private boolean monitoringFailed=false;
    
    public Monitor(MonitorConfiguration configuration) {
        this.configuration = configuration;
        // First one keeps all event data & it doesn't have filters
        this.eventDataMap.put(DEFAULT_MODEL_KEY, new EventDataRepository());
    }

    /**
     * @return The configuration for monitoring
     */
    public MonitorConfiguration getConfiguration() {
        return this.configuration;
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

        subscribe();

        if (null != this.configuration.getInteractiveNodeIDs()) {
            List<String> interactiveNodeIDs = this.configuration.getInteractiveNodeIDs();
            // now add models for this as well
            for (String string : interactiveNodeIDs) {

                final String nodeID = string;
                // for each wsnode there is one data model which
                this.eventDataMap.put(nodeID, new EventDataRepository(new EventFilter() {
                    public boolean isAcceptable(EventData event) {
                        return event != null && event.getNodeID() != null && event.getNodeID().equals(nodeID);
                    }
                }));
            }

        }
        getEventDataRepository().triggerListenerForPostMonitorStart();
    }

    /**
     * Stops monitoring without using a thread.
     * 
     * @throws MonitorException
     */
    public synchronized void stop() throws MonitorException {
        try {
			if (this.wsmgClient != null) {
				getEventDataRepository().triggerListenerForPreMonitorStop();
			    unsubscribe(this.wsmgClient);
			    this.wsmgClient = null;
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
    protected synchronized void handleNotification(XmlElement event) {
        Set<String> keys = this.eventDataMap.keySet();
        // Remove everthing leaving only the last one
        if(printRawMessages){
            System.out.println(XMLUtil.xmlElementToString(event));
        }
        for (String key : keys) {
            this.eventDataMap.get(key).addEvent(event);
        }
    }

    /**
     * Subscribe to the WS Messenger client to pull notifications from the message box
     * @throws MonitorException
     */
    private void subscribe() throws MonitorException {
        this.wsmgClient = new WsmgClient(this);
        //Users can set the timeout and interval for the subscription using wsmg setter methods, here we use the default values
        this.wsmgClient.setTimeout(this.getMessagePullTimeout());
        this.wsmgClient.subscribe();
        setMonitoring(true);

        // Enable/disable some menu items and show the monitor panel.
        sendSafeEvent(new Event(Type.MONITOR_STARTED));
    }

    /**
     * Unsubcribe from the ws messager client
     * @param client
     * @throws MonitorException
     */
    private void unsubscribe(WsmgClient client) throws MonitorException {
        // Enable/disable some menu items.
        sendSafeEvent(new Event(Type.MONITOR_STOPED));
        client.unsubscribe();
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

    private void setMonitoring(boolean monitoring) {
        this.monitoring = monitoring;
    }
    
    /**
     * Return the id of the experiment which the monitoring is done 
     * @return
     */
    public String getExperimentId(){
    	return getConfiguration().getTopic();
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
}