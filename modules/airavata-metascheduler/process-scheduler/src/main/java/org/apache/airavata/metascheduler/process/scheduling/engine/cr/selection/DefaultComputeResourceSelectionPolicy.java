package org.apache.airavata.metascheduler.process.scheduling.engine.cr.selection;

import org.apache.airavata.agents.api.AgentAdaptor;
import org.apache.airavata.agents.api.CommandOutput;
import org.apache.airavata.helix.core.support.adaptor.AdaptorSupportImpl;
import org.apache.airavata.metascheduler.core.adaptor.output.OutputParser;
import org.apache.airavata.metascheduler.process.scheduling.engine.output.OutputParserImpl;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionInterface;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionProtocol;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.UserConfigurationDataModel;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.registry.api.RegistryService.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * This class implements selecting compute resource defined in USER_CONFIGURATION_DATA and assumes only one
 * compute resource is selected for experiment.
 * This checks whether defined CR is live and schedulable
 */
public class DefaultComputeResourceSelectionPolicy extends ComputeResourceSelectionPolicyImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultComputeResourceSelectionPolicy.class);

    @Override
    public Optional<ComputationalResourceSchedulingModel> selectComputeResource(String processId) {
        final RegistryService.Client registryClient = this.registryClientPool.getResource();
        try {
            ProcessModel processModel = registryClient.getProcess(processId);

            ExperimentModel experiment = registryClient.getExperiment(processModel.getExperimentId());


            UserConfigurationDataModel userConfigurationDataModel = experiment.getUserConfigurationData();

            // Assume scheduling data is populated in USER_CONFIGURATION_DATA_MODEL
            ComputationalResourceSchedulingModel computationalResourceSchedulingModel = userConfigurationDataModel
                    .getComputationalResourceScheduling();

            String computeResourceId = computationalResourceSchedulingModel.getResourceHostId();

            ComputeResourceDescription comResourceDes = registryClient.getComputeResource(computeResourceId);

            List<JobSubmissionInterface> jobSubmissionInterfaces = comResourceDes.getJobSubmissionInterfaces();
            Collections.sort(jobSubmissionInterfaces, Comparator.comparingInt(JobSubmissionInterface::getPriorityOrder));
            JobSubmissionProtocol jobSubmissionProtocol = jobSubmissionInterfaces.get(0).getJobSubmissionProtocol();

            AdaptorSupportImpl adaptorSupport = AdaptorSupportImpl.getInstance();

            String computeResourceToken = getComputeResourceCredentialToken(
                    experiment.getGatewayId(),
                    processModel.getUserName(),
                    computeResourceId,
                    processModel.isUseUserCRPref(),
                    processModel.isSetGroupResourceProfileId(),
                    processModel.getGroupResourceProfileId());

            String loginUsername = getComputeResourceLoginUserName(experiment.getGatewayId(),
                    processModel.getUserName(),
                    computeResourceId,
                    processModel.isUseUserCRPref(),
                    processModel.isSetGroupResourceProfileId(),
                    processModel.getGroupResourceProfileId(),
                    computationalResourceSchedulingModel.getOverrideLoginUserName());

            AgentAdaptor adaptor = adaptorSupport.fetchAdaptor(experiment.getGatewayId(),
                    computeResourceId,
                    jobSubmissionProtocol,
                    computeResourceToken,
                    loginUsername);


            String command = "";
            String workingDirectory = "";
            CommandOutput commandOutput = adaptor.executeCommand(command, workingDirectory);

            OutputParser outputParser = new OutputParserImpl();
            if (outputParser.isComputeResourceAvailable(commandOutput)) {
                return Optional.of(computationalResourceSchedulingModel);
            }

        } catch (Exception exception) {
            LOGGER.error(" Exception occurred while scheduling Process with Id {}", processId, exception);
        } finally {
            this.registryClientPool.returnResource(registryClient);
        }
        return Optional.empty();
    }
}
