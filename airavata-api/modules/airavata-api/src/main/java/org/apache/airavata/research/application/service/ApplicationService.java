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
package org.apache.airavata.research.application.service;

import java.util.List;
import org.apache.airavata.core.service.CrudService;
import org.apache.airavata.research.application.model.Application;

/**
 * Domain service for managing science applications registered in the gateway.
 *
 * <p>Extends {@link CrudService} for the standard create/get/update/delete/listByGateway
 * contract. Domain-specific aliases and additional query methods are defined here.
 */
public interface ApplicationService extends CrudService<Application> {

    /** Return the application with the given id, or {@code null} if not found. */
    default Application getApplication(String applicationId) {
        return get(applicationId);
    }

    /** Return all applications registered in the given gateway. */
    default List<Application> getApplications(String gatewayId) {
        return listByGateway(gatewayId);
    }

    /** Create a new application and return its generated id. */
    default String createApplication(Application application) {
        return create(application);
    }

    /** Update the application identified by {@code applicationId}. */
    default void updateApplication(String applicationId, Application application) {
        update(applicationId, application);
    }

    /** Delete the application identified by {@code applicationId}. */
    default void deleteApplication(String applicationId) {
        delete(applicationId);
    }
}
