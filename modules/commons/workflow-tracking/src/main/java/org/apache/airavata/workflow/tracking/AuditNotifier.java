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

package org.apache.airavata.workflow.tracking;

import java.net.URI;

import org.apache.airavata.workflow.tracking.common.WorkflowTrackingContext;

/**
 * Utility to create and send notifications related to accounting and auditing. Enough information is sent in these
 * notifications for accounting information to be looked up and applied at a later time.
 * 
 * @version $Revision: 1.3 $
 * @author
 */
public interface AuditNotifier {

    /**
     * Send an applicationAudit message. This should be sent shortly after submitting a GRAM job to a batch scheduler
     * with an allocation policy. Arguments listed below as <i>not required by schema</i> may be passed in as <b>
     * <code>null</code></b>.
     * 
     * @param entity
     *            identity of the workflow/service's invocation that initiated this job
     * @param name
     *            human readable name of application
     * @param jobHandle
     *            a URI handle to the GRAM job
     * @param host
     *            hostname of the resource job is submitted to
     * @param queueName
     *            name of the queue submitted to (not required by schema)
     * @param jobId
     *            id of the job in the remote batch system (not required by schema)
     * @param dName
     *            distinguished name of the identity under which job is submitted
     * @param projectId
     *            project id that this job should be charged to (not required by schema)
     * @param rsl
     *            RSL string used when submitting this job
     * @param descriptionAndAnnotation
     *            optional vararg. The first element is used as the human readable description for this notification.
     *            The subsequent strings need to be serialized XML fragments that are added as annotation to the
     *            notification.
     * 
     */
    void appAudit(WorkflowTrackingContext context, String name, URI jobHandle, String host, String queueName,
            String jobId, String dName, String projectId, String rsl, String... descriptionAndAnnotation);
}
