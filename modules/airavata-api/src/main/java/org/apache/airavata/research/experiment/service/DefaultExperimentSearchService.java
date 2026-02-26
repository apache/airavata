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

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.airavata.core.exception.CoreExceptions.AiravataSystemException;
import org.apache.airavata.core.exception.RegistryExceptions.RegistryException;
import org.apache.airavata.core.exception.ValidationExceptions.ExceptionHandlerUtil;
import org.apache.airavata.core.model.EntitySearchField;
import org.apache.airavata.core.model.SearchCondition;
import org.apache.airavata.core.model.SearchCriteria;
import org.apache.airavata.core.util.Constants;
import org.apache.airavata.core.util.DBConstants;
import org.apache.airavata.iam.model.AuthzToken;
import org.apache.airavata.iam.service.SharingService;
import org.apache.airavata.research.experiment.entity.ExperimentSummaryEntity;
import org.apache.airavata.research.experiment.mapper.ExperimentSummaryMapper;
import org.apache.airavata.research.experiment.model.ExperimentSearchFields;
import org.apache.airavata.research.experiment.model.ExperimentState;
import org.apache.airavata.research.experiment.model.ExperimentStatistics;
import org.apache.airavata.research.experiment.model.ExperimentSummaryModel;
import org.apache.airavata.research.experiment.model.ResultOrderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service responsible for sharing-aware experiment search and statistics.
 * All queries are filtered through the sharing registry to return only
 * experiments accessible to the requesting user.
 * Extracted from {@link ExperimentOperationsService} as part of facade decomposition.
 * Also incorporates low-level summary/statistics queries previously in ExperimentSummaryService.
 */
@Service
@Transactional
public class DefaultExperimentSearchService implements ExperimentSearchService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultExperimentSearchService.class);
    private static final int ACCESSIBLE_EXPERIMENT_IDS_BATCH_SIZE = 10000;

    private final ExperimentSummaryMapper experimentSummaryMapper;
    private final EntityManager entityManager;
    private final SharingService sharingService;

    public DefaultExperimentSearchService(
            ExperimentSummaryMapper experimentSummaryMapper,
            EntityManager entityManager,
            SharingService sharingService) {
        this.experimentSummaryMapper = experimentSummaryMapper;
        this.entityManager = entityManager;
        this.sharingService = sharingService;
    }

    private AiravataSystemException airavataSystemException(String message, Throwable cause) {
        return ExceptionHandlerUtil.wrapAsAiravataException(message, cause);
    }

    /**
     * Search experiments accessible to a user, applying sharing-registry access control and
     * translating {@link ExperimentSearchFields} filters into both sharing-layer criteria and
     * database-layer criteria.
     *
     * @param authzToken authorization context, used to resolve the caller's gateway
     * @param gatewayId  the gateway to search within
     * @param userName   the user whose accessible experiments are searched
     * @param filters    key/value search filters; date range and structural filters are pushed
     *                   to the sharing layer, remaining filters are applied at the DB layer
     * @param limit      maximum number of results to return
     * @param offset     zero-based result offset for pagination
     * @return list of accessible experiment summaries matching all filters
     */
    public List<ExperimentSummaryModel> searchExperiments(
            AuthzToken authzToken,
            String gatewayId,
            String userName,
            Map<ExperimentSearchFields, String> filters,
            int limit,
            int offset)
            throws AiravataSystemException {
        try {
            var accessibleExpIds = new ArrayList<String>();
            var filtersCopy = new HashMap<>(filters);
            var sharingFilters = new ArrayList<SearchCriteria>();
            var entityTypeFilter = new SearchCriteria();
            entityTypeFilter.setSearchField(EntitySearchField.ENTITY_TYPE_ID);
            entityTypeFilter.setSearchCondition(SearchCondition.EQUAL);
            entityTypeFilter.setValue(gatewayId + ":EXPERIMENT");
            sharingFilters.add(entityTypeFilter);

            if (filtersCopy.containsKey(ExperimentSearchFields.FROM_DATE)) {
                var fromTime = filtersCopy.remove(ExperimentSearchFields.FROM_DATE);
                var criteria = new SearchCriteria();
                criteria.setSearchField(EntitySearchField.CREATED_TIME);
                criteria.setSearchCondition(SearchCondition.GTE);
                criteria.setValue(fromTime);
                sharingFilters.add(criteria);
            }
            if (filtersCopy.containsKey(ExperimentSearchFields.TO_DATE)) {
                var toTime = filtersCopy.remove(ExperimentSearchFields.TO_DATE);
                var criteria = new SearchCriteria();
                criteria.setSearchField(EntitySearchField.CREATED_TIME);
                criteria.setSearchCondition(SearchCondition.LTE);
                criteria.setValue(toTime);
                sharingFilters.add(criteria);
            }
            if (filtersCopy.containsKey(ExperimentSearchFields.PROJECT_ID)) {
                var projectId = filtersCopy.remove(ExperimentSearchFields.PROJECT_ID);
                var criteria = new SearchCriteria();
                criteria.setSearchField(EntitySearchField.PARRENT_ENTITY_ID);
                criteria.setSearchCondition(SearchCondition.EQUAL);
                criteria.setValue(projectId);
                sharingFilters.add(criteria);
            }
            if (filtersCopy.containsKey(ExperimentSearchFields.USER_NAME)) {
                var username = filtersCopy.remove(ExperimentSearchFields.USER_NAME);
                var criteria = new SearchCriteria();
                criteria.setSearchField(EntitySearchField.OWNER_ID);
                criteria.setSearchCondition(SearchCondition.EQUAL);
                criteria.setValue(username + "@" + gatewayId);
                sharingFilters.add(criteria);
            }
            if (filtersCopy.containsKey(ExperimentSearchFields.EXPERIMENT_NAME)) {
                var experimentName = filtersCopy.remove(ExperimentSearchFields.EXPERIMENT_NAME);
                var criteria = new SearchCriteria();
                criteria.setSearchField(EntitySearchField.NAME);
                criteria.setSearchCondition(SearchCondition.LIKE);
                criteria.setValue(experimentName);
                sharingFilters.add(criteria);
            }
            if (filtersCopy.containsKey(ExperimentSearchFields.EXPERIMENT_DESC)) {
                var experimentDescription = filtersCopy.remove(ExperimentSearchFields.EXPERIMENT_DESC);
                var criteria = new SearchCriteria();
                criteria.setSearchField(EntitySearchField.DESCRIPTION);
                criteria.setSearchCondition(SearchCondition.LIKE);
                criteria.setValue(experimentDescription);
                sharingFilters.add(criteria);
            }

            int searchOffset = 0;
            int searchLimit = Integer.MAX_VALUE;
            boolean filteredInSharing = filtersCopy.isEmpty();
            if (filteredInSharing) {
                searchOffset = offset;
                searchLimit = limit;
            }
            sharingService
                    .searchEntities(
                            authzToken.getClaimsMap().get(Constants.GATEWAY_ID),
                            userName + "@" + gatewayId,
                            sharingFilters,
                            searchOffset,
                            searchLimit)
                    .forEach(e -> accessibleExpIds.add(e.getEntityId()));
            int finalOffset = filteredInSharing ? 0 : offset;
            // Convert remaining ExperimentSearchFields filters to DBConstants string keys
            var dbFilters = new HashMap<String, String>();
            dbFilters.put(DBConstants.Experiment.GATEWAY_ID, gatewayId);
            for (Map.Entry<ExperimentSearchFields, String> entry : filtersCopy.entrySet()) {
                switch (entry.getKey()) {
                    case EXPERIMENT_NAME -> dbFilters.put(DBConstants.Experiment.EXPERIMENT_NAME, entry.getValue());
                    case EXPERIMENT_DESC -> dbFilters.put(DBConstants.Experiment.DESCRIPTION, entry.getValue());
                    case APPLICATION_ID -> dbFilters.put(DBConstants.Experiment.EXECUTION_ID, entry.getValue());
                    case STATUS -> dbFilters.put(DBConstants.ExperimentSummary.EXPERIMENT_STATUS, entry.getValue());
                    case PROJECT_ID -> dbFilters.put(DBConstants.Experiment.PROJECT_ID, entry.getValue());
                    case USER_NAME -> dbFilters.put(DBConstants.Experiment.USER_NAME, entry.getValue());
                    default -> {
                        /* FROM_DATE/TO_DATE/JOB_ID already handled above via sharingFilters */
                    }
                }
            }
            return searchAllAccessibleExperiments(accessibleExpIds, dbFilters, limit, finalOffset, null, null);
        } catch (Exception e) {
            String msg = "Error while retrieving experiments: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(msg, e);
        }
    }

    /**
     * Return experiment statistics (counts and summary lists per status) for experiments
     * accessible to the caller within the given gateway and time window.
     *
     * @param gatewayId        the gateway to query
     * @param fromTime         epoch-millisecond lower bound for experiment creation time (0 = no bound)
     * @param toTime           epoch-millisecond upper bound for experiment creation time (0 = no bound)
     * @param userName         optional user filter; {@code null} means all users
     * @param applicationName  optional application/execution-id filter; {@code null} means all apps
     * @param resourceHostName optional compute-resource filter; {@code null} means all resources
     * @param accessibleExpIds pre-resolved list of experiment IDs the caller may access
     *                         (pass {@code null} to query without an access-control restriction)
     * @param limit            maximum number of experiments per status bucket
     * @param offset           zero-based offset for pagination within each bucket
     * @return an {@link ExperimentStatistics} containing per-status counts and sample lists
     */
    public ExperimentStatistics getExperimentStatistics(
            String gatewayId,
            long fromTime,
            long toTime,
            String userName,
            String applicationName,
            String resourceHostName,
            List<String> accessibleExpIds,
            int limit,
            int offset)
            throws AiravataSystemException {
        try {
            var filters = new HashMap<String, String>();
            filters.put(DBConstants.Experiment.GATEWAY_ID, gatewayId);
            if (userName != null) filters.put(DBConstants.Experiment.USER_NAME, userName);
            if (applicationName != null) filters.put(DBConstants.Experiment.EXECUTION_ID, applicationName);
            if (resourceHostName != null) filters.put(DBConstants.Experiment.RESOURCE_HOST_ID, resourceHostName);
            if (fromTime > 0) filters.put(DBConstants.ExperimentSummary.FROM_DATE, String.valueOf(fromTime));
            if (toTime > 0) filters.put(DBConstants.ExperimentSummary.TO_DATE, String.valueOf(toTime));
            return getAccessibleExperimentStatistics(accessibleExpIds, filters, limit, offset);
        } catch (Exception e) {
            String msg = "Error while getting experiment statistics: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(msg, e);
        }
    }

    public List<ExperimentSummaryModel> searchAllAccessibleExperiments(
            List<String> accessibleExperimentIds,
            Map<String, String> filters,
            int limit,
            int offset,
            Object orderByIdentifier,
            ResultOrderType resultOrderType)
            throws RegistryException, IllegalArgumentException {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ExperimentSummaryEntity> query = cb.createQuery(ExperimentSummaryEntity.class);
        Root<ExperimentSummaryEntity> root = query.from(ExperimentSummaryEntity.class);

        List<Predicate> predicates = new ArrayList<>();

        if (filters == null || !filters.containsKey(DBConstants.Experiment.GATEWAY_ID)) {
            logger.error("GatewayId is required");
            throw new RegistryException("GatewayId is required");
        }

        // Build predicates from filters
        if (filters.get(DBConstants.Experiment.USER_NAME) != null) {
            predicates.add(cb.like(root.get("userName"), "%" + filters.get(DBConstants.Experiment.USER_NAME) + "%"));
        }

        if (filters.get(DBConstants.Experiment.GATEWAY_ID) != null) {
            predicates.add(cb.like(root.get("gatewayId"), "%" + filters.get(DBConstants.Experiment.GATEWAY_ID) + "%"));
        }

        if (filters.get(DBConstants.Experiment.PROJECT_ID) != null) {
            predicates.add(cb.like(root.get("projectId"), "%" + filters.get(DBConstants.Experiment.PROJECT_ID) + "%"));
        }

        if (filters.get(DBConstants.Experiment.EXPERIMENT_NAME) != null) {
            predicates.add(cb.like(root.get("name"), "%" + filters.get(DBConstants.Experiment.EXPERIMENT_NAME) + "%"));
        }

        if (filters.get(DBConstants.Experiment.DESCRIPTION) != null) {
            predicates.add(
                    cb.like(root.get("description"), "%" + filters.get(DBConstants.Experiment.DESCRIPTION) + "%"));
        }

        if (filters.get(DBConstants.Experiment.EXECUTION_ID) != null) {
            predicates.add(
                    cb.like(root.get("executionId"), "%" + filters.get(DBConstants.Experiment.EXECUTION_ID) + "%"));
        }

        if (filters.get(DBConstants.ExperimentSummary.EXPERIMENT_STATUS) != null) {
            String state = ExperimentState.valueOf(filters.get(DBConstants.ExperimentSummary.EXPERIMENT_STATUS))
                    .toString();
            predicates.add(cb.equal(root.get("experimentStatus"), state));
        }

        if (filters.get(DBConstants.ExperimentSummary.FROM_DATE) != null
                && filters.get(DBConstants.ExperimentSummary.TO_DATE) != null) {
            Timestamp fromDate = new Timestamp(Long.valueOf(filters.get(DBConstants.ExperimentSummary.FROM_DATE)));
            Timestamp toDate = new Timestamp(Long.valueOf(filters.get(DBConstants.ExperimentSummary.TO_DATE)));
            if (toDate.after(fromDate)) {
                predicates.add(cb.between(root.get("creationTime"), fromDate, toDate));
            }
        }

        // Apply accessible experiment IDs filter if provided
        // If empty but we have filters (e.g., USER_NAME when sharing is disabled), still search using filters
        if (!accessibleExperimentIds.isEmpty()) {
            predicates.add(root.get("experimentId").in(accessibleExperimentIds));
        } else if (filters.get(DBConstants.Experiment.USER_NAME) == null
                && filters.get(DBConstants.Experiment.PROJECT_ID) == null
                && filters.get(DBConstants.Experiment.EXPERIMENT_NAME) == null) {
            // If no accessible IDs and no user/project/name filters, return empty
            // This handles the case where sharing is enabled but no accessible experiments
            return new ArrayList<>();
        }
        // Otherwise, continue with filter-based search (sharing disabled with USER_NAME filter)

        query.where(cb.and(predicates.toArray(new Predicate[0])));

        if (orderByIdentifier != null
                && resultOrderType != null
                && orderByIdentifier.equals(DBConstants.Experiment.CREATION_TIME)) {
            if (resultOrderType == ResultOrderType.ASC) {
                query.orderBy(cb.asc(root.get("creationTime")), cb.asc(root.get("experimentId")));
            } else {
                query.orderBy(cb.desc(root.get("creationTime")), cb.asc(root.get("experimentId")));
            }
        }

        // Handle batching for large accessibleExperimentIds lists
        List<ExperimentSummaryModel> allExperimentSummaryModels = new ArrayList<>();
        double totalBatches = Math.ceil(
                Integer.valueOf(accessibleExperimentIds.size()).floatValue() / ACCESSIBLE_EXPERIMENT_IDS_BATCH_SIZE);

        for (int batchNum = 0; batchNum < totalBatches; batchNum++) {
            List<String> accessibleExperimentIdsBatch = accessibleExperimentIds.subList(
                    batchNum * ACCESSIBLE_EXPERIMENT_IDS_BATCH_SIZE,
                    Math.min(accessibleExperimentIds.size(), (batchNum + 1) * ACCESSIBLE_EXPERIMENT_IDS_BATCH_SIZE));

            // Update the IN predicate for this batch
            predicates.set(predicates.size() - 1, root.get("experimentId").in(accessibleExperimentIdsBatch));
            query.where(cb.and(predicates.toArray(new Predicate[0])));

            TypedQuery<ExperimentSummaryEntity> typedQuery = entityManager.createQuery(query);
            if (offset > 0 && batchNum == 0) {
                typedQuery.setFirstResult(offset);
            } else if (batchNum > 0) {
                typedQuery.setFirstResult(0);
            }
            if (limit > 0) {
                int remainingLimit = limit - allExperimentSummaryModels.size();
                if (remainingLimit > 0) {
                    typedQuery.setMaxResults(remainingLimit);
                } else {
                    break;
                }
            }

            List<ExperimentSummaryEntity> entities = typedQuery.getResultList();
            allExperimentSummaryModels.addAll(experimentSummaryMapper.toModelList(entities));

            if (allExperimentSummaryModels.size() == limit) {
                break;
            }
        }

        return allExperimentSummaryModels;
    }

    public ExperimentStatistics getAccessibleExperimentStatistics(
            List<String> accessibleExperimentIds, Map<String, String> filters, int limit, int offset)
            throws RegistryException {
        try {
            ExperimentStatistics experimentStatistics = new ExperimentStatistics();
            String gatewayId = null;
            String userName = null;
            String applicationName = null;
            String resourceHostName = null;
            Timestamp fromDate = null;
            Timestamp toDate = null;

            if (filters == null || !filters.containsKey(DBConstants.Experiment.GATEWAY_ID)) {
                logger.error("GatewayId is required");
                throw new RegistryException("GatewayId is required");
            }

            for (String field : filters.keySet()) {
                if (field.equals(DBConstants.Experiment.GATEWAY_ID)) {
                    gatewayId = filters.get(field);
                }
                if (field.equals(DBConstants.Experiment.USER_NAME)) {
                    userName = filters.get(field);
                }
                if (field.equals(DBConstants.Experiment.EXECUTION_ID)) {
                    applicationName = filters.get(field);
                }
                if (field.equals(DBConstants.Experiment.RESOURCE_HOST_ID)) {
                    resourceHostName = filters.get(field);
                }
                if (field.equals(DBConstants.ExperimentSummary.FROM_DATE)) {
                    fromDate = new Timestamp(Long.parseLong(filters.get(field)));
                }
                if (field.equals(DBConstants.ExperimentSummary.TO_DATE)) {
                    toDate = new Timestamp(Long.parseLong(filters.get(field)));
                }
            }

            int allExperimentsCount = getExperimentStatisticsCountForState(
                    null,
                    gatewayId,
                    fromDate,
                    toDate,
                    userName,
                    applicationName,
                    resourceHostName,
                    accessibleExperimentIds);
            List<ExperimentSummaryModel> allExperiments = getExperimentStatisticsForState(
                    null,
                    gatewayId,
                    fromDate,
                    toDate,
                    userName,
                    applicationName,
                    resourceHostName,
                    accessibleExperimentIds,
                    limit,
                    offset);
            experimentStatistics.setAllExperimentCount(allExperimentsCount);
            experimentStatistics.setAllExperiments(allExperiments);

            List<ExperimentState> createdStates = Arrays.asList(ExperimentState.CREATED, ExperimentState.VALIDATED);
            int createdExperimentsCount = getExperimentStatisticsCountForState(
                    createdStates,
                    gatewayId,
                    fromDate,
                    toDate,
                    userName,
                    applicationName,
                    resourceHostName,
                    accessibleExperimentIds);
            List<ExperimentSummaryModel> createdExperiments = getExperimentStatisticsForState(
                    createdStates,
                    gatewayId,
                    fromDate,
                    toDate,
                    userName,
                    applicationName,
                    resourceHostName,
                    accessibleExperimentIds,
                    limit,
                    offset);
            experimentStatistics.setCreatedExperimentCount(createdExperimentsCount);
            experimentStatistics.setCreatedExperiments(createdExperiments);

            List<ExperimentState> runningStates =
                    Arrays.asList(ExperimentState.EXECUTING, ExperimentState.SCHEDULED, ExperimentState.LAUNCHED);
            int runningExperimentsCount = getExperimentStatisticsCountForState(
                    runningStates,
                    gatewayId,
                    fromDate,
                    toDate,
                    userName,
                    applicationName,
                    resourceHostName,
                    accessibleExperimentIds);
            List<ExperimentSummaryModel> runningExperiments = getExperimentStatisticsForState(
                    runningStates,
                    gatewayId,
                    fromDate,
                    toDate,
                    userName,
                    applicationName,
                    resourceHostName,
                    accessibleExperimentIds,
                    limit,
                    offset);
            experimentStatistics.setRunningExperimentCount(runningExperimentsCount);
            experimentStatistics.setRunningExperiments(runningExperiments);

            List<ExperimentState> completedStates = Arrays.asList(ExperimentState.COMPLETED);
            int completedExperimentsCount = getExperimentStatisticsCountForState(
                    completedStates,
                    gatewayId,
                    fromDate,
                    toDate,
                    userName,
                    applicationName,
                    resourceHostName,
                    accessibleExperimentIds);
            List<ExperimentSummaryModel> completedExperiments = getExperimentStatisticsForState(
                    completedStates,
                    gatewayId,
                    fromDate,
                    toDate,
                    userName,
                    applicationName,
                    resourceHostName,
                    accessibleExperimentIds,
                    limit,
                    offset);
            experimentStatistics.setCompletedExperimentCount(completedExperimentsCount);
            experimentStatistics.setCompletedExperiments(completedExperiments);

            List<ExperimentState> failedStates = Arrays.asList(ExperimentState.FAILED);
            int failedExperimentsCount = getExperimentStatisticsCountForState(
                    failedStates,
                    gatewayId,
                    fromDate,
                    toDate,
                    userName,
                    applicationName,
                    resourceHostName,
                    accessibleExperimentIds);
            List<ExperimentSummaryModel> failedExperiments = getExperimentStatisticsForState(
                    failedStates,
                    gatewayId,
                    fromDate,
                    toDate,
                    userName,
                    applicationName,
                    resourceHostName,
                    accessibleExperimentIds,
                    limit,
                    offset);
            experimentStatistics.setFailedExperimentCount(failedExperimentsCount);
            experimentStatistics.setFailedExperiments(failedExperiments);

            List<ExperimentState> cancelledStates = Arrays.asList(ExperimentState.CANCELED, ExperimentState.CANCELING);
            int cancelledExperimentsCount = getExperimentStatisticsCountForState(
                    cancelledStates,
                    gatewayId,
                    fromDate,
                    toDate,
                    userName,
                    applicationName,
                    resourceHostName,
                    accessibleExperimentIds);
            List<ExperimentSummaryModel> cancelledExperiments = getExperimentStatisticsForState(
                    cancelledStates,
                    gatewayId,
                    fromDate,
                    toDate,
                    userName,
                    applicationName,
                    resourceHostName,
                    accessibleExperimentIds,
                    limit,
                    offset);
            experimentStatistics.setCancelledExperimentCount(cancelledExperimentsCount);
            experimentStatistics.setCancelledExperiments(cancelledExperiments);

            return experimentStatistics;
        } catch (RegistryException e) {
            String message =
                    String.format("Error while retrieving experiment statistics from registry: %s", e.getMessage());
            logger.error(message, e);
            throw new RegistryException(message, e);
        }
    }

    private int getExperimentStatisticsCountForState(
            List<ExperimentState> experimentStates,
            String gatewayId,
            Timestamp fromDate,
            Timestamp toDate,
            String userName,
            String applicationName,
            String resourceHostName,
            List<String> experimentIds)
            throws RegistryException, IllegalArgumentException {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<ExperimentSummaryEntity> root = query.from(ExperimentSummaryEntity.class);

        List<Predicate> predicates = buildStatisticsPredicates(
                cb,
                root,
                experimentStates,
                gatewayId,
                fromDate,
                toDate,
                userName,
                applicationName,
                resourceHostName,
                experimentIds);

        if (predicates.isEmpty() || experimentIds == null || experimentIds.isEmpty()) {
            return 0;
        }

        query.select(cb.count(root));
        query.where(cb.and(predicates.toArray(new Predicate[0])));

        Long count = entityManager.createQuery(query).getSingleResult();
        return Long.valueOf(count).intValue();
    }

    private List<ExperimentSummaryModel> getExperimentStatisticsForState(
            List<ExperimentState> experimentStates,
            String gatewayId,
            Timestamp fromDate,
            Timestamp toDate,
            String userName,
            String applicationName,
            String resourceHostName,
            List<String> experimentIds,
            int limit,
            int offset)
            throws RegistryException, IllegalArgumentException {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ExperimentSummaryEntity> query = cb.createQuery(ExperimentSummaryEntity.class);
        Root<ExperimentSummaryEntity> root = query.from(ExperimentSummaryEntity.class);

        List<Predicate> predicates = buildStatisticsPredicates(
                cb,
                root,
                experimentStates,
                gatewayId,
                fromDate,
                toDate,
                userName,
                applicationName,
                resourceHostName,
                experimentIds);

        if (predicates.isEmpty() || experimentIds == null || experimentIds.isEmpty()) {
            return new ArrayList<>();
        }

        query.where(cb.and(predicates.toArray(new Predicate[0])));
        query.orderBy(cb.desc(root.get("creationTime")), cb.asc(root.get("experimentId")));

        TypedQuery<ExperimentSummaryEntity> typedQuery = entityManager.createQuery(query);
        if (offset > 0) {
            typedQuery.setFirstResult(offset);
        }
        if (limit > 0) {
            typedQuery.setMaxResults(limit);
        }

        List<ExperimentSummaryEntity> entities = typedQuery.getResultList();
        return experimentSummaryMapper.toModelList(entities);
    }

    private List<Predicate> buildStatisticsPredicates(
            CriteriaBuilder cb,
            Root<ExperimentSummaryEntity> root,
            List<ExperimentState> experimentStates,
            String gatewayId,
            Timestamp fromDate,
            Timestamp toDate,
            String userName,
            String applicationName,
            String resourceHostName,
            List<String> experimentIds) {
        List<Predicate> predicates = new ArrayList<>();

        if (experimentStates != null) {
            List<String> statesAsStrings =
                    experimentStates.stream().map(ExperimentState::toString).toList();
            predicates.add(root.get("experimentStatus").in(statesAsStrings));
        }

        if (gatewayId != null) {
            predicates.add(cb.equal(root.get("gatewayId"), gatewayId));
        }

        if (fromDate != null && toDate != null && toDate.after(fromDate)) {
            predicates.add(cb.between(root.get("creationTime"), fromDate, toDate));
        }

        if (userName != null) {
            predicates.add(cb.equal(root.get("userName"), userName));
        }

        if (applicationName != null) {
            predicates.add(cb.equal(root.get("executionId"), applicationName));
        }

        if (experimentIds != null && !experimentIds.isEmpty()) {
            predicates.add(root.get("experimentId").in(experimentIds));
        } else {
            return new ArrayList<>(); // Return empty if no experimentIds
        }

        if (resourceHostName != null) {
            predicates.add(cb.equal(root.get("resourceHostId"), resourceHostName));
        }

        return predicates;
    }
}
