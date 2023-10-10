package org.apache.airavata.integration.clients;

import org.apache.airavata.api.Airavata;
import org.apache.airavata.common.utils.ThriftClientPool;
import org.apache.airavata.integration.utils.ClientFactory;
import org.apache.airavata.integration.utils.ConnectorUtils;
import org.apache.airavata.integration.utils.Constants;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.data.replica.DataProductModel;
import org.apache.airavata.model.data.replica.DataProductType;
import org.apache.airavata.model.data.replica.DataReplicaLocationModel;
import org.apache.airavata.model.data.replica.ReplicaLocationCategory;
import org.apache.airavata.model.error.AiravataClientException;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.ExperimentType;
import org.apache.airavata.model.experiment.UserConfigurationDataModel;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.model.status.ExperimentStatus;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/***
 * Airavata api operations
 */
public class AiravataAPIClient extends Connector {

    private static Logger LOGGER = LoggerFactory.getLogger(AiravataAPIClient.class);

    private ThriftClientPool<Airavata.Client> airavataClientPool;

    private String host;
    private int port;

    public AiravataAPIClient(String fileName) throws IOException, AiravataClientException {
        super(fileName);
        this.host = getProperties().getProperty(Constants.API_SERVER_HOST);
        this.port = Integer.parseInt(getProperties().getProperty(Constants.API_SERVER_PORT));

        airavataClientPool = new ThriftClientPool<>(
                new ClientFactory(this.host, this.port,
                        getProperties().getProperty(Constants.TRUST_STORE_PATH),
                        getProperties().getProperty(Constants.TRUST_STORE_PASSWORD), 10000)
                ,
                ConnectorUtils.<Airavata.Client>createGenericObjectPoolConfig(),
                host,
                port);
    }


    private ExperimentModel createExperimentDataModelForSingleApplication(AuthzToken token, String experimentName, String description) throws TException {
        try {
            Airavata.Client client = airavataClientPool.getResource();

            String executionId = getProperties().getProperty(Constants.APP_EXECUTION_ID);
            String projectId = getProperties().getProperty(Constants.PROJECT_ID);
            String gatewayId = getProperties().getProperty(Constants.GATEWAY_ID);
            ExperimentModel experimentModel = new ExperimentModel();
            experimentModel.setExperimentName(experimentName);
            experimentModel.setDescription(description);
            experimentModel.setExperimentType(ExperimentType.SINGLE_APPLICATION);
            experimentModel.setExecutionId(executionId);
            experimentModel.setGatewayId(gatewayId);
            experimentModel.setUserName(token.getClaimsMap().
                    get(org.apache.airavata.common.utils.Constants.USER_NAME));
            experimentModel.setProjectId(projectId);

            return experimentModel;

        } catch (Exception ex) {
            LOGGER.error("Error occurred while creating data model  for experiment " + experimentName, ex);
            throw ex;
        }
    }


    private UserConfigurationDataModel configureComputationResourceScheduling(String experimentDataDir) throws TException {
        try {

            ComputationalResourceSchedulingModel computationalResourceSchedulingModel = new ComputationalResourceSchedulingModel();
            String resourceHostId = getProperties().getProperty(Constants.RESOURCE_HOST_ID);
            String nodeCount = getProperties().getProperty(Constants.NODE_COUNT);
            String totalCPUCount = getProperties().getProperty(Constants.TOTAL_CPU_COUNT);
            String queueName = getProperties().getProperty(Constants.QUEUE_NAME);
            String wallTimeLimit = getProperties().getProperty(Constants.WALL_TIME_LIMIT);
            String groupResourceProfileId = getProperties().getProperty(Constants.GROUP_RESOURCE_PROFILE_ID);
            String storageId = getProperties().getProperty(Constants.STORAGE_RESOURCE_ID);

            computationalResourceSchedulingModel.setResourceHostId(resourceHostId);
            computationalResourceSchedulingModel.setNodeCount(Integer.parseInt(nodeCount));
            computationalResourceSchedulingModel.setTotalCPUCount(Integer.parseInt(totalCPUCount));
            computationalResourceSchedulingModel.setQueueName(queueName);
            computationalResourceSchedulingModel.setWallTimeLimit(Integer.parseInt(wallTimeLimit));


            UserConfigurationDataModel userConfigurationDataModel = new UserConfigurationDataModel();
            userConfigurationDataModel.setComputationalResourceScheduling(computationalResourceSchedulingModel);
            userConfigurationDataModel.setGroupResourceProfileId(groupResourceProfileId);
            userConfigurationDataModel.setStorageId(storageId);
            userConfigurationDataModel.setExperimentDataDir(experimentDataDir);
            userConfigurationDataModel.setAiravataAutoSchedule(false);
            userConfigurationDataModel.setOverrideManualScheduledParams(true);

            return userConfigurationDataModel;
        } catch (Exception ex) {
            LOGGER.error("Error occurred while creating computation resource scheduling model ", ex);
            throw ex;
        }

    }

    private String registerInputFile(AuthzToken token, String inputFileName, String uploadedStoragePath) throws TException {
        Airavata.Client client = airavataClientPool.getResource();
        try {

            String gatewayId = getProperties().getProperty(Constants.GATEWAY_ID);
            String username = token.getClaimsMap().
                    get(org.apache.airavata.common.utils.Constants.USER_NAME);
            String storageId = getProperties().getProperty(Constants.STORAGE_RESOURCE_ID);
            String storageName = getProperties().getProperty(Constants.STORAGE_NAME);
            DataProductModel dataProductModel = new DataProductModel();
            dataProductModel.setGatewayId(gatewayId);
            dataProductModel.setOwnerName(username);
            dataProductModel.setProductName(inputFileName);
            dataProductModel.setDataProductType(DataProductType.FILE);

            DataReplicaLocationModel dataReplicaLocationModel = new DataReplicaLocationModel();
            dataReplicaLocationModel.setStorageResourceId(storageId);

            String fileName = inputFileName + " gateway data store copy";

            dataReplicaLocationModel.setReplicaName(fileName);
            dataReplicaLocationModel.setReplicaLocationCategory(ReplicaLocationCategory.GATEWAY_DATA_STORE);
            String fullUploadedPath = uploadedStoragePath + inputFileName;
            String filePath = "file://" + storageName + ":" + fullUploadedPath;
            dataReplicaLocationModel.setFilePath(filePath);

            dataProductModel.addToReplicaLocations(dataReplicaLocationModel);


            String uri = client.registerDataProduct(token, dataProductModel);
            airavataClientPool.returnResource(client);
            return uri;

        } catch (Exception ex) {
            airavataClientPool.returnBrokenResource(client);
            LOGGER.error("Error occurred while registering input file: " + inputFileName, ex);
            throw ex;
        }

    }


    private ExperimentModel configureInputAndOutputs(AuthzToken token, ExperimentModel experimentModel, String[] inputFiles) throws TException {
        Airavata.Client client = airavataClientPool.getResource();
        try {

            String executionId = getProperties().getProperty(Constants.APP_EXECUTION_ID);
            List<InputDataObjectType> inputDataObjectTypes = client.getApplicationInputs(token, executionId);
            AtomicInteger count = new AtomicInteger();
            inputDataObjectTypes.forEach(obj -> {
                obj.setValue(inputFiles[0]);
                count.getAndIncrement();
            });

            experimentModel.setExperimentInputs(inputDataObjectTypes);
            List<OutputDataObjectType> outputs = client.getApplicationOutputs(token, executionId);
            experimentModel.setExperimentOutputs(outputs);
            airavataClientPool.returnResource(client);
            return experimentModel;

        } catch (Exception ex) {
            airavataClientPool.returnBrokenResource(client);
            LOGGER.error("Error occurred while configuring  input and output files: ", ex);
            throw ex;
        }
    }


    public String createExperiment(AuthzToken token, String experimentName, String description, String experimentDataDir,
                                   String inputFileName, String uploadedStoragePath) throws TException {
        Airavata.Client client = airavataClientPool.getResource();

        System.out.println(client.getAPIVersion());
        try {
            String gatewayId = getProperties().getProperty(Constants.GATEWAY_ID);

            ExperimentModel experimentModel = createExperimentDataModelForSingleApplication(token, experimentName, description);

            UserConfigurationDataModel userConfigurationDataModel = configureComputationResourceScheduling(experimentDataDir);

            experimentModel.setUserConfigurationData(userConfigurationDataModel);

            String dataFile = registerInputFile(token, inputFileName, uploadedStoragePath);

            ExperimentModel experimentM = configureInputAndOutputs(token, experimentModel, new String[]{dataFile});
            String expId = client.createExperiment(token, gatewayId, experimentM);
            airavataClientPool.returnResource(client);
            return expId;
        } catch (Exception ex) {
            airavataClientPool.returnBrokenResource(client);
            LOGGER.error("Error occurred while creating experiment : " + experimentName, ex);
            throw ex;
        }
    }


    public void launchExperiment(AuthzToken token, String experimentId) throws TException {
        Airavata.Client client = airavataClientPool.getResource();
        try {
            String gatewayId = getProperties().getProperty(Constants.GATEWAY_ID);
            client.launchExperiment(token, experimentId, gatewayId);
            airavataClientPool.returnResource(client);
        } catch (Exception ex) {
            airavataClientPool.returnBrokenResource(client);
            LOGGER.error("Error occurred while launching experiment : " + experimentId, ex);
            throw ex;

        }

    }

    public ExperimentStatus getExperimentStatus(AuthzToken token, String experimentId) throws TException {
        Airavata.Client client = airavataClientPool.getResource();
        try {
            return client.getExperimentStatus(token, experimentId);
        } catch (Exception ex) {
            airavataClientPool.returnBrokenResource(client);
            LOGGER.error("Error occurred while launching experiment : " + experimentId, ex);
            throw ex;
        } finally {
            airavataClientPool.returnResource(client);
        }
    }


}
