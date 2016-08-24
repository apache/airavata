package org.airavata.xbaya.connectors.airavata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.airavata.xbaya.util.XbayaContext;
import org.apache.airavata.api.Airavata;
import org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.error.AiravataClientException;
import org.apache.airavata.model.error.AiravataErrorType;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.ExperimentSearchFields;
import org.apache.airavata.model.experiment.ExperimentSummaryModel;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.model.status.JobStatus;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

public class AiravataManager {

    private static AiravataManager instance;

    private Airavata.Client airavataClient;

    private AiravataCache<String, Object> airavataCache;

    private AiravataManager() throws AiravataClientException {
        try {
            this.airavataClient = createAiravataClient();
            this.airavataCache = new AiravataCache<>(200, 500, 50);

            //FIXME - To create the default user & project if not exists
        } catch (Exception e) {
            throw new AiravataClientException(AiravataErrorType.UNKNOWN);
        }
    }

    public static AiravataManager getInstance() throws AiravataClientException {
        if (AiravataManager.instance == null) {
            AiravataManager.instance = new AiravataManager();
        }
        return AiravataManager.instance;
    }

    private Airavata.Client createAiravataClient() throws TTransportException {
        String host = XbayaContext.getInstance().getAiravataHost();
        int port = XbayaContext.getInstance().getAiravataPort();
        TTransport transport = new TSocket(host, port);
        transport.open();
        TProtocol protocol = new TBinaryProtocol(transport);
        return new Airavata.Client(protocol);
    }

    private Airavata.Client getClient() throws AiravataClientException, TTransportException {
        try{
            airavataClient.getAPIVersion(getAuthzToken());
        } catch (Exception e) {
            airavataClient = createAiravataClient();
        }
        return airavataClient;
    }

    private AuthzToken getAuthzToken() {
        return new AuthzToken(XbayaContext.getInstance().getOAuthToken());
    }

    private String getGatewayId() {
        return XbayaContext.getInstance().getAiravataGatewayId();
    }

    private String getUserName() {
        return XbayaContext.getInstance().getUserName();
    }

    public synchronized List<ExperimentSummaryModel> getExperimentSummaries(Map<ExperimentSearchFields, String> filters,
                                                                            int limit, int offset) throws TException {
        List<ExperimentSummaryModel> exp = getClient().searchExperiments(
                getAuthzToken(), getGatewayId(), getUserName(), filters, limit, offset);
        return exp;
    }

    public synchronized List<ExperimentSummaryModel> getExperimentSummariesInProject(String projectId) throws TException {
        List<ExperimentSummaryModel> exp;
        Map<ExperimentSearchFields, String> filters = new HashMap<>();
        filters.put(ExperimentSearchFields.PROJECT_ID, projectId);
        exp = getClient().searchExperiments(
                getAuthzToken(), getGatewayId(), getUserName(), filters, -1, 0);
        return exp;
    }

    public synchronized List<ExperimentSummaryModel> getRecentExperimentSummaries(int limit) throws TException {
        List<ExperimentSummaryModel> exp;

        Map<ExperimentSearchFields, String> filters = new HashMap<>();
        exp = getClient().searchExperiments(
                getAuthzToken(), getGatewayId(), getUserName(), filters, limit, 0);
        return exp;
    }




    public synchronized ExperimentModel getExperiment(String experimentId) throws TException {
        return getClient().getExperiment(getAuthzToken(), experimentId);
    }

    public synchronized ComputeResourceDescription getComputeResource(String resourceId) throws TException {
        ComputeResourceDescription computeResourceDescription;

        if (airavataCache.get(resourceId) != null) {
            computeResourceDescription = (ComputeResourceDescription) airavataCache.get(resourceId);
        } else {
            computeResourceDescription = getClient().getComputeResource(getAuthzToken(), resourceId);
            airavataCache.put(resourceId, computeResourceDescription);
        }
        return computeResourceDescription;
    }

    public synchronized ApplicationInterfaceDescription getApplicationInterface(String interfaceId) throws TException {
        ApplicationInterfaceDescription applicationInterfaceDescription = null;

        if (airavataCache.get(interfaceId) != null) {
            applicationInterfaceDescription = (ApplicationInterfaceDescription) airavataCache.get(interfaceId);
        } else {
            applicationInterfaceDescription = getClient().getApplicationInterface(getAuthzToken(), interfaceId);
            airavataCache.put(interfaceId, applicationInterfaceDescription);
        }

        return applicationInterfaceDescription;
    }


    public synchronized Map<String, JobStatus> getJobStatuses(String expId) throws TException {
        Map<String, JobStatus> jobStatuses;
        jobStatuses = getClient().getJobStatuses(getAuthzToken(), expId);
        return jobStatuses;
    }

    public synchronized List<ApplicationInterfaceDescription> getAllApplicationInterfaces() throws TException {
        List<ApplicationInterfaceDescription> allApplicationInterfaces;
        allApplicationInterfaces = getClient().getAllApplicationInterfaces(getAuthzToken(), getGatewayId());
        Collections.sort(allApplicationInterfaces, (o1, o2) -> o1.getApplicationName().compareTo(o2.getApplicationName()));
        return allApplicationInterfaces;
    }

    public synchronized List<ComputeResourceDescription> getAvailableComputeResourcesForApp(String applicationInterfaceId)
            throws TException {
        List<ComputeResourceDescription> availableComputeResources;
        Map<String, String> temp = getClient().getAvailableAppInterfaceComputeResources(getAuthzToken(), applicationInterfaceId);
        availableComputeResources = new ArrayList<>();
        for (String resourceId : temp.keySet()) {
            availableComputeResources.add(getComputeResource(resourceId));
        }
        return availableComputeResources;
    }

    public synchronized String createExperiment(ExperimentModel experimentModel) throws TException {
        return getClient().createExperiment(getAuthzToken(), getGatewayId(), experimentModel);
    }

    public synchronized void updateExperiment(ExperimentModel experimentModel) throws TException {
        getClient().updateExperiment(getAuthzToken(), experimentModel.getExperimentId(), experimentModel);
    }

    public synchronized void launchExperiment(String experimentId) throws TException {
        getClient().launchExperiment(getAuthzToken(), experimentId, getGatewayId());
    }

    public synchronized void deleteExperiment(String experimentId) throws TException {
        getClient().deleteExperiment(getAuthzToken(), experimentId);
    }

    public synchronized void cancelExperiment(String experimentId) throws TException {
        getClient().terminateExperiment(getAuthzToken(),experimentId, getGatewayId());
    }

    public synchronized String cloneExperiment(String experimentId, String newExpName) throws TException {
        return getClient().cloneExperiment(getAuthzToken(), experimentId, newExpName);
    }
}
