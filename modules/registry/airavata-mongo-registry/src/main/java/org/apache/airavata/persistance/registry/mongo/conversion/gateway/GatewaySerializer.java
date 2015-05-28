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
package org.apache.airavata.persistance.registry.mongo.conversion.gateway;

import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.persistance.registry.mongo.conversion.AbstractThriftSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GatewaySerializer extends
        AbstractThriftSerializer<Gateway._Fields, Gateway> {
    private final static Logger logger = LoggerFactory.getLogger(GatewaySerializer.class);

    @Override
    protected Gateway._Fields[] getFieldValues() {
        return Gateway._Fields.values();
    }

    @Override
    protected Class<Gateway> getThriftClass() {
        return null;
    }
}