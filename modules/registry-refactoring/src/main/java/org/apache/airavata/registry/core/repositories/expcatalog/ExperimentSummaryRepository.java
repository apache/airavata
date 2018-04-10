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
        String query = "SELECT DISTINCT ES FROM " + ExperimentSummaryEntity.class.getSimpleName() + " ES ";

        if (!filters.isEmpty() && filters != null) {

            for (String field : filters.keySet()) {

                query += " WHERE ";

                if (field.equals(DBConstants.Experiment.USER_NAME)) {
                    logger.debug("Filter Experiments by User");
                    query += "ES.userName LIKE :" + filters.get(field) + " AND ";
                }

                else if (field.equals(DBConstants.Experiment.GATEWAY_ID)) {
                    logger.debug("Filter Experiments by Gateway ID");
                    query += "ES.gatewayId LIKE :" + filters.get(field) + " AND ";
                }

                else if (field.equals(DBConstants.Experiment.PROJECT_ID)) {
                    logger.debug("Filter Experiments by Project ID");
                    query += "ES.projectId LIKE :" + filters.get(field) + " AND ";
                }

                else if (field.equals(DBConstants.Experiment.EXPERIMENT_NAME)) {
                    logger.debug("Filter Experiments by Name");
                    query += "ES.name LIKE :" + filters.get(field) + " AND ";
                }

                else if (field.equals(DBConstants.Experiment.DESCRIPTION)) {
                    logger.debug("Filter Experiments by Description");
                    query += "ES.description LIKE :" + filters.get(field) + " AND ";
                }

                else if (field.equals(DBConstants.Experiment.EXECUTION_ID)) {
                    logger.debug("Filter Experiments by Execution ID");
                    query += "ES.executionId LIKE :" + filters.get(field) + " AND ";
                }

            }

        }

        if (filters.get(DBConstants.ExperimentStatus.STATE) != null) {
            logger.debug("Filter Experiments by State");
            String state = ExperimentState.valueOf(filters.get(DBConstants.ExperimentStatus.STATE)).toString();
            query += "ES.state LIKE :" + state + " AND ";
        }

        if (filters.get(DBConstants.ExperimentSummary.FROM_DATE) != null
                && filters.get(DBConstants.ExperimentSummary.TO_DATE) != null) {

            Timestamp fromDate = new Timestamp(Long.parseLong(filters.get(DBConstants.ExperimentSummary.FROM_DATE)));
            Timestamp toDate = new Timestamp(Long.parseLong(filters.get(DBConstants.ExperimentSummary.TO_DATE)));

            if (toDate.after(fromDate)) {
                logger.debug("Filter Experiments by Date");
                query += "ES.creationTime BETWEEN :" + fromDate + " AND :" + toDate + " AND ";
            }

        }

        if (!accessibleExperimentIds.isEmpty() && accessibleExperimentIds != null) {
            logger.debug("Filter Projects by Accessible Experiment IDs");
            query += " ES.experimentId IN :" + accessibleExperimentIds;
        }

        else {
            logger.debug("Removing the last operator from the query");
            query = query.substring(0, query.length() - 5);
        }

        List<ExperimentSummaryModel> experimentSummaryModelList = select(query, offset);
        return experimentSummaryModelList;
    }

    public ExperimentStatistics getExperimentStatistics(Map<String,String> filters) throws RegistryException {

        try {

            ExperimentStatistics experimentStatistics = new ExperimentStatistics();
            String gatewayId = filters.get(DBConstants.Experiment.GATEWAY_ID);
            String userName = filters.get(DBConstants.Experiment.USER_NAME);
            String applicationName = filters.get(DBConstants.Experiment.EXECUTION_ID);
            String resourceHostName = filters.get(DBConstants.Experiment.RESOURCE_HOST_ID);
            Timestamp fromDate = new Timestamp(Long.parseLong(filters.get(DBConstants.ExperimentSummary.FROM_DATE)));
            Timestamp toDate = new Timestamp(Long.parseLong(filters.get(DBConstants.ExperimentSummary.TO_DATE)));

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

        String query = "SELECT DISTINCT ES FROM " + ExperimentSummaryEntity.class.getSimpleName() + " ES WHERE ";

        if (experimentState != null) {
            query += "ES.state LIKE :" + experimentState.toString() + " AND ";
        }

        query += "ES.creationTime BETWEEN :" + fromDate + " AND :" + toDate + " AND " +
                "ES.gatewayId LIKE :" + gatewayId + " ";

        if (userName != null) {
            query += "AND ES.userName LIKE :" + userName + " ";
        }

        if (applicationName != null) {
            query += "AND ES.executionId LIKE :" + applicationName + " ";
        }

        if (resourceHostName != null) {
            query += "AND ES.resourceHostId LIKE :" + resourceHostName + " ";
        }

        query += "ORDER BY ES.creationTime DESC";
        List<ExperimentSummaryModel> experimentSummaryModelList = select(query, 0);
        return experimentSummaryModelList;
    }

}
