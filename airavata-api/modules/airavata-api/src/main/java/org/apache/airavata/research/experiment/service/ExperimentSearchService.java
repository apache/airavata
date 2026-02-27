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
package org.apache.airavata.research.experiment.service;

import java.util.List;
import java.util.Map;
import org.apache.airavata.core.exception.CoreExceptions.AiravataSystemException;
import org.apache.airavata.core.exception.RegistryExceptions.RegistryException;
import org.apache.airavata.iam.model.AuthzToken;
import org.apache.airavata.research.experiment.model.ExperimentSearchFields;
import org.apache.airavata.research.experiment.model.ExperimentStatistics;
import org.apache.airavata.research.experiment.model.ExperimentSummary;
import org.apache.airavata.research.experiment.model.ResultOrderType;

/**
 * Service responsible for sharing-aware experiment search and statistics.
 * All queries are filtered through the sharing registry to return only
 * experiments accessible to the requesting user.
 */
public interface ExperimentSearchService {

    List<ExperimentSummary> searchExperiments(
            AuthzToken authzToken,
            String gatewayId,
            String userName,
            Map<ExperimentSearchFields, String> filters,
            int limit,
            int offset)
            throws AiravataSystemException;

    ExperimentStatistics getExperimentStatistics(
            String gatewayId,
            long fromTime,
            long toTime,
            String userName,
            String applicationName,
            String resourceHostName,
            List<String> accessibleExpIds,
            int limit,
            int offset)
            throws AiravataSystemException;

    // ========== Summary/Statistics (from ExperimentSummaryService) ==========

    List<ExperimentSummary> searchAllAccessibleExperiments(
            List<String> accessibleExperimentIds,
            Map<String, String> filters,
            int limit,
            int offset,
            Object orderByIdentifier,
            ResultOrderType resultOrderType)
            throws RegistryException;

    ExperimentStatistics getAccessibleExperimentStatistics(
            List<String> accessibleExperimentIds, Map<String, String> filters, int limit, int offset)
            throws RegistryException;
}
