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
package org.apache.airavata.registry.core.repositories.expcatalog;

import org.apache.airavata.model.experiment.ExperimentStatistics;
import org.apache.airavata.model.experiment.ExperimentSummaryModel;
import org.apache.airavata.model.status.ExperimentState;
import org.apache.airavata.registry.core.entities.expcatalog.ExperimentSummaryEntity;
import org.apache.airavata.registry.core.entities.expcatalog.JobEntity;
import org.apache.airavata.registry.core.utils.DBConstants;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.airavata.registry.cpi.ResultOrderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.Query;

public class ExperimentSummaryRepository extends ExpCatAbstractRepository<ExperimentSummaryModel, ExperimentSummaryEntity, String> {
    private final static Logger logger = LoggerFactory.getLogger(ExperimentSummaryRepository.class);
    private final static int ACCESSIBLE_EXPERIMENT_IDS_BATCH_SIZE = 10000;

    public ExperimentSummaryRepository() { super(ExperimentSummaryModel.class, ExperimentSummaryEntity.class); }

    public List<ExperimentSummaryModel> searchAllAccessibleExperiments(List<String> accessibleExperimentIds, Map<String, String> filters, int limit,
                                                                       int offset, Object orderByIdentifier, ResultOrderType resultOrderType) throws RegistryException, IllegalArgumentException {
        String query = "SELECT ES FROM " + ExperimentSummaryEntity.class.getSimpleName() + " ES WHERE ";
        String whereClause = "";
        Map<String, Object> queryParameters = new HashMap<>();

        if (filters == null || !filters.containsKey(DBConstants.Experiment.GATEWAY_ID)) {
            logger.error("GatewayId is required");
            throw new RegistryException("GatewayId is required");
        }

        if (filters.get(DBConstants.Job.JOB_ID) != null) {
            logger.debug("Filter Experiments by JobId");
            queryParameters.put(DBConstants.Job.JOB_ID, filters.get(DBConstants.Job.JOB_ID));
            String query_jobId = "SELECT P.experimentId FROM "
                    + JobEntity.class.getSimpleName() + " J "
                    + " JOIN J.task T"
                    + " JOIN T.process P"
                    + " WHERE J.jobId = : " + DBConstants.Job.JOB_ID;
            whereClause += "ES.experimentId IN  ( " + query_jobId + " ) AND ";
        }

        if (filters.get(DBConstants.Experiment.USER_NAME) != null) {
            logger.debug("Filter Experiments by User");
            queryParameters.put(DBConstants.Experiment.USER_NAME, filters.get(DBConstants.Experiment.USER_NAME));
            whereClause += "ES.userName LIKE :" + DBConstants.Experiment.USER_NAME + " AND ";
        }

        if (filters.get(DBConstants.Experiment.GATEWAY_ID) != null) {
            logger.debug("Filter Experiments by Gateway ID");
            queryParameters.put(DBConstants.Experiment.GATEWAY_ID, filters.get(DBConstants.Experiment.GATEWAY_ID));
            whereClause += "ES.gatewayId LIKE :" + DBConstants.Experiment.GATEWAY_ID + " AND ";
        }

        if (filters.get(DBConstants.Experiment.PROJECT_ID) != null) {
            logger.debug("Filter Experiments by Project ID");
            queryParameters.put(DBConstants.Experiment.PROJECT_ID, filters.get(DBConstants.Experiment.PROJECT_ID));
            whereClause += "ES.projectId LIKE :" + DBConstants.Experiment.PROJECT_ID + " AND ";
        }

        if (filters.get(DBConstants.Experiment.EXPERIMENT_NAME) != null) {
            logger.debug("Filter Experiments by Name");
            queryParameters.put(DBConstants.Experiment.EXPERIMENT_NAME, filters.get(DBConstants.Experiment.EXPERIMENT_NAME));
            whereClause += "ES.name LIKE :" + DBConstants.Experiment.EXPERIMENT_NAME + " AND ";
        }

        if (filters.get(DBConstants.Experiment.DESCRIPTION) != null) {
            logger.debug("Filter Experiments by Description");
            queryParameters.put(DBConstants.Experiment.DESCRIPTION, filters.get(DBConstants.Experiment.DESCRIPTION));
            whereClause += "ES.description LIKE :" + DBConstants.Experiment.DESCRIPTION + " AND ";
        }

        if (filters.get(DBConstants.Experiment.EXECUTION_ID) != null) {
            logger.debug("Filter Experiments by Execution ID");
            queryParameters.put(DBConstants.Experiment.EXECUTION_ID, filters.get(DBConstants.Experiment.EXECUTION_ID));
            whereClause += "ES.executionId LIKE :" + DBConstants.Experiment.EXECUTION_ID + " AND ";
        }

        if (filters.get(DBConstants.ExperimentSummary.EXPERIMENT_STATUS) != null) {
            logger.debug("Filter Experiments by State");
            String state = ExperimentState.valueOf(filters.get(DBConstants.ExperimentSummary.EXPERIMENT_STATUS)).toString();
            queryParameters.put(DBConstants.ExperimentSummary.EXPERIMENT_STATUS, state);
            whereClause += "ES.experimentStatus LIKE :" + DBConstants.ExperimentSummary.EXPERIMENT_STATUS + " AND ";
        }

        if (filters.get(DBConstants.ExperimentSummary.FROM_DATE) != null
                && filters.get(DBConstants.ExperimentSummary.TO_DATE) != null) {

            Timestamp fromDate = new Timestamp(Long.valueOf(filters.get(DBConstants.ExperimentSummary.FROM_DATE)));
            Timestamp toDate = new Timestamp(Long.valueOf(filters.get(DBConstants.ExperimentSummary.TO_DATE)));

            if (toDate.after(fromDate)) {
                logger.debug("Filter Experiments by CreationTime");
                queryParameters.put(DBConstants.ExperimentSummary.FROM_DATE, fromDate);
                queryParameters.put(DBConstants.ExperimentSummary.TO_DATE, toDate);
                whereClause += "ES.creationTime BETWEEN :" + DBConstants.ExperimentSummary.FROM_DATE + " AND :"
                        + DBConstants.ExperimentSummary.TO_DATE + " AND ";
            }

        }

        if (filters.get(DBConstants.Experiment.USER_NAME) != null) {
            logger.debug("Filter Experiments by Username");
            queryParameters.put(DBConstants.Experiment.USER_NAME, filters.get(DBConstants.Experiment.USER_NAME));
            whereClause += "ES.userName = :" + DBConstants.Experiment.USER_NAME + " AND ";
        }

        if (!accessibleExperimentIds.isEmpty()) {
            logger.debug("Filter Experiments by Accessible Experiment IDs");
            queryParameters.put(DBConstants.Experiment.ACCESSIBLE_EXPERIMENT_IDS, accessibleExperimentIds);
            whereClause += " ES.experimentId IN :" + DBConstants.Experiment.ACCESSIBLE_EXPERIMENT_IDS;
        }

        else {
            // If no experiments are accessible then immediately return an empty list
            return new ArrayList<ExperimentSummaryModel>();
        }

        int queryLimit = limit;
        int queryOffset = offset;
        int accessibleExperimentIdsBatchNum = 0;

        // Figure out the initial batch of accessible experiment ids and the
        // offset into it by counting the matching experiments in each batch
        if (queryOffset > 0) {
            String countQuery = "SELECT COUNT(ES) FROM " + ExperimentSummaryEntity.class.getSimpleName() + " ES WHERE ";
            countQuery += whereClause;
            BatchOffset batchOffset = findInitialAccessibleExperimentsBatchOffset(countQuery, queryOffset, queryParameters, accessibleExperimentIds);
            queryOffset = batchOffset.offset;
            accessibleExperimentIdsBatchNum = batchOffset.batchNum;
        }

        query += whereClause;
        if (orderByIdentifier != null && resultOrderType != null && orderByIdentifier.equals(DBConstants.Experiment.CREATION_TIME)) {
            String order = (resultOrderType == ResultOrderType.ASC) ? "ASC" : "DESC";
            query += " ORDER BY ES." + DBConstants.Experiment.CREATION_TIME + " " + order;
        }

        List<ExperimentSummaryModel> allExperimentSummaryModels = new ArrayList<>();

        // Break up the query in batches over accessibleExperimentIds
        // NOTE: this assumes that the accessibleExperimentIds are sorted in the
        // same order as the expected experiment summary results
        double totalBatches = Math.ceil(
                Integer.valueOf(accessibleExperimentIds.size()).floatValue() / ACCESSIBLE_EXPERIMENT_IDS_BATCH_SIZE);
        for (int batchNum = accessibleExperimentIdsBatchNum; batchNum < totalBatches; batchNum++) {
            List<String> accessibleExperimentIdsBatch = accessibleExperimentIds.subList(
                    batchNum * ACCESSIBLE_EXPERIMENT_IDS_BATCH_SIZE,
                    Math.min(accessibleExperimentIds.size(), (batchNum + 1) * ACCESSIBLE_EXPERIMENT_IDS_BATCH_SIZE));
            queryParameters.put(DBConstants.Experiment.ACCESSIBLE_EXPERIMENT_IDS, accessibleExperimentIdsBatch);
            List<ExperimentSummaryModel> experimentSummaryModelList = select(query, queryLimit, queryOffset, queryParameters);
            allExperimentSummaryModels.addAll(experimentSummaryModelList);
            if (allExperimentSummaryModels.size() == limit) {
                return allExperimentSummaryModels;
            } else if (limit > 0 && allExperimentSummaryModels.size() < limit) {
                queryLimit -= experimentSummaryModelList.size();
                // In the next and subsequent batches, start from offset 0
                queryOffset = 0;
            }
        }
        return allExperimentSummaryModels;
    }

    class BatchOffset {
        final int batchNum;
        final int offset;

        BatchOffset(int batchNum, int offset) {
            this.batchNum = batchNum;
            this.offset = offset;
        }
    }

    private BatchOffset findInitialAccessibleExperimentsBatchOffset(String query, int queryOffset,
            Map<String, Object> queryParameters, List<String> accessibleExperimentIds) {
        
        int accumulator = 0;

        double totalBatches = Math.ceil(
                Integer.valueOf(accessibleExperimentIds.size()).floatValue() / ACCESSIBLE_EXPERIMENT_IDS_BATCH_SIZE);
        for (int batchNum = 0; batchNum < totalBatches; batchNum++) {
            List<String> accessibleExperimentIdsBatch = accessibleExperimentIds.subList(
                    batchNum * ACCESSIBLE_EXPERIMENT_IDS_BATCH_SIZE,
                    Math.min(accessibleExperimentIds.size(), (batchNum + 1) * ACCESSIBLE_EXPERIMENT_IDS_BATCH_SIZE));
            queryParameters.put(DBConstants.Experiment.ACCESSIBLE_EXPERIMENT_IDS, accessibleExperimentIdsBatch);
            int count = scalarInt(query, queryParameters);
            if (accumulator + count > queryOffset ) {
                return new BatchOffset(batchNum, queryOffset - accumulator);
            } else if (accumulator + count == queryOffset) {
                // The initial batch is the next batch since this batch ends at the queryOffset
                return new BatchOffset(batchNum + 1, 0);
            }
            accumulator += count;
        }
        // We didn't find a batch with the offset in it, so just return a batch
        // num past the last one
        return new BatchOffset(Double.valueOf(totalBatches).intValue(), 0);
    }

    public ExperimentStatistics getAccessibleExperimentStatistics(List<String> accessibleExperimentIds, Map<String,String> filters, int limit, int offset) throws RegistryException {

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
                    logger.debug("Set the GatewayId");
                    gatewayId = filters.get(field);
                }

                if (field.equals(DBConstants.Experiment.USER_NAME)) {
                    logger.debug("Set the UserName");
                    userName = filters.get(field);
                }

                if (field.equals(DBConstants.Experiment.EXECUTION_ID)) {
                    logger.debug("Set the ApplicationName");
                    applicationName = filters.get(field);
                }

                if (field.equals(DBConstants.Experiment.RESOURCE_HOST_ID)) {
                    logger.debug("Set the ResourceHostName");
                    resourceHostName = filters.get(field);
                }

                if (field.equals(DBConstants.ExperimentSummary.FROM_DATE)) {
                    logger.debug("Set the FromDate");
                    fromDate = new Timestamp(Long.parseLong(filters.get(field)));
                }

                if (field.equals(DBConstants.ExperimentSummary.TO_DATE)) {
                    logger.debug("Set the ToDate");
                    toDate = new Timestamp(Long.parseLong(filters.get(field)));
                }

            }

            int allExperimentsCount = getExperimentStatisticsCountForState(null, gatewayId, fromDate, toDate,
                    userName, applicationName, resourceHostName, accessibleExperimentIds);
            List<ExperimentSummaryModel> allExperiments = getExperimentStatisticsForState(null, gatewayId,
                    fromDate, toDate, userName, applicationName, resourceHostName, accessibleExperimentIds, limit, offset);
            experimentStatistics.setAllExperimentCount(allExperimentsCount);
            experimentStatistics.setAllExperiments(allExperiments);

            List<ExperimentState> createdStates = Arrays.asList(ExperimentState.CREATED, ExperimentState.VALIDATED);
            int createdExperimentsCount = getExperimentStatisticsCountForState(
                    createdStates, gatewayId, fromDate, toDate,
                    userName, applicationName, resourceHostName, accessibleExperimentIds);
            List<ExperimentSummaryModel> createdExperiments = getExperimentStatisticsForState(createdStates, gatewayId,
                    fromDate, toDate, userName, applicationName, resourceHostName, accessibleExperimentIds, limit, offset);
            experimentStatistics.setCreatedExperimentCount(createdExperimentsCount);
            experimentStatistics.setCreatedExperiments(createdExperiments);

            List<ExperimentState> runningStates = Arrays.asList(ExperimentState.EXECUTING, ExperimentState.SCHEDULED, ExperimentState.LAUNCHED);
            int runningExperimentsCount = getExperimentStatisticsCountForState(
                    runningStates, gatewayId,
                    fromDate, toDate, userName, applicationName, resourceHostName, accessibleExperimentIds);
            List<ExperimentSummaryModel> runningExperiments = getExperimentStatisticsForState(runningStates, gatewayId,
                    fromDate, toDate, userName, applicationName, resourceHostName, accessibleExperimentIds, limit, offset);
            experimentStatistics.setRunningExperimentCount(runningExperimentsCount);
            experimentStatistics.setRunningExperiments(runningExperiments);

            List<ExperimentState> completedStates = Arrays.asList(ExperimentState.COMPLETED);
            int completedExperimentsCount = getExperimentStatisticsCountForState(
                    completedStates, gatewayId,
                    fromDate, toDate, userName, applicationName, resourceHostName, accessibleExperimentIds);
            List<ExperimentSummaryModel> completedExperiments = getExperimentStatisticsForState(
                    completedStates, gatewayId, fromDate, toDate, userName, applicationName, resourceHostName,
                    accessibleExperimentIds, limit, offset);
            experimentStatistics.setCompletedExperimentCount(completedExperimentsCount);
            experimentStatistics.setCompletedExperiments(completedExperiments);

            List<ExperimentState> failedStates = Arrays.asList(ExperimentState.FAILED);
            int failedExperimentsCount = getExperimentStatisticsCountForState(
                    failedStates, gatewayId,
                    fromDate, toDate, userName, applicationName, resourceHostName, accessibleExperimentIds);
            List<ExperimentSummaryModel> failedExperiments = getExperimentStatisticsForState(failedStates,
                    gatewayId, fromDate, toDate, userName, applicationName, resourceHostName, accessibleExperimentIds, limit, offset);
            experimentStatistics.setFailedExperimentCount(failedExperimentsCount);
            experimentStatistics.setFailedExperiments(failedExperiments);

            List<ExperimentState> cancelledStates = Arrays.asList(ExperimentState.CANCELED, ExperimentState.CANCELING);
            int cancelledExperimentsCount = getExperimentStatisticsCountForState(
                    cancelledStates, gatewayId,
                    fromDate, toDate, userName, applicationName, resourceHostName, accessibleExperimentIds);
            List<ExperimentSummaryModel> cancelledExperiments = getExperimentStatisticsForState(
                    cancelledStates, gatewayId, fromDate, toDate, userName, applicationName, resourceHostName,
                    accessibleExperimentIds, limit, offset);
            experimentStatistics.setCancelledExperimentCount(cancelledExperimentsCount);
            experimentStatistics.setCancelledExperiments(cancelledExperiments);

            return experimentStatistics;
        }

        catch (RegistryException e) {
            logger.error("Error while retrieving experiment statistics from registry", e);
            throw new RegistryException(e);
        }

    }

    protected int getExperimentStatisticsCountForState(List<ExperimentState> experimentStates, String gatewayId, Timestamp fromDate, Timestamp toDate,
                                                                           String userName, String applicationName, String resourceHostName, List<String> experimentIds) throws RegistryException, IllegalArgumentException {
        String query = "SELECT count(ES.experimentId) FROM " + ExperimentSummaryEntity.class.getSimpleName() + " ES WHERE ";
        Map<String, Object> queryParameters = new HashMap<>();

        String finalQuery = filterExperimentStatisticsQuery(query, queryParameters, experimentStates, gatewayId, 
            fromDate, toDate, userName, applicationName, resourceHostName, experimentIds);
        
        if (finalQuery == null) {
            return 0;
        }

        long count = (long) execute(entityManager -> {
            Query jpaQuery = entityManager.createQuery(finalQuery);
            for (Map.Entry<String, Object> entry : queryParameters.entrySet()) {

                jpaQuery.setParameter(entry.getKey(), entry.getValue());
            }
            return jpaQuery.getSingleResult();
        });
        return Long.valueOf(count).intValue();
    }

    protected List<ExperimentSummaryModel> getExperimentStatisticsForState(List<ExperimentState> experimentStates, String gatewayId, Timestamp fromDate, Timestamp toDate,
                                                                           String userName, String applicationName, String resourceHostName, List<String> experimentIds, int limit, int offset) throws RegistryException, IllegalArgumentException {

        String query = "SELECT ES FROM " + ExperimentSummaryEntity.class.getSimpleName() + " ES WHERE ";
        Map<String, Object> queryParameters = new HashMap<>();

        query = filterExperimentStatisticsQuery(query, queryParameters, experimentStates, gatewayId, 
            fromDate, toDate, userName, applicationName, resourceHostName, experimentIds);

        if (query == null) {
            return new ArrayList<ExperimentSummaryModel>();
        }

        query += "ORDER BY ES.creationTime DESC, ES.experimentId"; // experimentId is the ordering tiebreaker
        List<ExperimentSummaryModel> experimentSummaryModelList = select(query, limit, offset, queryParameters);
        return experimentSummaryModelList;
    }

    protected String filterExperimentStatisticsQuery(String query, Map<String, Object> queryParameters, 
            List<ExperimentState> experimentStates, String gatewayId, Timestamp fromDate, 
            Timestamp toDate, String userName, String applicationName, String resourceHostName, List<String> experimentIds) {

        if (experimentStates != null) {
            logger.debug("Filter Experiments by Experiment States");
            List<String> statesAsStrings = experimentStates.stream().map(s -> s.toString()).collect(Collectors.toList());
            queryParameters.put(DBConstants.ExperimentSummary.EXPERIMENT_STATUS, statesAsStrings);
            query += "ES.experimentStatus IN :" + DBConstants.ExperimentSummary.EXPERIMENT_STATUS + " AND ";
        }

        if (gatewayId != null) {
            logger.debug("Filter Experiments by GatewayId");
            queryParameters.put(DBConstants.Experiment.GATEWAY_ID, gatewayId);
            query += "ES.gatewayId = :" + DBConstants.Experiment.GATEWAY_ID + " AND ";
        }

        if (fromDate != null && toDate != null) {

            if (toDate.after(fromDate)) {
                logger.debug("Filter Experiments by CreationTime");
                queryParameters.put(DBConstants.ExperimentSummary.FROM_DATE, fromDate);
                queryParameters.put(DBConstants.ExperimentSummary.TO_DATE, toDate);
                query += "ES.creationTime BETWEEN :" + DBConstants.ExperimentSummary.FROM_DATE + " AND :" + DBConstants.ExperimentSummary.TO_DATE + " AND ";
            }
        }

        if (userName != null) {
            logger.debug("Filter Experiments by UserName");
            queryParameters.put(DBConstants.Experiment.USER_NAME, userName);
            query += "ES.userName = :" + DBConstants.Experiment.USER_NAME + " AND ";
        }

        if (applicationName != null) {
            logger.debug("Filter Experiments by ApplicationName");
            queryParameters.put(DBConstants.Experiment.EXECUTION_ID, applicationName);
            query += "ES.executionId = :" + DBConstants.Experiment.EXECUTION_ID + " AND ";
        }

        if (experimentIds != null) {
            if (!experimentIds.isEmpty()) {
                logger.debug("Filter Experiments by experimentIds");
                queryParameters.put(DBConstants.Experiment.EXPERIMENT_ID, experimentIds);
                query += "ES.experimentId IN :" + DBConstants.Experiment.EXPERIMENT_ID + " AND ";
            } else {
                return null;
            }
        }

        if (resourceHostName != null) {
            logger.debug("Filter Experiments by ResourceHostName");
            queryParameters.put(DBConstants.Experiment.RESOURCE_HOST_ID, resourceHostName);
            query += "ES.resourceHostId = :" + DBConstants.Experiment.RESOURCE_HOST_ID + " ";
        }

        else {
            logger.debug("Removing the last operator from the query");
            query = query.substring(0, query.length() - 4);
        }

        return query;
    }

}
