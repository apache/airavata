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
package org.apache.airavata.persistance.registry.jpa.mongo.conversion.experiment.appstatus;

import org.apache.airavata.model.workspace.experiment.ApplicationStatus;
import org.apache.airavata.persistance.registry.jpa.mongo.conversion.AbstractThriftSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationStatusSerializer extends
        AbstractThriftSerializer<ApplicationStatus._Fields, ApplicationStatus> {
    private final static Logger logger = LoggerFactory.getLogger(
            ApplicationStatusSerializer.class);

    @Override
    protected ApplicationStatus._Fields[] getFieldValues() {
        return ApplicationStatus._Fields.values();
    }

    @Override
    protected Class<ApplicationStatus> getThriftClass() {
        return null;
    }
}