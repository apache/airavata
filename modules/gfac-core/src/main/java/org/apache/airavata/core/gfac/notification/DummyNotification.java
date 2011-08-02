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

package org.apache.airavata.core.gfac.notification;

import java.net.URI;

import org.apache.airavata.core.gfac.context.MessageContext;
import org.apache.airavata.core.gfac.exception.GfacException;

import edu.indiana.extreme.lead.workflow_tracking.common.DataDurationObj;
import edu.indiana.extreme.lead.workflow_tracking.common.DataObj;
import edu.indiana.extreme.lead.workflow_tracking.common.DurationObj;

public class DummyNotification implements NotificationService {

    public DataDurationObj dataReceiveFinished(DataDurationObj dataObj, String... descriptionAndAnnotation) {
        // TODO Auto-generated method stub
        return null;
    }

    public DataDurationObj dataReceiveStarted(URI dataID, URI remoteLocation, URI localLocation) {
        // TODO Auto-generated method stub
        return null;
    }

    public DataDurationObj dataSendFinished(DataDurationObj dataObj, String... descriptionAndAnnotation) {
        // TODO Auto-generated method stub
        return null;
    }

    public DataDurationObj dataSendStarted(DataObj dataObj, URI remoteLocation) {
        // TODO Auto-generated method stub
        return null;
    }

    public void dataConsumed(URI dataID, URI replica, String type, String soapElementName) {
        // TODO Auto-generated method stub

    }

    public void dataProduced(URI dataID, URI replica, String type, String soapElementName) {
        // TODO Auto-generated method stub

    }

    public DurationObj computationDuration(long durationMillis) {
        // TODO Auto-generated method stub
        return null;
    }

    public DurationObj computationFinished(DurationObj compObj) {
        // TODO Auto-generated method stub
        return null;
    }

    public DurationObj computationStarted() {
        // TODO Auto-generated method stub
        return null;
    }

    public void exception(String... descriptionAndAnnotation) {
        // TODO Auto-generated method stub

    }

    public void flush() {
        // TODO Auto-generated method stub

    }

    public void info(String... descriptionAndAnnotation) {
        // TODO Auto-generated method stub

    }

    public void sendingResponseFailed(Throwable trace, String... descriptionAndAnnotation) {
        // TODO Auto-generated method stub

    }

    public void sendingResponseSucceeded(String... descriptionAndAnnotation) {
        // TODO Auto-generated method stub

    }

    public void sendingResult(MessageContext messageContext) throws GfacException {
        // TODO Auto-generated method stub

    }

    public void warning(String... descriptionAndAnnotation) {
        // TODO Auto-generated method stub

    }

    public void sendingFault(String... descriptionAndAnnotation) {
        // TODO Auto-generated method stub

    }

    public void publishURL(String title, String url, String... descriptionAndAnnotation) {
        // TODO Auto-generated method stub

    }

    public void appAudit(String name, URI jobHandle, String host, String queueName, String jobId, String dName,
            String projectId, String rsl, String... descriptionAndAnnotation) {
        // TODO Auto-generated method stub

    }

    public void sendResourceMappingNotifications(String hostName, String... descriptionAndAnnotation) {
        // TODO Auto-generated method stub

    }

}
