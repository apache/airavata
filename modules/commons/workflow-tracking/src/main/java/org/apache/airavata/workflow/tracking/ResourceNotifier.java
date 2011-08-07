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

import org.apache.airavata.workflow.tracking.common.WorkflowTrackingContext;

/**
 * Utility to create and send notifications related to resource broker and job status.r
 */
public interface ResourceNotifier {

    /**
     * Method resourceMapping
     * 
     * @param entity
     *            an InvocationEntity
     * @param mappedResource
     *            a String
     * @param retryStatusCount
     *            an int
     * @param descriptionAndAnnotationa
     *            String
     * 
     */
    void resourceMapping(WorkflowTrackingContext context, String mappedResource, int retryStatusCount,
            String... descriptionAndAnnotation);

    /**
     * Method jobStatus
     * 
     * @param entity
     *            an InvocationEntity
     * @param status
     *            a String
     * @param retryCount
     *            an int
     * @param descriptionAndAnnotationa
     *            String
     * 
     */
    void jobStatus(WorkflowTrackingContext context, String status, int retryCount, String... descriptionAndAnnotation);
}
