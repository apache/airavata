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

package org.apache.airavata.workflow.tracking.impl.state;

import java.net.URI;

import org.apache.airavata.workflow.tracking.common.InvocationEntity;
import org.apache.airavata.workflow.tracking.types.BaseIDType;

public class InvocationEntityImpl implements InvocationEntity {

    protected URI workflowID;
    protected URI serviceID;
    protected String workflowNodeID;
    protected Integer workflowTimestep;

    /**
     * Constructor used when only service ID is available (i.e. entity not in the context of an invocation)
     * 
     * @param serviceID_
     *            an URI
     * 
     */
    public InvocationEntityImpl(URI serviceID_) {

        if (serviceID_ == null)
            throw new RuntimeException("ServiceID passed was null!");

        serviceID = serviceID_;

        workflowID = null;
        workflowNodeID = null;
        workflowTimestep = null;
    }

    /**
     * Constructor used when all IDs are potentially available (i.e. entity in the context of an invocation)
     * 
     * @param workflowID_
     *            an URI
     * @param serviceID_
     *            an URI
     * @param workflowNodeID_
     *            a String
     * @param workflowTimestep_
     *            an int
     * 
     */
    public InvocationEntityImpl(URI workflowID_, URI serviceID_, String workflowNodeID_, Integer workflowTimestep_) {

        if (serviceID_ == null)
            throw new RuntimeException("ServiceID passed was null!");

        workflowID = workflowID_;
        serviceID = serviceID_;
        workflowNodeID = workflowNodeID_;
        workflowTimestep = workflowTimestep_;
    }

    /**
     * Copy Constructor
     * 
     * @param source
     *            an InvocationEntity
     * 
     */
    protected InvocationEntityImpl(InvocationEntity source) {
        this(source.getWorkflowID(), source.getServiceID(), source.getWorkflowNodeID(), source.getWorkflowTimestep());
    }

    public String getWorkflowNodeID() {

        return workflowNodeID;
    }

    public URI getServiceID() {

        return serviceID;
    }

    public Integer getWorkflowTimestep() {

        return workflowTimestep;
    }

    public URI getWorkflowID() {

        return workflowID;
    }

    public BaseIDType toBaseIDType() {

        BaseIDType baseID = BaseIDType.Factory.newInstance();
        if (serviceID != null)
            baseID.setServiceID(serviceID.toString());
        if (workflowID != null)
            baseID.setWorkflowID(workflowID.toString());
        if (workflowTimestep != null)
            baseID.setWorkflowTimestep(workflowTimestep);
        if (workflowNodeID != null)
            baseID.setWorkflowNodeID(workflowNodeID);

        return baseID;
    }

}
