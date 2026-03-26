/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.core.service;

import java.util.List;

/**
 * Generic CRUD contract for domain services that manage a single model type
 * scoped to a gateway.
 *
 * <p>Services with richer APIs should extend this interface with additional
 * domain-specific methods.
 *
 * @param <M> the domain model type
 */
public interface CrudService<M> {

    String create(M model);

    M get(String id);

    void update(String id, M model);

    void delete(String id);

    List<M> listByGateway(String gatewayId);
}
