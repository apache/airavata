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

package org.apache.airavata.workflow.tracking.common;

import org.apache.axis2.addressing.EndpointReference;

public class NotifierConfig {
    private EndpointReference bokerEpr;
    private String publisherImpl;
    private boolean enableAsyncPublishing;

    public EndpointReference getBokerEpr() {
        return bokerEpr;
    }

    public void setBokerEpr(EndpointReference bokerEpr) {
        this.bokerEpr = bokerEpr;
    }

    public String getPublisherImpl() {
        return publisherImpl;
    }

    public void setPublisherImpl(String publisherImpl) {
        this.publisherImpl = publisherImpl;
    }

    public boolean isEnableAsyncPublishing() {
        return enableAsyncPublishing;
    }

    public void setEnableAsyncPublishing(boolean enableAsyncPublishing) {
        this.enableAsyncPublishing = enableAsyncPublishing;
    }
}
