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
package org.apache.airavata.registry.core.repositories.workflowcatalog;

import org.apache.airavata.model.NodeModel;
import org.apache.airavata.registry.core.entities.workflowcatalog.NodeEntity;
import org.apache.airavata.registry.core.entities.workflowcatalog.NodePK;
import org.apache.airavata.registry.core.repositories.AbstractRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeRepository extends AbstractRepository<NodeModel, NodeEntity, NodePK> {


    private final static Logger logger = LoggerFactory.getLogger(NodeRepository.class);

    public NodeRepository(Class<NodeModel> thriftGenericClass, Class<NodeEntity> dbEntityGenericClass) {
        super(thriftGenericClass, dbEntityGenericClass);
    }
}