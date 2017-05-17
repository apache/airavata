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

public interface EventDataListener {
	
	/**
	 * Gets triggered when a new message is received relevant for the experiment subscribed to
	 * @param eventDataRepo - Contains a collection of events up to this point in monitoring
	 * @param eventData - the new message related to the experiment
	 */
	public void notify(EventDataRepository eventDataRepo, EventData eventData);

	/**
	 * Set the Monitor object
	 * @param monitor
	 */
	public void setExperimentMonitor(Monitor monitor);

	/**
	 * Gets triggered just before the experiment monitoring is started
	 */
	public void monitoringPreStart();

	/**
	 * Gets triggered just after the experiment monitoring is started
	 */
	public void monitoringPostStart();

	/**
	 * Gets triggered just before the experiment monitoring is stopped
	 */
	public void monitoringPreStop();

	/**
	 * Gets triggered just after the experiment monitoring is stopped
	 */
	public void monitoringPostStop();

	/**
	 * Gets triggered when experiment fails
	 */
	public void onFail(EventData failNotification);
	
	/**
	 * Gets triggered when the experiment completes
	 */
	public void onCompletion(EventData completionNotification);
}
