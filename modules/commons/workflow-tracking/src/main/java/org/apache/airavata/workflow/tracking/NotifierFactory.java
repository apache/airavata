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

import org.apache.airavata.workflow.tracking.impl.GenericNotifierImpl;
import org.apache.airavata.workflow.tracking.impl.NotifierImpl;
import org.apache.airavata.workflow.tracking.impl.ProvenanceNotifierImpl;
import org.apache.log4j.Logger;

/**
 * Create a Notifier instance.
 * 
 * This code was inspired by the listener agent factory in xmessages
 */
public class NotifierFactory {
    private final static org.apache.log4j.Logger logger = Logger.getLogger(NotifierFactory.class);

    public static Notifier createNotifier() {
        return new NotifierImpl();
    }

    public static GenericNotifier createGenericNotifier() {
        return new GenericNotifierImpl();
    }

    public static WorkflowNotifier createWorkflowNotifier() {
        return new ProvenanceNotifierImpl();
    }

    public static ServiceNotifier createServiceNotifier() {
        return new ProvenanceNotifierImpl();
    }

    public static ProvenanceNotifier createProvenanceNotifier() {
        return new ProvenanceNotifierImpl();
    }

    public static PerformanceNotifier createPerformanceNotifier() {
        return new NotifierImpl();
    }

    public static AuditNotifier createAuditNotifier() {
        return new NotifierImpl();
    }

}
