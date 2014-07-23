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
package org.apache.airavata.gfac.monitor.core;

import org.apache.airavata.common.utils.MonitorPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the abstract Monitor which needs to be used by
 * any Monitoring implementation which expect nto consume
 * to store the status to registry. Because they have to
 * use the MonitorPublisher to publish the monitoring statuses
 * to the Event Bus. All the Monitor statuses publish to the eventbus
 * will be saved to the Registry.
 */
public abstract class AiravataAbstractMonitor implements Monitor {
    private final static Logger logger = LoggerFactory.getLogger(AiravataAbstractMonitor.class);
    protected MonitorPublisher publisher;

    public MonitorPublisher getPublisher() {
        return publisher;
    }

    public void setPublisher(MonitorPublisher publisher) {
        this.publisher = publisher;
    }
}
