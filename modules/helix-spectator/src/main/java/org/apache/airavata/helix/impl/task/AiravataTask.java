package org.apache.airavata.helix.impl.task;

import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.helix.core.AbstractTask;
import org.apache.airavata.helix.core.OutPort;
import org.apache.airavata.helix.task.api.annotation.TaskOutPort;
import org.apache.airavata.helix.task.api.annotation.TaskParam;
import org.apache.airavata.messaging.core.MessageContext;
import org.apache.airavata.messaging.core.Publisher;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionInterface;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionProtocol;
import org.apache.airavata.model.appcatalog.gatewayprofile.ComputeResourcePreference;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserComputeResourcePreference;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserResourceProfile;
import org.apache.airavata.model.messaging.event.MessageType;
import org.apache.airavata.model.messaging.event.TaskIdentifier;
import org.apache.airavata.model.messaging.event.TaskStatusChangeEvent;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.status.TaskState;
import org.apache.airavata.model.status.TaskStatus;
import org.apache.airavata.registry.core.experiment.catalog.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.*;
import org.apache.helix.HelixManager;
import org.apache.helix.task.TaskResult;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.*;

public abstract class AiravataTask extends AbstractTask {

    private static final Logger logger = LogManager.getLogger(AiravataTask.class);

    private AppCatalog appCatalog;
    private ExperimentCatalog experimentCatalog;
    private Publisher statusPublisher;
    private ProcessModel processModel;

    private ComputeResourceDescription computeResourceDescription;
    private ComputeResourcePreference gatewayComputeResourcePreference;
    private UserComputeResourcePreference userComputeResourcePreference;
    private UserResourceProfile userResourceProfile;
    private GatewayResourceProfile gatewayResourceProfile;

    @TaskParam(name = "Process Id")
    private String processId;

    @TaskParam(name = "experimentId")
    private String experimentId;

    @TaskParam(name = "gatewayId")
    private String gatewayId;

    @TaskOutPort(name = "Success Port")
    private OutPort onSuccess;


    protected TaskResult onSuccess(String message) {
        String successMessage = "Task " + getTaskId() + " completed." + message != null ? " Message : " + message : "";
        logger.info(successMessage);
        return onSuccess.invoke(new TaskResult(TaskResult.Status.COMPLETED, message));
    }

    protected TaskResult onFail(String reason, boolean fatal, Throwable error) {
        String errorMessage;

        if (error == null) {
            errorMessage = "Task " + getTaskId() + " failed due to " + reason;
            logger.error(errorMessage);
        } else {
            errorMessage = "Task " + getTaskId() + " failed due to " + reason + ", " + error.getMessage();
            logger.error(errorMessage, error);
        }
        return new TaskResult(fatal ? TaskResult.Status.FATAL_FAILED : TaskResult.Status.FAILED, errorMessage);

    }

    @Override
    public void init(HelixManager manager, String workflowName, String jobName, String taskName) {
        super.init(manager, workflowName, jobName, taskName);
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            experimentCatalog = RegistryFactory.getDefaultExpCatalog();
            processModel = (ProcessModel) experimentCatalog.get(ExperimentCatalogModelType.PROCESS, processId);

            this.computeResourceDescription = getAppCatalog().getComputeResource().getComputeResource(getProcessModel()
                    .getComputeResourceId());
            this.gatewayComputeResourcePreference = getAppCatalog().getGatewayProfile()
                    .getComputeResourcePreference(getGatewayId(), computeResourceDescription.getComputeResourceId());

            this.userComputeResourcePreference = getAppCatalog().getUserResourceProfile()
                    .getUserComputeResourcePreference(getProcessModel().getUserName(), getGatewayId(), getProcessModel()
                            .getComputeResourceId());

            this.userResourceProfile = getAppCatalog().getUserResourceProfile()
                    .getUserResourceProfile(getProcessModel().getUserName(), getGatewayId());

            this.gatewayResourceProfile = getAppCatalog().getGatewayProfile().getGatewayProfile(getGatewayId());

        } catch (AppCatalogException e) {
            e.printStackTrace();
        } catch (RegistryException e) {
            e.printStackTrace();
        }
    }

    protected AppCatalog getAppCatalog() {
        return appCatalog;
    }

    protected void publishTaskState(TaskState ts) throws RegistryException {

        TaskStatus taskStatus = new TaskStatus();
        taskStatus.setState(ts);
        taskStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
        experimentCatalog.add(ExpCatChildDataType.TASK_STATUS, taskStatus, getTaskId());
        TaskIdentifier identifier = new TaskIdentifier(getTaskId(),
                getProcessId(), getExperimentId(), getGatewayId());
        TaskStatusChangeEvent taskStatusChangeEvent = new TaskStatusChangeEvent(ts,
                identifier);
        MessageContext msgCtx = new MessageContext(taskStatusChangeEvent, MessageType.TASK, AiravataUtils.getId
                (MessageType.TASK.name()), getGatewayId());
        msgCtx.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
    }


    ///////////////////

    public String getComputeResourceId() {
        if (isUseUserCRPref() &&
                userComputeResourcePreference != null &&
                isValid(userComputeResourcePreference.getComputeResourceId())) {
            return userComputeResourcePreference.getComputeResourceId();
        } else {
            return gatewayComputeResourcePreference.getComputeResourceId();
        }
    }

    public String getComputeResourceCredentialToken(){
        if (isUseUserCRPref()) {
            if (userComputeResourcePreference != null &&
                    isValid(userComputeResourcePreference.getResourceSpecificCredentialStoreToken())) {
                return userComputeResourcePreference.getResourceSpecificCredentialStoreToken();
            } else {
                return userResourceProfile.getCredentialStoreToken();
            }
        } else {
            if (isValid(gatewayComputeResourcePreference.getResourceSpecificCredentialStoreToken())) {
                return gatewayComputeResourcePreference.getResourceSpecificCredentialStoreToken();
            } else {
                return gatewayResourceProfile.getCredentialStoreToken();
            }
        }
    }

    public String getComputeResourceLoginUserName(){
        if (isUseUserCRPref() &&
                userComputeResourcePreference != null &&
                isValid(userComputeResourcePreference.getLoginUserName())) {
            return userComputeResourcePreference.getLoginUserName();
        } else if (isValid(getProcessModel().getProcessResourceSchedule().getOverrideLoginUserName())) {
            return getProcessModel().getProcessResourceSchedule().getOverrideLoginUserName();
        } else {
            return gatewayComputeResourcePreference.getLoginUserName();
        }
    }

    public JobSubmissionInterface getPreferredJobSubmissionInterface() throws AppCatalogException {
        try {
            JobSubmissionProtocol preferredJobSubmissionProtocol = getJobSubmissionProtocol();
            ComputeResourceDescription resourceDescription = getComputeResourceDescription();
            List<JobSubmissionInterface> jobSubmissionInterfaces = resourceDescription.getJobSubmissionInterfaces();
            Map<JobSubmissionProtocol, List<JobSubmissionInterface>> orderedInterfaces = new HashMap<>();
            List<JobSubmissionInterface> interfaces = new ArrayList<>();
            if (jobSubmissionInterfaces != null && !jobSubmissionInterfaces.isEmpty()) {
                for (JobSubmissionInterface submissionInterface : jobSubmissionInterfaces){

                    if (preferredJobSubmissionProtocol != null){
                        if (preferredJobSubmissionProtocol.toString().equals(submissionInterface.getJobSubmissionProtocol().toString())){
                            if (orderedInterfaces.containsKey(submissionInterface.getJobSubmissionProtocol())){
                                List<JobSubmissionInterface> interfaceList = orderedInterfaces.get(submissionInterface.getJobSubmissionProtocol());
                                interfaceList.add(submissionInterface);
                            }else {
                                interfaces.add(submissionInterface);
                                orderedInterfaces.put(submissionInterface.getJobSubmissionProtocol(), interfaces);
                            }
                        }
                    }else {
                        Collections.sort(jobSubmissionInterfaces, new Comparator<JobSubmissionInterface>() {
                            @Override
                            public int compare(JobSubmissionInterface jobSubmissionInterface, JobSubmissionInterface jobSubmissionInterface2) {
                                return jobSubmissionInterface.getPriorityOrder() - jobSubmissionInterface2.getPriorityOrder();
                            }
                        });
                    }
                }
                interfaces = orderedInterfaces.get(preferredJobSubmissionProtocol);
                Collections.sort(interfaces, new Comparator<JobSubmissionInterface>() {
                    @Override
                    public int compare(JobSubmissionInterface jobSubmissionInterface, JobSubmissionInterface jobSubmissionInterface2) {
                        return jobSubmissionInterface.getPriorityOrder() - jobSubmissionInterface2.getPriorityOrder();
                    }
                });
            } else {
                throw new AppCatalogException("Compute resource should have at least one job submission interface defined...");
            }
            return interfaces.get(0);
        } catch (AppCatalogException e) {
            throw new AppCatalogException("Error occurred while retrieving data from app catalog", e);
        }
    }

    //////////////////////////


    protected boolean isValid(String str) {
        return str != null && !str.trim().isEmpty();
    }

    public boolean isUseUserCRPref() {
        return getProcessModel().isUseUserCRPref();
    }

    public JobSubmissionProtocol getJobSubmissionProtocol() {
        return getGatewayComputeResourcePreference().getPreferredJobSubmissionProtocol();
    }

    public ComputeResourcePreference getGatewayComputeResourcePreference() {
        return gatewayComputeResourcePreference;
    }


    public ComputeResourceDescription getComputeResourceDescription() {
        return computeResourceDescription;
    }

    ////////////////////////

    
    public void setAppCatalog(AppCatalog appCatalog) {
        this.appCatalog = appCatalog;
    }

    public ExperimentCatalog getExperimentCatalog() {
        return experimentCatalog;
    }

    public void setExperimentCatalog(ExperimentCatalog experimentCatalog) {
        this.experimentCatalog = experimentCatalog;
    }

    public Publisher getStatusPublisher() {
        return statusPublisher;
    }

    public void setStatusPublisher(Publisher statusPublisher) {
        this.statusPublisher = statusPublisher;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public ProcessModel getProcessModel() {
        return processModel;
    }

    public void setProcessModel(ProcessModel processModel) {
        this.processModel = processModel;
    }
}
