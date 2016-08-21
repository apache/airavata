/**
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
 */
package org.apache.airavata.gfac.core;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.*;
import org.apache.airavata.credential.store.store.CredentialReader;
import org.apache.airavata.credential.store.store.impl.CredentialReaderImpl;
import org.apache.airavata.gfac.core.context.ProcessContext;
import org.apache.airavata.gfac.core.context.TaskContext;
import org.apache.airavata.messaging.core.MessageContext;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationDeploymentDescription;
import org.apache.airavata.model.appcatalog.appdeployment.CommandObject;
import org.apache.airavata.model.appcatalog.computeresource.*;
import org.apache.airavata.model.appcatalog.gatewayprofile.ComputeResourcePreference;
import org.apache.airavata.model.application.io.DataType;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.commons.ErrorModel;
import org.apache.airavata.model.data.replica.*;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.job.JobModel;
import org.apache.airavata.model.messaging.event.*;
import org.apache.airavata.model.parallelism.ApplicationParallelismType;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.status.*;
import org.apache.airavata.model.task.JobSubmissionTaskModel;
import org.apache.airavata.registry.core.experiment.catalog.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.*;
import org.apache.airavata.registry.cpi.utils.Constants;
import org.apache.commons.io.FileUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.ZKPaths;
import org.apache.thrift.TException;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.*;
import java.io.*;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import org.apache.airavata.commons.gfac.type.ActualParameter;

public class GFacUtils {
    private final static Logger log = LoggerFactory.getLogger(GFacUtils.class);
    public static final ArrayList<ACL> OPEN_ACL_UNSAFE = ZooDefs.Ids.OPEN_ACL_UNSAFE;

    private GFacUtils() {
    }

    /**
     * Read data from inputStream and convert it to String.
     *
     * @param in
     * @return String read from inputStream
     * @throws java.io.IOException
     */
    public static String readFromStream(InputStream in) throws IOException {
        try {
            StringBuffer wsdlStr = new StringBuffer();

            int read;

            byte[] buf = new byte[1024];
            while ((read = in.read(buf)) > 0) {
                wsdlStr.append(new String(buf, 0, read));
            }
            return wsdlStr.toString();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    log.warn("Cannot close InputStream: "
                            + in.getClass().getName(), e);
                }
            }
        }
    }

    /**
     * This will read
     *
     * @param maxWalltime
     * @return
     */
    public static String maxWallTimeCalculator(int maxWalltime) {
        if (maxWalltime < 60) {
            return "00:" + maxWalltime + ":00";
        } else {
            int minutes = maxWalltime % 60;
            int hours = maxWalltime / 60;
            return hours + ":" + minutes + ":00";
        }
    }

    public static String maxWallTimeCalculatorForLSF(int maxWalltime) {
        if (maxWalltime < 60) {
            return "00:" + maxWalltime;
        } else {
            int minutes = maxWalltime % 60;
            int hours = maxWalltime / 60;
            return hours + ":" + minutes;
        }
    }

    public static String readFileToString(String file)
            throws FileNotFoundException, IOException {
        BufferedReader instream = null;
        try {

            instream = new BufferedReader(new FileReader(file));
            StringBuffer buff = new StringBuffer();
            String temp = null;
            while ((temp = instream.readLine()) != null) {
                buff.append(temp);
                buff.append(GFacConstants.NEWLINE);
            }
            return buff.toString();
        } finally {
            if (instream != null) {
                try {
                    instream.close();
                } catch (IOException e) {
                    log.warn("Cannot close FileinputStream", e);
                }
            }
        }
    }

    public static boolean isLocalHost(String appHost)
            throws UnknownHostException {
        String localHost = InetAddress.getLocalHost().getCanonicalHostName();
        return (localHost.equals(appHost)
                || GFacConstants.LOCALHOST.equals(appHost) || GFacConstants._127_0_0_1
                .equals(appHost));
    }

    public static String createUniqueNameWithDate(String name) {
        String date = new Date().toString();
        date = date.replaceAll(" ", "_");
        date = date.replaceAll(":", "_");
        return name + "_" + date;
    }

    public static List<Element> getElementList(Document doc, String expression) throws XPathExpressionException {
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();
        XPathExpression expr = xPath.compile(expression);
        NodeList nodeList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
        List<Element> elementList = new ArrayList<Element>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node item = nodeList.item(i);
            if (item instanceof Element) {
                elementList.add((Element) item);
            }
        }
        return elementList;
    }

    public static String createGsiftpURIAsString(String host, String localPath)
            throws URISyntaxException {
        StringBuffer buf = new StringBuffer();
        if (!host.startsWith("gsiftp://"))
            buf.append("gsiftp://");
        buf.append(host);
        if (!host.endsWith("/"))
            buf.append("/");
        buf.append(localPath);
        return buf.toString();
    }

	public static void saveJobStatus(ProcessContext processContext, JobModel jobModel) throws GFacException {
		try {
            // first we save job jobModel to the registry for sa and then save the job status.
            JobStatus jobStatus = null;
            if(jobModel.getJobStatuses() != null)
			    jobStatus = jobModel.getJobStatuses().get(0);

            ExperimentCatalog experimentCatalog = processContext.getExperimentCatalog();
            List<JobStatus> statuses = new ArrayList<>();
            statuses.add(jobStatus);
            jobModel.setJobStatuses(statuses);
            if (jobStatus.getTimeOfStateChange() == 0 || jobStatus.getTimeOfStateChange() > 0 ){
                jobStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
            }else {
                jobStatus.setTimeOfStateChange(jobStatus.getTimeOfStateChange());
            }
            CompositeIdentifier ids = new CompositeIdentifier(jobModel.getTaskId(), jobModel.getJobId());
			experimentCatalog.add(ExpCatChildDataType.JOB_STATUS, jobStatus, ids);
            JobIdentifier identifier = new JobIdentifier(jobModel.getJobId(), jobModel.getTaskId(),
                    processContext.getProcessId(), processContext.getProcessModel().getExperimentId(),
                    processContext.getGatewayId());
            JobStatusChangeEvent jobStatusChangeEvent = new JobStatusChangeEvent(jobStatus.getJobState(), identifier);
			MessageContext msgCtx = new MessageContext(jobStatusChangeEvent, MessageType.JOB, AiravataUtils.getId
					(MessageType.JOB.name()), processContext.getGatewayId());
			msgCtx.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
			processContext.getStatusPublisher().publish(msgCtx);
        } catch (Exception e) {
			throw new GFacException("Error persisting job status"
					+ e.getLocalizedMessage(), e);
		}
	}

    public static void saveAndPublishTaskStatus(TaskContext taskContext) throws GFacException {
        try {
	        TaskState state = taskContext.getTaskState();
	        // first we save job jobModel to the registry for sa and then save the job status.
	        ProcessContext processContext = taskContext.getParentProcessContext();
	        ExperimentCatalog experimentCatalog = processContext.getExperimentCatalog();
	        TaskStatus status = taskContext.getTaskStatus();
            if (status.getTimeOfStateChange() == 0 || status.getTimeOfStateChange() > 0 ){
                status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
            }else {
                status.setTimeOfStateChange(status.getTimeOfStateChange());
            }
	        experimentCatalog.add(ExpCatChildDataType.TASK_STATUS, status, taskContext.getTaskId());
	        TaskIdentifier identifier = new TaskIdentifier(taskContext.getTaskId(),
			        processContext.getProcessId(), processContext.getProcessModel().getExperimentId(),
			        processContext.getGatewayId());
	        TaskStatusChangeEvent taskStatusChangeEvent = new TaskStatusChangeEvent(state,
			        identifier);
	        MessageContext msgCtx = new MessageContext(taskStatusChangeEvent, MessageType.TASK, AiravataUtils.getId
			        (MessageType.TASK.name()), taskContext.getParentProcessContext().getGatewayId());
	        msgCtx.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
	        processContext.getStatusPublisher().publish(msgCtx);
        } catch (Exception e) {
            throw new GFacException("Error persisting task status"
                    + e.getLocalizedMessage(), e);
        }
    }

    public static void saveAndPublishProcessStatus(ProcessContext processContext) throws GFacException {
        try {
            // first we save job jobModel to the registry for sa and then save the job status.
            ExperimentCatalog experimentCatalog = processContext.getExperimentCatalog();
            ProcessStatus status = processContext.getProcessStatus();
            if (status.getTimeOfStateChange() == 0 || status.getTimeOfStateChange() > 0 ){
                status.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
            }else {
                status.setTimeOfStateChange(status.getTimeOfStateChange());
            }
            experimentCatalog.add(ExpCatChildDataType.PROCESS_STATUS, status, processContext.getProcessId());
            ProcessIdentifier identifier = new ProcessIdentifier(processContext.getProcessId(),
                                                                 processContext.getProcessModel().getExperimentId(),
                                                                 processContext.getGatewayId());
            ProcessStatusChangeEvent processStatusChangeEvent = new ProcessStatusChangeEvent(status.getState(), identifier);
	        MessageContext msgCtx = new MessageContext(processStatusChangeEvent, MessageType.PROCESS,
			        AiravataUtils.getId(MessageType.PROCESS.name()), processContext.getGatewayId());
	        msgCtx.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
	        processContext.getStatusPublisher().publish(msgCtx);
        } catch (Exception e) {
            throw new GFacException("Error persisting process status"
                    + e.getLocalizedMessage(), e);
        }
    }

    private static void removeCancelDeliveryTagNode(String experimentPath, CuratorFramework curatorClient) throws Exception {
        Stat exists = curatorClient.checkExists().forPath(experimentPath + AiravataZKUtils.CANCEL_DELIVERY_TAG_POSTFIX);
        if (exists != null) {
            ZKPaths.deleteChildren(curatorClient.getZookeeperClient().getZooKeeper(), experimentPath + AiravataZKUtils.CANCEL_DELIVERY_TAG_POSTFIX, true);
        }
    }

    private static void copyChildren(CuratorFramework curatorClient, String oldPath, String newPath, int depth) throws Exception {
        for (String childNode : curatorClient.getChildren().forPath(oldPath)) {
            String oldChildPath = oldPath + File.separator + childNode;
            Stat stat = curatorClient.checkExists().forPath(oldChildPath); // no need to check exists
            String newChildPath = newPath + File.separator + childNode;
            log.info("Creating new znode: " + newChildPath);
            curatorClient.create().withMode(CreateMode.PERSISTENT).withACL(OPEN_ACL_UNSAFE)
                    .forPath(newChildPath, curatorClient.getData().storingStatIn(stat).forPath(oldChildPath));
            if (--depth > 0) {
                copyChildren(curatorClient, oldChildPath, newChildPath, depth);
            }
        }
    }

	// Fixme - remove this method. with new changes we don't need to use this method.
    public static boolean setExperimentCancelRequest(String processId, CuratorFramework curatorClient, long
		    deliveryTag) throws Exception {
	    String experimentNode = ZKPaths.makePath(ZkConstants.ZOOKEEPER_EXPERIMENT_NODE, processId);
	    String cancelListenerNodePath = ZKPaths.makePath(experimentNode, ZkConstants.ZOOKEEPER_CANCEL_LISTENER_NODE);
	    curatorClient.setData().withVersion(-1).forPath(cancelListenerNodePath, ZkConstants.ZOOKEEPER_CANCEL_REQEUST
			    .getBytes());
	    return true;
    }

    public static CredentialReader getCredentialReader()
            throws ApplicationSettingsException, IllegalAccessException,
            InstantiationException {
        try {
            String jdbcUrl = ServerSettings.getCredentialStoreDBURL();
            String jdbcUsr = ServerSettings.getCredentialStoreDBUser();
            String jdbcPass = ServerSettings.getCredentialStoreDBPassword();
            String driver = ServerSettings.getCredentialStoreDBDriver();
            return new CredentialReaderImpl(new DBUtil(jdbcUrl, jdbcUsr, jdbcPass,
                    driver));
        } catch (ClassNotFoundException e) {
            log.error("Not able to find driver: " + e.getLocalizedMessage());
            return null;
        }
    }

    public static LOCALSubmission getLocalJobSubmission(String submissionId) throws AppCatalogException {
        try {
            AppCatalog appCatalog = RegistryFactory.getAppCatalog();
            return appCatalog.getComputeResource().getLocalJobSubmission(submissionId);
        } catch (Exception e) {
            String errorMsg = "Error while retrieving local job submission with submission id : " + submissionId;
            log.error(errorMsg, e);
            throw new AppCatalogException(errorMsg, e);
        }
    }

    public static UnicoreJobSubmission getUnicoreJobSubmission(String submissionId) throws AppCatalogException {
        try {
            AppCatalog appCatalog = RegistryFactory.getAppCatalog();
            return appCatalog.getComputeResource().getUNICOREJobSubmission(submissionId);
        } catch (Exception e) {
            String errorMsg = "Error while retrieving UNICORE job submission with submission id : " + submissionId;
            log.error(errorMsg, e);
            throw new AppCatalogException(errorMsg, e);
        }
    }

    public static SSHJobSubmission getSSHJobSubmission(String submissionId) throws AppCatalogException {
        try {
            AppCatalog appCatalog = RegistryFactory.getAppCatalog();
            return appCatalog.getComputeResource().getSSHJobSubmission(submissionId);
        } catch (Exception e) {
            String errorMsg = "Error while retrieving SSH job submission with submission id : " + submissionId;
            log.error(errorMsg, e);
            throw new AppCatalogException(errorMsg, e);
        }
    }

    /**
     * To convert list to separated value
     *
     * @param listOfStrings
     * @param separator
     * @return
     */
    public static String listToCsv(List<String> listOfStrings, char separator) {
        StringBuilder sb = new StringBuilder();

        // all but last
        for (int i = 0; i < listOfStrings.size() - 1; i++) {
            sb.append(listOfStrings.get(i));
            sb.append(separator);
        }

        // last string, no separator
        if (listOfStrings.size() > 0) {
            sb.append(listOfStrings.get(listOfStrings.size() - 1));
        }

        return sb.toString();
    }

    public static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }

    public static long bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(bytes);
        buffer.flip();//need flip
        return buffer.getLong();
    }

    public static String getZKGfacServersParentPath() {
        return ZKPaths.makePath(ZkConstants.ZOOKEEPER_SERVERS_NODE, ZkConstants.ZOOKEEPER_GFAC_SERVER_NODE);
    }

    public static JobDescriptor createJobDescriptor(ProcessContext processContext, TaskContext taskContext)
            throws GFacException, AppCatalogException, ApplicationSettingsException {

        JobDescriptor jobDescriptor = new JobDescriptor();
        ProcessModel processModel = processContext.getProcessModel();
        ResourceJobManager resourceJobManager = getResourceJobManager(processContext);
        setMailAddresses(processContext, jobDescriptor); // set email options and addresses

        jobDescriptor.setInputDirectory(processContext.getInputDir());
        jobDescriptor.setOutputDirectory(processContext.getOutputDir());
        jobDescriptor.setExecutablePath(processContext.getApplicationDeploymentDescription().getExecutablePath());
        jobDescriptor.setStandardOutFile(processContext.getStdoutLocation());
        jobDescriptor.setStandardErrorFile(processContext.getStderrLocation());
        ComputeResourcePreference crp = getComputeResourcePreference(processContext);
        if (crp.getAllocationProjectNumber() != null) {
            jobDescriptor.setAcountString(crp.getAllocationProjectNumber());
        }
        jobDescriptor.setReservation(getReservation(crp));

        // To make job name alpha numeric
        jobDescriptor.setJobName("A" + String.valueOf(generateJobName()));
        jobDescriptor.setWorkingDirectory(processContext.getWorkingDir());

        List<String> inputValues = getProcessInputValues(processModel.getProcessInputs());
        inputValues.addAll(getProcessOutputValues(processModel.getProcessOutputs()));
        jobDescriptor.setInputValues(inputValues);

        jobDescriptor.setUserName(processContext.getJobSubmissionRemoteCluster().getServerInfo().getUserName());
        jobDescriptor.setShellName("/bin/bash");
        jobDescriptor.setAllEnvExport(true);
        jobDescriptor.setOwner(processContext.getJobSubmissionRemoteCluster().getServerInfo().getUserName());
        // get walltime
        try {
            JobSubmissionTaskModel jobSubmissionTaskModel = ((JobSubmissionTaskModel) taskContext.getSubTaskModel());
            if (jobSubmissionTaskModel.getWallTime() > 0) {
                jobDescriptor.setMaxWallTime(jobSubmissionTaskModel.getWallTime() + "");
            }
        } catch (TException e) {
            log.error("Error while getting job submissiont sub task model", e);
        }

        ComputationalResourceSchedulingModel scheduling = processModel.getProcessResourceSchedule();
        if (scheduling != null) {
            int totalNodeCount = scheduling.getNodeCount();
            int totalCPUCount = scheduling.getTotalCPUCount();

            if (scheduling.getQueueName() != null) {
                jobDescriptor.setQueueName(scheduling.getQueueName());
            }
            if (totalNodeCount > 0) {
                jobDescriptor.setNodes(totalNodeCount);
            }
            // qos per queue
            String qoS = getQoS(crp.getQualityOfService(), scheduling.getQueueName());
            if (qoS != null) {
                jobDescriptor.setQoS(qoS);
            }
            if (totalCPUCount > 0) {
                int ppn = totalCPUCount / totalNodeCount;
                jobDescriptor.setProcessesPerNode(ppn);
                jobDescriptor.setCPUCount(totalCPUCount);
            }
            // max wall time may be set before this level if jobsubmission task has wall time configured to this job,
            // if so we ignore scheduling configuration.
            if (scheduling.getWallTimeLimit() > 0 && jobDescriptor.getMaxWallTime() == null) {
                jobDescriptor.setMaxWallTime(String.valueOf(scheduling.getWallTimeLimit()));
                if (resourceJobManager != null) {
                    if (resourceJobManager.getResourceJobManagerType().equals(ResourceJobManagerType.LSF)) {
                        jobDescriptor.setMaxWallTimeForLSF(String.valueOf(scheduling.getWallTimeLimit()));
                    }
                }
            }
            if (scheduling.getTotalPhysicalMemory() > 0) {
                jobDescriptor.setUsedMemory(scheduling.getTotalPhysicalMemory() + "");
            }
        } else {
            log.error("Task scheduling cannot be null at this point..");
        }

        ApplicationDeploymentDescription appDepDescription = processContext.getApplicationDeploymentDescription();
        List<CommandObject> moduleCmds = appDepDescription.getModuleLoadCmds();
        if (moduleCmds != null) {
            Collections.sort(moduleCmds,
                    (o1, o2) -> ((CommandObject) o1).getCommandOrder() - ((CommandObject) o2).getCommandOrder());
            for (CommandObject moduleCmd : moduleCmds) {
                jobDescriptor.addModuleLoadCommands(moduleCmd.getCommand());
            }
        }
        List<CommandObject> preJobCommands = appDepDescription.getPreJobCommands();
        if (preJobCommands != null) {
            Collections.sort(preJobCommands,
                    (o1, o2) -> ((CommandObject) o1).getCommandOrder() - ((CommandObject) o2).getCommandOrder());
            for (CommandObject preJobCommand : preJobCommands) {
                jobDescriptor.addPreJobCommand(parseCommand(preJobCommand.getCommand(), processContext));
            }
        }

        List<CommandObject> postJobCommands = appDepDescription.getPostJobCommands();
        if (postJobCommands != null) {
            Collections.sort(postJobCommands,
                    (o1, o2) -> ((CommandObject) o1).getCommandOrder() - ((CommandObject) o2).getCommandOrder());
            for (CommandObject postJobCommand : postJobCommands) {
                jobDescriptor.addPostJobCommand(parseCommand(postJobCommand.getCommand(), processContext));
            }
        }

        ApplicationParallelismType parallelism = appDepDescription.getParallelism();
        Map<ApplicationParallelismType, String> parallelismPrefix = processContext.getResourceJobManager().getParallelismPrefix();
        if (parallelism != null) {
            if (parallelism != ApplicationParallelismType.SERIAL) {
                if (parallelismPrefix != null){
                    String parallelismCommand = parallelismPrefix.get(parallelism);
                    if (parallelismCommand != null){
                        jobDescriptor.setJobSubmitter(parallelismCommand);
                    }else {
                        throw new GFacException("Parallelism prefix is not defined for given parallelism type " + parallelism + ".. Please define the parallelism prefix at App Catalog");
                    }
                }
            }
        }
        return jobDescriptor;
    }

    private static void setMailAddresses(ProcessContext processContext, JobDescriptor jobDescriptor)
            throws GFacException, AppCatalogException, ApplicationSettingsException {

        ProcessModel processModel =  processContext.getProcessModel();
        String emailIds = null;
        if (isEmailBasedJobMonitor(processContext)) {
            emailIds = ServerSettings.getEmailBasedMonitorAddress();
        }
        if (ServerSettings.getSetting(ServerSettings.JOB_NOTIFICATION_ENABLE).equalsIgnoreCase("true")) {
            String flags = ServerSettings.getSetting(ServerSettings.JOB_NOTIFICATION_FLAGS);
            if (flags != null && processContext.getComputeResourceDescription().getHostName().equals("stampede.tacc.xsede.org")) {
                flags = "ALL";
            }
            jobDescriptor.setMailOptions(flags);

            String userJobNotifEmailIds = ServerSettings.getSetting(ServerSettings.JOB_NOTIFICATION_EMAILIDS);
            if (userJobNotifEmailIds != null && !userJobNotifEmailIds.isEmpty()) {
                if (emailIds != null && !emailIds.isEmpty()) {
                    emailIds += ("," + userJobNotifEmailIds);
                } else {
                    emailIds = userJobNotifEmailIds;
                }
            }
            if (processModel.isEnableEmailNotification()) {
                List<String> emailList = processModel.getEmailAddresses();
                String elist = GFacUtils.listToCsv(emailList, ',');
                if (elist != null && !elist.isEmpty()) {
                    if (emailIds != null && !emailIds.isEmpty()) {
                        emailIds = emailIds + "," + elist;
                    } else {
                        emailIds = elist;
                    }
                }
            }
        }
        if (emailIds != null && !emailIds.isEmpty()) {
            log.info("Email list: " + emailIds);
            jobDescriptor.setMailAddress(emailIds);
        }
    }

    private static String getReservation(ComputeResourcePreference crp) {
        long start = crp.getReservationStartTime();
        long end = crp.getReservationEndTime();
        String reservation = null;
        if (start > 0 && start < end) {
            long now = Calendar.getInstance().getTimeInMillis();
            if (now > start && now < end) {
                reservation = crp.getReservation();
            }
        } else {
            reservation = crp.getReservation();
        }
       return reservation;
    }

    private static List<String> getProcessOutputValues(List<OutputDataObjectType> processOutputs) {
        List<String> inputValues = new ArrayList<>();
        if (processOutputs != null) {
            for (OutputDataObjectType output : processOutputs) {
                if (output.getApplicationArgument() != null
                        && !output.getApplicationArgument().equals("")) {
                    inputValues.add(output.getApplicationArgument());
                }
                if (output.getValue() != null && !output.getValue().equals("") && output.isRequiredToAddedToCommandLine()) {
                    if (output.getType() == DataType.URI) {
                        String filePath = output.getValue();
                        filePath = filePath.substring(filePath.lastIndexOf(File.separatorChar) + 1, filePath.length());
                        inputValues.add(filePath);
                    }
                }
            }
        }
        return inputValues;
    }

    private static List<String> getProcessInputValues(List<InputDataObjectType> processInputs) {
        List<String> inputValues = new ArrayList<String>();
        if (processInputs != null) {

            // sort the inputs first and then build the command ListR
            Comparator<InputDataObjectType> inputOrderComparator = new Comparator<InputDataObjectType>() {
                @Override
                public int compare(InputDataObjectType inputDataObjectType, InputDataObjectType t1) {
                    return inputDataObjectType.getInputOrder() - t1.getInputOrder();
                }
            };
            Set<InputDataObjectType> sortedInputSet = new TreeSet<InputDataObjectType>(inputOrderComparator);
            for (InputDataObjectType input : processInputs) {
                sortedInputSet.add(input);
            }
            for (InputDataObjectType inputDataObjectType : sortedInputSet) {
                if (!inputDataObjectType.isRequiredToAddedToCommandLine()) {
                    continue;
                }
                if (inputDataObjectType.getApplicationArgument() != null
                        && !inputDataObjectType.getApplicationArgument().equals("")) {
                    inputValues.add(inputDataObjectType.getApplicationArgument());
                }

                if (inputDataObjectType.getValue() != null
                        && !inputDataObjectType.getValue().equals("")) {
                    if (inputDataObjectType.getType() == DataType.URI) {
                        // set only the relative path
                        String filePath = inputDataObjectType.getValue();
                        filePath = filePath.substring(filePath.lastIndexOf(File.separatorChar) + 1, filePath.length());
                        inputValues.add(filePath);
                    } else if (inputDataObjectType.getType() == DataType.URI_COLLECTION) {
                        String filePaths = inputDataObjectType.getValue();
                        String[] paths = filePaths.split(GFacConstants.MULTIPLE_INPUTS_SPLITTER);
                        String filePath;
                        String inputs = "";
                        int i = 0;
                        for (; i < paths.length - 1; i++) {
                            filePath = paths[i];
                            filePath = filePath.substring(filePath.lastIndexOf(File.separatorChar) + 1, filePath.length());
                            // File names separate by a space
                            inputs += filePath + " ";
                        }
                        inputs += paths[i];
                        inputValues.add(inputs);
                    } else {
                        inputValues.add(inputDataObjectType.getValue());
                    }

                }
            }
        }
        return inputValues;
    }

    static String getQoS(String qualityOfService, String preferredBatchQueue) {
        if(preferredBatchQueue == null  || preferredBatchQueue.isEmpty()
                ||  qualityOfService == null  || qualityOfService.isEmpty()) return null;
        final String qos = "qos";
        Pattern pattern = Pattern.compile(preferredBatchQueue + "=(?<" + qos + ">[^,]*)");
        Matcher matcher = pattern.matcher(qualityOfService);
        if (matcher.find()) {
            return matcher.group(qos);
        }
        return null;
    }

    private static int generateJobName() {
        Random random = new Random();
        int i = random.nextInt(Integer.MAX_VALUE);
        i = i + 99999999;
        if (i < 0) {
            i = i * (-1);
        }
        return i;
    }

    private static String parseCommand(String value, ProcessContext context) {
        String parsedValue = value.replaceAll("\\$workingDir", context.getWorkingDir());
        parsedValue = parsedValue.replaceAll("\\$inputDir", context.getInputDir());
        parsedValue = parsedValue.replaceAll("\\$outputDir", context.getOutputDir());
        return parsedValue;
    }

    public static ResourceJobManager getResourceJobManager(ProcessContext processContext) {
        try {
            JobSubmissionProtocol submissionProtocol = getPreferredJobSubmissionProtocol(processContext);
            JobSubmissionInterface jobSubmissionInterface = getPreferredJobSubmissionInterface(processContext);
            if (submissionProtocol == JobSubmissionProtocol.SSH ) {
                SSHJobSubmission sshJobSubmission = GFacUtils.getSSHJobSubmission(jobSubmissionInterface.getJobSubmissionInterfaceId());
                if (sshJobSubmission != null) {
                    return sshJobSubmission.getResourceJobManager();
                }
            } else if (submissionProtocol == JobSubmissionProtocol.LOCAL) {
                LOCALSubmission localJobSubmission = GFacUtils.getLocalJobSubmission(jobSubmissionInterface.getJobSubmissionInterfaceId());
                if (localJobSubmission != null) {
                    return localJobSubmission.getResourceJobManager();
                }
            } else if (submissionProtocol == JobSubmissionProtocol.SSH_FORK){
                SSHJobSubmission sshJobSubmission = GFacUtils.getSSHJobSubmission(jobSubmissionInterface.getJobSubmissionInterfaceId());
                if (sshJobSubmission != null) {
                    return sshJobSubmission.getResourceJobManager();
                }
            }
        } catch (AppCatalogException e) {
            log.error("Error occured while retrieving resource job manager", e);
        }
        return null;
    }

    public static boolean isEmailBasedJobMonitor(ProcessContext processContext) throws GFacException, AppCatalogException {
        JobSubmissionProtocol jobSubmissionProtocol = getPreferredJobSubmissionProtocol(processContext);
        JobSubmissionInterface jobSubmissionInterface = getPreferredJobSubmissionInterface(processContext);
        if (jobSubmissionProtocol == JobSubmissionProtocol.SSH) {
            String jobSubmissionInterfaceId = jobSubmissionInterface.getJobSubmissionInterfaceId();
            SSHJobSubmission sshJobSubmission = processContext.getAppCatalog().getComputeResource().getSSHJobSubmission(jobSubmissionInterfaceId);
            MonitorMode monitorMode = sshJobSubmission.getMonitorMode();
            return monitorMode != null && monitorMode == MonitorMode.JOB_EMAIL_NOTIFICATION_MONITOR;
        } else {
            return false;
        }
    }

    public static JobSubmissionInterface getPreferredJobSubmissionInterface(ProcessContext context) throws AppCatalogException {
        try {
            String resourceHostId = context.getComputeResourceDescription().getComputeResourceId();
            ComputeResourcePreference resourcePreference = context.getComputeResourcePreference();
            JobSubmissionProtocol preferredJobSubmissionProtocol = resourcePreference.getPreferredJobSubmissionProtocol();
            ComputeResourceDescription resourceDescription = context.getAppCatalog().getComputeResource().getComputeResource(resourceHostId);
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

    public static JobSubmissionProtocol getPreferredJobSubmissionProtocol(ProcessContext context) throws AppCatalogException {
        try {
            GwyResourceProfile gatewayProfile = context.getAppCatalog().getGatewayProfile();
            String resourceHostId = context.getComputeResourceDescription().getComputeResourceId();
            ComputeResourcePreference preference = gatewayProfile.getComputeResourcePreference(context.getGatewayId()
		            , resourceHostId);
            return preference.getPreferredJobSubmissionProtocol();
        } catch (AppCatalogException e) {
            log.error("Error occurred while initializing app catalog", e);
            throw new AppCatalogException("Error occurred while initializing app catalog", e);
        }
    }

    public static ComputeResourcePreference getComputeResourcePreference(ProcessContext context) throws AppCatalogException {
        try {
            GwyResourceProfile gatewayProfile = context.getAppCatalog().getGatewayProfile();
            String resourceHostId = context.getComputeResourceDescription().getComputeResourceId();
            return gatewayProfile.getComputeResourcePreference(context.getGatewayId(), resourceHostId);
        } catch (AppCatalogException e) {
            log.error("Error occurred while initializing app catalog", e);
            throw new AppCatalogException("Error occurred while initializing app catalog", e);
        }
    }

    public static File createJobFile(TaskContext taskContext, JobDescriptor jobDescriptor, JobManagerConfiguration jobManagerConfiguration) throws GFacException {
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            URL resource = ApplicationSettings.loadFile(jobManagerConfiguration.getJobDescriptionTemplateName());

            if (resource == null) {
                String error = "System configuration file '" + jobManagerConfiguration.getJobDescriptionTemplateName()
                        + "' not found in the classpath";
                throw new GFacException(error);
            }

            Source xslt = new StreamSource(new File(resource.getPath()));
            Transformer transformer;
            StringWriter results = new StringWriter();
            File tempJobFile = null;
            // generate the pbs script using xslt
            transformer = factory.newTransformer(xslt);
            Source text = new StreamSource(new ByteArrayInputStream(jobDescriptor.toXML().getBytes()));
            transformer.transform(text, new StreamResult(results));
            String scriptContent = results.toString().replaceAll("^[ |\t]*\n$", "");
            if (scriptContent.startsWith("\n")) {
                scriptContent = scriptContent.substring(1);
            }
            // creating a temporary file using pbs script generated above
            int number = new SecureRandom().nextInt();
            number = (number < 0 ? -number : number);

	        tempJobFile = new File(GFacUtils.getLocalDataDir(taskContext), "job_" + Integer.toString(number) +
			        jobManagerConfiguration.getScriptExtension());
	        FileUtils.writeStringToFile(tempJobFile, scriptContent);
            return tempJobFile;
        } catch (IOException e) {
            throw new GFacException("Error occurred while creating the temp job script file", e);
        } catch (TransformerConfigurationException e) {
            throw new GFacException("Error occurred while creating the temp job script file", e);
        } catch (TransformerException e) {
            throw new GFacException("Error occurred while creating the temp job script file", e);
        }
    }

	public static File getLocalDataDir(TaskContext taskContext) {
		String outputPath = ServerSettings.getLocalDataLocation();
		outputPath = (outputPath.endsWith(File.separator) ? outputPath : outputPath + File.separator);
		return new File(outputPath + taskContext.getParentProcessContext() .getProcessId());
	}
	public static String getExperimentNodePath(String experimentId) {
		return ZKPaths.makePath(ZkConstants.ZOOKEEPER_EXPERIMENT_NODE, experimentId);
	}

	public static long getProcessDeliveryTag(CuratorFramework curatorClient, String experimentId, String processId) throws Exception {
		String deliveryTagPath = ZKPaths.makePath(ZKPaths.makePath(getExperimentNodePath(experimentId), processId),
				ZkConstants.ZOOKEEPER_DELIVERYTAG_NODE);
		Stat stat = curatorClient.checkExists().forPath(deliveryTagPath);
		if (stat != null) {
			byte[] bytes = curatorClient.getData().forPath(deliveryTagPath);
			return GFacUtils.bytesToLong(bytes);
		} else {
			throw new GFacException("Couldn't fine the deliveryTag path: " + deliveryTagPath);
		}
	}

	public static void saveJobModel(ProcessContext processContext, JobModel jobModel) throws GFacException {
		try {
			ExperimentCatalog experimentCatalog = processContext.getExperimentCatalog();
			experimentCatalog.add(ExpCatChildDataType.JOB, jobModel, processContext.getProcessId());
		} catch (RegistryException e) {
			String msg = "expId: " + processContext.getExperimentId() + " processId: " + processContext.getProcessId()
					+ " jobId: " + jobModel.getJobId() + " : - Error while saving Job Model";
			throw new GFacException(msg, e);
		}
	}

    public static void saveExperimentInput(ProcessContext processContext, String inputName, String inputVal) throws GFacException {
        try {
            ExperimentCatalog experimentCatalog = processContext.getExperimentCatalog();
            String experimentId = processContext.getExperimentId();
            ExperimentModel experiment = (ExperimentModel)experimentCatalog.get(ExperimentCatalogModelType.EXPERIMENT, experimentId);
            List<InputDataObjectType> experimentInputs = experiment.getExperimentInputs();
            if (experimentInputs != null && !experimentInputs.isEmpty()){
                for (InputDataObjectType expInput : experimentInputs){
                    if (expInput.getName().equals(inputName)){
                        expInput.setValue(inputVal);
                    }
                }
            }
            experimentCatalog.update(ExperimentCatalogModelType.EXPERIMENT, experiment, experimentId);
        } catch (RegistryException e) {
            String msg = "expId: " + processContext.getExperimentId() + " processId: " + processContext.getProcessId()
                    + " : - Error while updating experiment inputs";
            throw new GFacException(msg, e);
        }
    }

    public static void saveProcessInput(ProcessContext processContext, String inputName, String inputVal) throws GFacException {
        try {
            ExperimentCatalog experimentCatalog = processContext.getExperimentCatalog();
            String processId = processContext.getProcessId();
            ProcessModel processModel = (ProcessModel)experimentCatalog.get(ExperimentCatalogModelType.PROCESS, processId);
            List<InputDataObjectType> processInputs = processModel.getProcessInputs();
            if (processInputs != null && !processInputs.isEmpty()){
                for (InputDataObjectType processInput : processInputs){
                    if (processInput.getName().equals(inputName)){
                        processInput.setValue(inputVal);
                    }
                }
            }
            experimentCatalog.update(ExperimentCatalogModelType.PROCESS, processModel, processId);
        } catch (RegistryException e) {
            String msg = "expId: " + processContext.getExperimentId() + " processId: " + processContext.getProcessId()
                    + " : - Error while updating experiment inputs";
            throw new GFacException(msg, e);
        }
    }

    public static void saveExperimentOutput(ProcessContext processContext, String outputName, String outputVal) throws GFacException {
        try {
            ExperimentCatalog experimentCatalog = processContext.getExperimentCatalog();
            String experimentId = processContext.getExperimentId();
            ExperimentModel experiment = (ExperimentModel)experimentCatalog.get(ExperimentCatalogModelType.EXPERIMENT, experimentId);
            List<OutputDataObjectType> experimentOutputs = experiment.getExperimentOutputs();
            if (experimentOutputs != null && !experimentOutputs.isEmpty()){
                for (OutputDataObjectType expOutput : experimentOutputs){
                    if (expOutput.getName().equals(outputName)){
                        DataProductModel dataProductModel = new DataProductModel();
                        dataProductModel.setGatewayId(processContext.getGatewayId());
                        dataProductModel.setOwnerName(processContext.getProcessModel().getUserName());
                        dataProductModel.setProductName(outputName);
                        dataProductModel.setDataProductType(DataProductType.FILE);

                        DataReplicaLocationModel replicaLocationModel = new DataReplicaLocationModel();
                        replicaLocationModel.setStorageResourceId(processContext.getStorageResource().getStorageResourceId());
                        replicaLocationModel.setReplicaName(outputName + " gateway data store copy");
                        replicaLocationModel.setReplicaLocationCategory(ReplicaLocationCategory.GATEWAY_DATA_STORE);
                        replicaLocationModel.setReplicaPersistentType(ReplicaPersistentType.TRANSIENT);
                        replicaLocationModel.setFilePath(outputVal);
                        dataProductModel.addToReplicaLocations(replicaLocationModel);

                        ReplicaCatalog replicaCatalog = RegistryFactory.getReplicaCatalog();
                        String productUri = replicaCatalog.registerDataProduct(dataProductModel);
                        expOutput.setValue(productUri);
                    }
                }
            }
            experimentCatalog.update(ExperimentCatalogModelType.EXPERIMENT, experiment, experimentId);
        } catch (RegistryException e) {
            String msg = "expId: " + processContext.getExperimentId() + " processId: " + processContext.getProcessId()
                    + " : - Error while updating experiment outputs";
            throw new GFacException(msg, e);
        }
    }

    public static void saveProcessOutput(ProcessContext processContext, String outputName, String outputVal) throws GFacException {
        try {
            ExperimentCatalog experimentCatalog = processContext.getExperimentCatalog();
            String processId = processContext.getProcessId();
            List<OutputDataObjectType>  processOutputs = (List<OutputDataObjectType> )experimentCatalog.get(ExperimentCatalogModelType.PROCESS_OUTPUT, processId);
            if (processOutputs != null && !processOutputs.isEmpty()){
                for (OutputDataObjectType processOutput : processOutputs){
                    if (processOutput.getName().equals(outputName)){
                        processOutput.setValue(outputVal);
                    }
                }
            }
            ProcessModel processModel = processContext.getProcessModel();
            processModel.setProcessOutputs(processOutputs);
            experimentCatalog.update(ExperimentCatalogModelType.PROCESS, processModel, processId);
        } catch (RegistryException e) {
            String msg = "expId: " + processContext.getExperimentId() + " processId: " + processContext.getProcessId()
                    + " : - Error while updating experiment outputs";
            throw new GFacException(msg, e);
        }
    }

    public static void saveExperimentError(ProcessContext processContext, ErrorModel errorModel) throws GFacException {
        try {
            ExperimentCatalog experimentCatalog = processContext.getExperimentCatalog();
            String experimentId = processContext.getExperimentId();
            errorModel.setErrorId(AiravataUtils.getId("EXP_ERROR"));
            experimentCatalog.add(ExpCatChildDataType.EXPERIMENT_ERROR, errorModel, experimentId);
        } catch (RegistryException e) {
            String msg = "expId: " + processContext.getExperimentId() + " processId: " + processContext.getProcessId()
                    + " : - Error while updating experiment errors";
            throw new GFacException(msg, e);
        }
    }

    public static void saveProcessError(ProcessContext processContext, ErrorModel errorModel) throws GFacException {
        try {
            ExperimentCatalog experimentCatalog = processContext.getExperimentCatalog();
            errorModel.setErrorId(AiravataUtils.getId("PROCESS_ERROR"));
            experimentCatalog.add(ExpCatChildDataType.PROCESS_ERROR, errorModel, processContext.getProcessId());
        } catch (RegistryException e) {
            String msg = "expId: " + processContext.getExperimentId() + " processId: " + processContext.getProcessId()
                    + " : - Error while updating process errors";
            throw new GFacException(msg, e);
        }
    }

    public static void saveTaskError(TaskContext taskContext, ErrorModel errorModel) throws GFacException {
        try {
            ExperimentCatalog experimentCatalog = taskContext.getParentProcessContext().getExperimentCatalog();
            String taskId = taskContext.getTaskId();
            errorModel.setErrorId(AiravataUtils.getId("TASK_ERROR"));
            experimentCatalog.add(ExpCatChildDataType.TASK_ERROR, errorModel, taskId);
        } catch (RegistryException e) {
            String msg = "expId: " + taskContext.getParentProcessContext().getExperimentId() + " processId: " + taskContext.getParentProcessContext().getProcessId() + " taskId: " + taskContext.getTaskId()
                    + " : - Error while updating task errors";
            throw new GFacException(msg, e);
        }
    }

	public static void handleProcessInterrupt(ProcessContext processContext) throws GFacException {
		if (processContext.isCancel()) {
			ProcessStatus pStatus = new ProcessStatus(ProcessState.CANCELLING);
			pStatus.setReason("Process Cancel triggered");
			pStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
			processContext.setProcessStatus(pStatus);
			saveAndPublishProcessStatus(processContext);
			// do cancel operation here

			pStatus.setState(ProcessState.CANCELED);
			processContext.setProcessStatus(pStatus);
            pStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
            saveAndPublishProcessStatus(processContext);
		}else if (processContext.isHandOver()) {

		} else {
			log.error("expId: {}, processId: {} :- Unknown process interrupt", processContext.getExperimentId(),
					processContext.getProcessId());
		}
	}

    public static JobModel getJobModel(ProcessContext processContext) throws RegistryException {
        ExperimentCatalog experimentCatalog = processContext.getExperimentCatalog();
        List<Object> objects = experimentCatalog.get(ExperimentCatalogModelType.JOB,
                Constants.FieldConstants.JobConstants.PROCESS_ID, processContext.getProcessId());
        List<JobModel> jobModels = new ArrayList<>();
        JobModel jobModel = null;
        if (objects != null) {
            for (Object object : objects) {
                jobModel = ((JobModel) object);
                if (jobModel.getJobId() != null || !jobModel.equals("")) {
                    return jobModel;
                }
            }
        }
        return jobModel;
    }

    public static List<String> parseTaskDag(String taskDag) {
        // TODO - parse taskDag and create taskId list
        String[] tasks = taskDag.split(",");
        return Arrays.asList(tasks);
    }

}
