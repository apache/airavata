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

import java.lang.reflect.Constructor;

import org.apache.airavata.workflow.tracking.common.ConstructorProps;
import org.apache.airavata.workflow.tracking.common.NotifierCreationException;
import org.apache.airavata.workflow.tracking.common.WorkflowTrackingContext;
import org.apache.airavata.workflow.tracking.impl.publish.NotificationPublisher;

public class PublisherFactory {
    protected static final Class[] PUBLISHER_CONSTRUCTOR_PARAM_TYPES = { ConstructorProps.class };

    protected static NotificationPublisher createSomePublisher(String publisherClassName,
            WorkflowTrackingContext context) {

        try {
            // Try to load the notifier's class.
            Class publisherClazz = Class.forName(publisherClassName);

            // Try to get the notifier's constructor.
            Constructor publisherConstructor = publisherClazz.getConstructor(PUBLISHER_CONSTRUCTOR_PARAM_TYPES);

            // Define the parameters for the notifier's constructor.
            Object[] constructorParameters = { context };

            // Create the notifier by calling its constructor.
            return (NotificationPublisher) publisherConstructor.newInstance(constructorParameters);

        } catch (Exception exception) {
            throw new NotifierCreationException(exception);
        }
    }
}
