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

    protected boolean print;

    protected long timeout = 20000L;

    protected boolean monitoring = false;
    
    private boolean monitoringCompleted=false;
    
    private boolean monitoringFailed=false;
    
    /**
     * Constructs a Monitor.
     * 
     * @param configuration
     */
    public Monitor(MonitorConfiguration configuration) {
        this.configuration = configuration;
        // The first one is special and it is for the main event panel display
        // and
        // it does not have and filters
        this.eventDataMap.put(DEFAULT_MODEL_KEY, new EventDataRepository());


    }

    /**
     * @return The configuration
     */
    public MonitorConfiguration getConfiguration() {
        return this.configuration;
    }

    /**
     * Return the event data repository containing all the notifications
     * @return 
     */
    public EventDataRepository getEventDataRepository() {
        // send the first one cos that is the default one
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
        // Stop the previous monitoring if any.
    	monitoringCompleted=false;
    	monitoringFailed=false;
    			
    	getEventDataRepository().triggerListenerForPreMonitorStart();
        asynchronousStop();

        subscribe();

        if (null != this.configuration.getInteractiveNodeIDs()) {
            List<String> interactiveNodeIDs = this.configuration.getInteractiveNodeIDs();
            // now add models for this as well
            for (String string : interactiveNodeIDs) {

                final String nodeID = string;
                // for each wsnode there is one data model which
                this.eventDataMap.put(nodeID, new EventDataRepository(new EventFilter() {
                    /**
                     * @see org.apache.airavata.ws.monitor.EventFilter#isAcceptable(org.apache.airavata.ws.monitor.EventData)
                     */
                    public boolean isAcceptable(EventData event) {
                        return event != null && event.getNodeID() != null && event.getNodeID().equals(nodeID);
                    }
                }));
            }

        }
        getEventDataRepository().triggerListenerForPostMonitorStart();
    }
    
    public void startMonitoring(){
    	final Monitor m=this;
    	new Thread(){
    		@Override
    		public void run() {
    			try {
					m.start();
				} catch (MonitorException e) {
					e.printStackTrace();
				}
    		}
    	}.start();
    }
    
    public void stopMonitoring(){
		asynchronousStop();
    }

    /**
     * Stops monitoring.
     */
    protected void asynchronousStop() {
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
     * Resets the graph and clear the monitoring table. Remove all the extra tablemodels available
     */
    public void reset() {
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
    public synchronized void handleNotification(XmlElement event) {
        Set<String> keys = this.eventDataMap.keySet();
        // Remove everthing leaving only the last one
        if(print){
            System.out.println(XMLUtil.xmlElementToString(event));
        }
        for (String key : keys) {
            this.eventDataMap.get(key).addEvent(event);
        }
    }

    private void subscribe() throws MonitorException {
        this.wsmgClient = new WsmgClient(this);
        this.wsmgClient.setTimeout(this.getMessagePullTimeout());
        //Users can set the timeout and interval for the subscription using wsmg setter methods, here we use the default values
        this.wsmgClient.subscribe();
        setMonitoring(true);

        // Enable/disable some menu items and show the monitor panel.
        sendSafeEvent(new Event(Type.MONITOR_STARTED));
    }

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
    	this.print = print;
    }
    
    /**
     * Retrieve the timeout in milliseconds for pulling messages
     * @return
     */
    public long getMessagePullTimeout() {
        return timeout;
    }

    /**
     * Set the timeout in milliseconds for pulling messages
     * @param timeout
     */
    public void setMessagePullTimeout(long timeout) {
        this.timeout = timeout;
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
        this.print = print;
    }
}