package org.apache.airavata.registry.core.repositories.expcatalog;

import org.apache.airavata.model.experiment.ExperimentStatistics;
import org.apache.airavata.model.experiment.ExperimentSummaryModel;
import org.apache.airavata.model.status.ExperimentState;
import org.apache.airavata.registry.core.entities.expcatalog.ExperimentSummaryEntity;
import org.apache.airavata.registry.core.utils.DBConstants;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.airavata.registry.cpi.ResultOrderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExperimentSummaryRepository extends ExpCatAbstractRepository<ExperimentSummaryModel, ExperimentSummaryEntity, String> {
    private final static Logger logger = LoggerFactory.getLogger(ExperimentRepository.class);

    public ExperimentSummaryRepository() { super(ExperimentSummaryModel.class, ExperimentSummaryEntity.class); }

    public List<ExperimentSummaryModel> searchExperiments(Map<String, String> filters, int limit,
                                                          int offset, Object orderByIdentifier, ResultOrderType resultOrderType) throws RegistryException {
        return searchAllAccessibleExperiments(null, filters, limit, offset, orderByIdentifier, resultOrderType);
    }

    public List<ExperimentSummaryModel> searchAllAccessibleExperiments(List<String> accessibleExperimentIds, Map<String, String> filters, int limit,
                                                                       int offset, Object orderByIdentifier, ResultOrderType resultOrderType) throws RegistryException {
        String query = "SELECT ES FROM " + ExperimentSummaryEntity.class.getSimpleName() + " ES WHERE ";
        Map<String, Object> queryParameters = new HashMap<>();

        if (filters != null && !filters.isEmpty()) {

            for (String field : filters.keySet()) {

                if (field.equals(DBConstants.Experiment.USER_NAME)) {
                    logger.debug("Filter Experiments by User");
                    queryParameters.put(DBConstants.Experiment.USER_NAME, filters.get(field));
                    query += "ES.userName LIKE :" + DBConstants.Experiment.USER_NAME + " AND ";
                }

                else if (field.equals(DBConstants.Experiment.GATEWAY_ID)) {
                    logger.debug("Filter Experiments by Gateway ID");
                    queryParameters.put(DBConstants.Experiment.GATEWAY_ID, filters.get(field));
                    query += "ES.gatewayId LIKE :" + DBConstants.Experiment.GATEWAY_ID + " AND ";
                }

                else if (field.equals(DBConstants.Experiment.PROJECT_ID)) {
                    logger.debug("Filter Experiments by Project ID");
                    queryParameters.put(DBConstants.Experiment.PROJECT_ID, filters.get(field));
                    query += "ES.projectId LIKE :" + DBConstants.Experiment.PROJECT_ID + " AND ";
                }

                else if (field.equals(DBConstants.Experiment.EXPERIMENT_NAME)) {
                    logger.debug("Filter Experiments by Name");
                    queryParameters.put(DBConstants.Experiment.EXPERIMENT_NAME, filters.get(field));
                    query += "ES.name LIKE :" + DBConstants.Experiment.EXPERIMENT_NAME + " AND ";
                }

                else if (field.equals(DBConstants.Experiment.DESCRIPTION)) {
                    logger.debug("Filter Experiments by Description");
                    queryParameters.put(DBConstants.Experiment.DESCRIPTION, filters.get(field));
                    query += "ES.description LIKE :" + DBConstants.Experiment.DESCRIPTION + " AND ";
                }

                else if (field.equals(DBConstants.Experiment.EXECUTION_ID)) {
                    logger.debug("Filter Experiments by Execution ID");
                    queryParameters.put(DBConstants.Experiment.EXECUTION_ID, filters.get(field));
                    query += "ES.executionId LIKE :" + DBConstants.Experiment.EXECUTION_ID + " AND ";
                }

            }

        }

        if (filters.get(DBConstants.ExperimentStatus.STATE) != null) {
            logger.debug("Filter Experiments by State");
            String state = ExperimentState.valueOf(filters.get(DBConstants.ExperimentStatus.STATE)).toString();
            queryParameters.put(DBConstants.ExperimentStatus.STATE, state);
            query += "ES.state LIKE :" + DBConstants.ExperimentStatus.STATE + " AND ";
        }

        if (filters.get(DBConstants.ExperimentSummary.FROM_DATE) != null
                && filters.get(DBConstants.ExperimentSummary.TO_DATE) != null) {

            Timestamp fromDate = Timestamp.valueOf(filters.get(DBConstants.ExperimentSummary.FROM_DATE));
            Timestamp toDate = Timestamp.valueOf(filters.get(DBConstants.ExperimentSummary.TO_DATE));

            if (toDate.after(fromDate)) {
                logger.debug("Filter Experiments by CreationTime");
                queryParameters.put(DBConstants.ExperimentSummary.FROM_DATE, fromDate);
                queryParameters.put(DBConstants.ExperimentSummary.TO_DATE, toDate);
                query += "ES.creationTime BETWEEN :" + DBConstants.ExperimentSummary.FROM_DATE + " AND :" + DBConstants.ExperimentSummary.TO_DATE + " AND ";
            }

        }

        if (accessibleExperimentIds != null && !accessibleExperimentIds.isEmpty()) {
            logger.debug("Filter Experiments by Accessible Experiment IDs");
            queryParameters.put(DBConstants.Experiment.ACCESSIBLE_EXPERIMENT_IDS, accessibleExperimentIds);
            query += " ES.experimentId IN :" + DBConstants.Experiment.ACCESSIBLE_EXPERIMENT_IDS;
        }

        else {
            logger.debug("Removing the last operator from the query");
            query = query.substring(0, query.length() - 5);
        }

        List<ExperimentSummaryModel> experimentSummaryModelList = select(query, limit, offset, queryParameters);
        return experimentSummaryModelList;
    }

    public ExperimentStatistics getExperimentStatistics(Map<String,String> filters) throws RegistryException {

        try {

            ExperimentStatistics experimentStatistics = new ExperimentStatistics();
            String gatewayId = null;
            String userName = null;
            String applicationName = null;
            String resourceHostName = null;
            Timestamp fromDate = null;
            Timestamp toDate = null;

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
                    fromDate = Timestamp.valueOf(filters.get(field));
                }

                if (field.equals(DBConstants.ExperimentSummary.TO_DATE)) {
                    logger.debug("Set the ToDate");
                    toDate = Timestamp.valueOf(filters.get(field));
                }

            }

            List<ExperimentSummaryModel> allExperiments = getExperimentStatisticsForState(null, gatewayId,
                    fromDate, toDate, userName, applicationName, resourceHostName);
            experimentStatistics.setAllExperimentCount(allExperiments.size());
            experimentStatistics.setAllExperiments(allExperiments);

            List<ExperimentSummaryModel> createdExperiments = getExperimentStatisticsForState(ExperimentState.CREATED, gatewayId,
                    fromDate, toDate, userName, applicationName, resourceHostName);
            createdExperiments.addAll(getExperimentStatisticsForState(ExperimentState.VALIDATED, gatewayId,
                    fromDate, toDate, userName, applicationName, resourceHostName));
            experimentStatistics.setCreatedExperimentCount(createdExperiments.size());
            experimentStatistics.setCreatedExperiments(createdExperiments);

            List<ExperimentSummaryModel> runningExperiments = getExperimentStatisticsForState(ExperimentState.EXECUTING, gatewayId,
                    fromDate, toDate, userName, applicationName, resourceHostName);
            runningExperiments.addAll(getExperimentStatisticsForState(ExperimentState.SCHEDULED, gatewayId,
                    fromDate, toDate, userName, applicationName, resourceHostName));
            runningExperiments.addAll(getExperimentStatisticsForState(ExperimentState.LAUNCHED, gatewayId,
                    fromDate, toDate, userName, applicationName, resourceHostName));
            experimentStatistics.setRunningExperimentCount(runningExperiments.size());
            experimentStatistics.setRunningExperiments(runningExperiments);

            List<ExperimentSummaryModel> completedExperiments = getExperimentStatisticsForState(ExperimentState.COMPLETED, gatewayId,
                    fromDate, toDate, userName, applicationName, resourceHostName);
            experimentStatistics.setCompletedExperimentCount(completedExperiments.size());
            experimentStatistics.setCompletedExperiments(completedExperiments);

            List<ExperimentSummaryModel> failedExperiments = getExperimentStatisticsForState(ExperimentState.FAILED, gatewayId,
                    fromDate, toDate, userName, applicationName, resourceHostName);
            experimentStatistics.setFailedExperimentCount(failedExperiments.size());
            experimentStatistics.setFailedExperiments(failedExperiments);

            List<ExperimentSummaryModel> cancelledExperiments = getExperimentStatisticsForState(ExperimentState.CANCELED, gatewayId,
                    fromDate, toDate, userName, applicationName, resourceHostName);
            cancelledExperiments.addAll(getExperimentStatisticsForState(ExperimentState.CANCELING, gatewayId,
                    fromDate, toDate, userName, applicationName, resourceHostName));
            experimentStatistics.setCancelledExperimentCount(cancelledExperiments.size());
            experimentStatistics.setCancelledExperiments(cancelledExperiments);

            return experimentStatistics;
        }

        catch (RegistryException e) {
            logger.error("Error while retrieving experiment statistics from registry", e);
            throw new RegistryException(e);
        }

    }

    protected List<ExperimentSummaryModel> getExperimentStatisticsForState(ExperimentState experimentState, String gatewayId, Timestamp fromDate, Timestamp toDate,
                                                                           String userName, String applicationName, String resourceHostName) throws RegistryException {

        String query = "SELECT ES FROM " + ExperimentSummaryEntity.class.getSimpleName() + " ES WHERE ";
        Map<String, Object> queryParameters = new HashMap<>();

        if (experimentState != null) {
            logger.debug("Filter Experiments by Experiment State");
            queryParameters.put(DBConstants.Experiment.EXPERIMENT_STATE, experimentState);
            query += "ES.state LIKE :" + DBConstants.Experiment.EXPERIMENT_STATE + " AND ";
        }

        if (gatewayId != null) {
            logger.debug("Filter Experiments by GatewayId");
            queryParameters.put(DBConstants.Experiment.GATEWAY_ID, gatewayId);
            query += "ES.gatewayId LIKE :" + DBConstants.Experiment.GATEWAY_ID + " AND ";
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
            query += "ES.userName LIKE :" + DBConstants.Experiment.USER_NAME + " AND ";
        }

        if (applicationName != null) {
            logger.debug("Filter Experiments by ApplicationName");
            queryParameters.put(DBConstants.Experiment.EXECUTION_ID, applicationName);
            query += "ES.executionId LIKE :" + DBConstants.Experiment.EXECUTION_ID + " AND ";
        }

        if (resourceHostName != null) {
            logger.debug("Filter Experiments by ResourceHostName");
            queryParameters.put(DBConstants.Experiment.RESOURCE_HOST_ID, resourceHostName);
            query += "ES.resourceHostId LIKE :" + DBConstants.Experiment.RESOURCE_HOST_ID + " ";
        }

        else {
            logger.debug("Removing the last operator from the query");
            query = query.substring(0, query.length() - 4);
        }

        query += "ORDER BY ES.creationTime DESC";
        List<ExperimentSummaryModel> experimentSummaryModelList = select(query, -1, 0, queryParameters);
        return experimentSummaryModelList;
    }

}
