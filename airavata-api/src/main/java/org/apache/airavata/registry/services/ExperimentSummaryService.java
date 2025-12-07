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
package org.apache.airavata.registry.services;

import com.github.dozermapper.core.Mapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.airavata.model.experiment.ExperimentStatistics;
import org.apache.airavata.model.experiment.ExperimentSummaryModel;
import org.apache.airavata.model.status.ExperimentState;
import org.apache.airavata.registry.cpi.ResultOrderType;
import org.apache.airavata.registry.entities.expcatalog.ExperimentSummaryEntity;
import org.apache.airavata.registry.entities.expcatalog.JobEntity;
import org.apache.airavata.registry.exceptions.RegistryException;
import org.apache.airavata.registry.utils.DBConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ExperimentSummaryService {
    private static final Logger logger = LoggerFactory.getLogger(ExperimentSummaryService.class);
    private static final int ACCESSIBLE_EXPERIMENT_IDS_BATCH_SIZE = 10000;

    @Autowired
    private Mapper mapper;

    @PersistenceContext(unitName = "experiment_data_new")
    private EntityManager entityManager;

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
        if (filters.get(DBConstants.Job.JOB_ID) != null) {
            logger.debug("Filter Experiments by JobId");
            // Subquery for job-based filtering
            Subquery<String> jobSubquery = query.subquery(String.class);
            Root<JobEntity> jobRoot = jobSubquery.from(JobEntity.class);
            jobSubquery
                    .select(jobRoot.get("task").get("process").get("experimentId"))
                    .where(cb.equal(jobRoot.get("jobId"), filters.get(DBConstants.Job.JOB_ID)));
            predicates.add(root.get("experimentId").in(jobSubquery));
        }

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
            predicates.add(cb.like(root.get("experimentStatus"), "%" + state + "%"));
        }

        if (filters.get(DBConstants.ExperimentSummary.FROM_DATE) != null
                && filters.get(DBConstants.ExperimentSummary.TO_DATE) != null) {
            Timestamp fromDate = new Timestamp(Long.valueOf(filters.get(DBConstants.ExperimentSummary.FROM_DATE)));
            Timestamp toDate = new Timestamp(Long.valueOf(filters.get(DBConstants.ExperimentSummary.TO_DATE)));
            if (toDate.after(fromDate)) {
                predicates.add(cb.between(root.get("creationTime"), fromDate, toDate));
            }
        }

        if (!accessibleExperimentIds.isEmpty()) {
            predicates.add(root.get("experimentId").in(accessibleExperimentIds));
        } else {
            return new ArrayList<>();
        }

        query.where(cb.and(predicates.toArray(new Predicate[0])));

        if (orderByIdentifier != null
                && resultOrderType != null
                && orderByIdentifier.equals(DBConstants.Experiment.CREATION_TIME)) {
            if (resultOrderType == ResultOrderType.ASC) {
                query.orderBy(cb.asc(root.get("creationTime")));
            } else {
                query.orderBy(cb.desc(root.get("creationTime")));
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
            entities.forEach(e -> allExperimentSummaryModels.add(mapper.map(e, ExperimentSummaryModel.class)));

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
            logger.error("Error while retrieving experiment statistics from registry", e);
            throw new RegistryException(e);
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
        return entities.stream()
                .map(e -> mapper.map(e, ExperimentSummaryModel.class))
                .toList();
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
                    experimentStates.stream().map(ExperimentState::toString).collect(Collectors.toList());
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
