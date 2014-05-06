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

package org.apache.airavata.gfac.core.notification.listeners;

import com.google.common.eventbus.Subscribe;
import org.apache.airavata.gfac.core.notification.events.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingListener {
    private static Logger log = LoggerFactory.getLogger("gfac-logginglistener");

    @Subscribe
    public void logGFacEvent(GFacEvent e){
        log.info("GFac event of type " + e.getEventType() + " received.");
    }

    @Subscribe
    public void logExecutionFail(ExecutionFailEvent e){
        log.error("Execution failed." + e.getEventType());
    }

    @Subscribe
    public void logFinishExecutionEvent(FinishExecutionEvent event){
        log.info("Execution has Finished ...");
    }

    @Subscribe
    public void logStartExecutionEvent(StartExecutionEvent event){
        log.info("Execution has started ...");
    }

    @Subscribe
    public void logStatusChangeEvent(StatusChangeEvent event){
        log.info("Job status has changed ...");
        log.info(event.getStatusMessage());
    }
}
