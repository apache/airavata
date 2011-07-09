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

public interface NotificationService {

    DataDurationObj dataReceiveFinished(DataDurationObj dataObj, String... descriptionAndAnnotation);

    DataDurationObj dataReceiveStarted(URI dataID, URI remoteLocation, URI localLocation);

    DataDurationObj dataSendFinished(DataDurationObj dataObj, String... descriptionAndAnnotation);

    DataDurationObj dataSendStarted(DataObj dataObj, URI remoteLocation);

    void dataConsumed(URI dataID, URI replica, String type, String soapElementName);

    void dataProduced(URI dataID, URI replica, String type, String soapElementName);

    DurationObj computationDuration(long durationMillis);

    DurationObj computationFinished(DurationObj compObj);

    DurationObj computationStarted();

    void exception(String... descriptionAndAnnotation);

    void flush();

    void info(String... descriptionAndAnnotation);

    void sendingResponseFailed(Throwable trace, String... descriptionAndAnnotation);

    void sendingResponseSucceeded(String... descriptionAndAnnotation);

    void sendingResult(MessageContext messageContext) throws GfacException;

    void warning(String... descriptionAndAnnotation);

    void sendingFault(String... descriptionAndAnnotation);

    void publishURL(String title, String url, String... descriptionAndAnnotation);

    void appAudit(String name, URI jobHandle, String host, String queueName, String jobId, String dName,
            String projectId, String rsl, String... descriptionAndAnnotation);

    void sendResourceMappingNotifications(String hostName, String... descriptionAndAnnotation);

}
