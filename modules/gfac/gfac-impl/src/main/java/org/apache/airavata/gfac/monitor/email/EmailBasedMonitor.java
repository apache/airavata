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
package org.apache.airavata.gfac.monitor.email;

import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.utils.ThriftUtils;
import org.apache.airavata.gfac.core.GFacException;
import org.apache.airavata.gfac.core.GFacThreadPoolExecutor;
import org.apache.airavata.gfac.core.GFacUtils;
import org.apache.airavata.gfac.core.config.ResourceConfig;
import org.apache.airavata.gfac.core.context.ProcessContext;
import org.apache.airavata.gfac.core.context.TaskContext;
import org.apache.airavata.gfac.core.monitor.EmailParser;
import org.apache.airavata.gfac.core.monitor.JobMonitor;
import org.apache.airavata.gfac.core.monitor.JobStatusResult;
import org.apache.airavata.gfac.impl.Factory;
import org.apache.airavata.gfac.impl.GFacWorker;
import org.apache.airavata.model.appcatalog.computeresource.ResourceJobManagerType;
import org.apache.airavata.model.job.JobModel;
import org.apache.airavata.model.status.JobState;
import org.apache.airavata.model.status.JobStatus;
import org.apache.airavata.model.status.ProcessState;
import org.apache.airavata.model.status.ProcessStatus;
import org.apache.airavata.model.status.TaskState;
import org.apache.airavata.model.status.TaskStatus;
import org.apache.airavata.registry.api.RegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.FlagTerm;
import javax.mail.search.SearchTerm;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class EmailBasedMonitor implements JobMonitor, Runnable{
    private static final Logger log = LoggerFactory.getLogger(EmailBasedMonitor.class);

    public static final int COMPARISON = 6; // after and equal
    public static final String IMAPS = "imaps";
    public static final String POP3 = "pop3";
    private boolean stopMonitoring = false;

    private Session session ;
    private Store store;
    private Folder emailFolder;
    private Properties properties;
    private Map<String, TaskContext> jobMonitorMap = new ConcurrentHashMap<>();
    private String host, emailAddress, password, storeProtocol, folderName ;
    private Date monitorStartDate;
    private Map<ResourceJobManagerType, EmailParser> emailParserMap = new HashMap<ResourceJobManagerType, EmailParser>();
	private Map<String, ResourceJobManagerType> addressMap = new HashMap<>();
	private Message[] flushUnseenMessages;
    private Map<String, Boolean> canceledJobs = new ConcurrentHashMap<>();
    private Timer timer;


    public EmailBasedMonitor(Map<ResourceJobManagerType, ResourceConfig> resourceConfigs) throws AiravataException {
		init();
		populateAddressAndParserMap(resourceConfigs);
	}

	private void init() throws AiravataException {
        host = ServerSettings.getEmailBasedMonitorHost();
        emailAddress = ServerSettings.getEmailBasedMonitorAddress();
        password = ServerSettings.getEmailBasedMonitorPassword();
        storeProtocol = ServerSettings.getEmailBasedMonitorStoreProtocol();
        folderName = ServerSettings.getEmailBasedMonitorFolderName();
        if (!(storeProtocol.equals(IMAPS) || storeProtocol.equals(POP3))) {
            throw new AiravataException("Unsupported store protocol , expected " +
                    IMAPS + " or " + POP3 + " but found " + storeProtocol);
        }
        properties = new Properties();
        properties.put("mail.store.protocol", storeProtocol);
        timer = new Timer("CancelJobHandler", true);
        long period = 1000 * 60 * 5; // five minute delay between successive task executions.
        timer.schedule(new CancelTimerTask(), 0 , period);
    }

	private void populateAddressAndParserMap(Map<ResourceJobManagerType, ResourceConfig> resourceConfigs) throws AiravataException {
		for (Map.Entry<ResourceJobManagerType, ResourceConfig> resourceConfigEntry : resourceConfigs.entrySet()) {
			ResourceJobManagerType type = resourceConfigEntry.getKey();
			ResourceConfig config = resourceConfigEntry.getValue();
			List<String> resourceEmailAddresses = config.getResourceEmailAddresses();
            if (resourceEmailAddresses != null && !resourceEmailAddresses.isEmpty()){
                for (String resourceEmailAddress : resourceEmailAddresses) {
                    addressMap.put(resourceEmailAddress, type);
                }
                try {
                    Class<? extends EmailParser> emailParserClass = Class.forName(config.getEmailParser()).asSubclass(EmailParser.class);
                    EmailParser emailParser = emailParserClass.getConstructor().newInstance();
                    emailParserMap.put(type, emailParser);
                } catch (Exception e) {
                    throw new AiravataException("Error while instantiation email parsers", e);
                }
            }
		}

	}
	@Override
	public void monitor(String jobId, TaskContext taskContext) {
		log.info("[EJM]: Added monitor Id : {} to email based monitor map", jobId);
		jobMonitorMap.put(jobId, taskContext);
        taskContext.getParentProcessContext().setPauseTaskExecution(true);
	}

	@Override
	public void stopMonitor(String jobId, boolean runOutflow) {
		TaskContext taskContext = jobMonitorMap.remove(jobId);
		if (taskContext != null && runOutflow) {
            RegistryService.Client registryClient = Factory.getRegistryServiceClient();
			try {
                ProcessContext pc = taskContext.getParentProcessContext();
                if (taskContext.isCancel()) {
                    // Moved job status to cancel
                    JobModel jobModel = pc.getJobModel();
                    JobStatus newJobStatus = new JobStatus(JobState.CANCELED);
                    newJobStatus.setReason("Moving job status to cancel, as we didn't see any email from this job " +
                            "for a while after execute job cancel command. This may happen if job was in queued state " +
                            "when we run the cancel command");
                    jobModel.setJobStatuses(Arrays.asList(newJobStatus));
                    GFacUtils.saveJobStatus(pc, registryClient, jobModel);
                }
                ProcessStatus pStatus = new ProcessStatus(ProcessState.CANCELLING);
                pStatus.setReason("Job cancelled");
                pc.setProcessStatus(pStatus);
                GFacUtils.saveAndPublishProcessStatus(pc, registryClient);
                GFacThreadPoolExecutor.getCachedThreadPool().execute(new GFacWorker(pc));
			} catch (GFacException e) {
				log.info("[EJM]: Error while running output tasks", e);
            } finally {
                if (registryClient != null) {
                    ThriftUtils.close(registryClient);
                }
			}
		}
	}

    @Override
    public boolean isMonitoring(String jobId) {
        return jobMonitorMap.containsKey(jobId);
    }

    @Override
    public void canceledJob(String jobId) {
        canceledJobs.put(jobId, Boolean.FALSE);
    }

    private JobStatusResult parse(Message message) throws MessagingException, AiravataException {
        Address fromAddress = message.getFrom()[0];
        String addressStr = fromAddress.toString();
        ResourceJobManagerType jobMonitorType = getJobMonitorType(addressStr);
        EmailParser emailParser = emailParserMap.get(jobMonitorType);
	    if (emailParser == null) {
		    throw new AiravataException("[EJM]: Un-handle resource job manager type: " + jobMonitorType
				    .toString() + " for email monitoring -->  " + addressStr);
	    }
        return emailParser.parseEmail(message);
    }

    private ResourceJobManagerType getJobMonitorType(String addressStr) throws AiravataException {
//        System.out.println("*********** address ******** : " + addressStr);
	    for (Map.Entry<String, ResourceJobManagerType> addressEntry : addressMap.entrySet()) {
            if (addressStr.contains(addressEntry.getKey())) {
                return addressEntry.getValue();
            }
        }
	    throw new AiravataException("[EJM]: Couldn't identify Resource job manager type from address " + addressStr);
    }

    @Override
    public void run() {
        boolean quite = false;

	    while (!stopMonitoring && !ServerSettings.isStopAllThreads()) {
		    try {
			    session = Session.getDefaultInstance(properties);
			    store = session.getStore(storeProtocol);
			    store.connect(host, emailAddress, password);
			    emailFolder = store.getFolder(folderName);
			    // first time we search for all unread messages.
			    SearchTerm unseenBefore = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
			    while (!(stopMonitoring || ServerSettings.isStopAllThreads())) {
				    Thread.sleep(ServerSettings.getEmailMonitorPeriod());// sleep a bit - get a rest till job finishes
				    if (jobMonitorMap.isEmpty()) {
                        if (!quite) {
                            log.info("[EJM]: Job Monitor Map is empty, no need to retrieve emails");
                        }
                        quite = true;
					    continue;
				    } else {
                        quite = false;
					    log.info("[EJM]: {} job/s in job monitor map", jobMonitorMap.size());
				    }
				    if (!store.isConnected()) {
					    store.connect();
					    emailFolder = store.getFolder(folderName);
				    }
				    log.info("[EJM]: Retrieving unseen emails");
				    emailFolder.open(Folder.READ_WRITE);
				    if (emailFolder.isOpen()) {
					    // flush if any message left in flushUnseenMessage
					    if (flushUnseenMessages != null && flushUnseenMessages.length > 0) {
						    try {
							    emailFolder.setFlags(flushUnseenMessages, new Flags(Flags.Flag.SEEN), false);
							    flushUnseenMessages = null;
						    } catch (MessagingException e) {
							    if (!store.isConnected()) {
								    store.connect();
								    emailFolder.setFlags(flushUnseenMessages, new Flags(Flags.Flag.SEEN), false);
								    flushUnseenMessages = null;
							    }
						    }
					    }
					    Message[] searchMessages = emailFolder.search(unseenBefore);
					    if (searchMessages == null || searchMessages.length == 0) {
						    log.info("[EJM]: No new email messages");
					    } else {
						    log.info("[EJM]: " + searchMessages.length + " new email/s received");
					    }
                        RegistryService.Client registryClient = Factory.getRegistryServiceClient();
					    try {
                            processMessages(registryClient, searchMessages);
                        } finally {
                            if (registryClient != null) {
                                ThriftUtils.close(registryClient);
                            }
                        }
					    emailFolder.close(false);
				    }
			    }
		    } catch (MessagingException e) {
			    log.error("[EJM]: Couldn't connect to the store ", e);
		    } catch (InterruptedException e) {
			    log.error("[EJM]: Interrupt exception while sleep ", e);
		    } catch (AiravataException e) {
			    log.error("[EJM]: UnHandled arguments ", e);
		    } catch (Throwable e)  {
			    log.error("[EJM]: Caught a throwable ", e);
		    } finally {
			    try {
				    emailFolder.close(false);
				    store.close();
			    } catch (MessagingException e) {
				    log.error("[EJM]: Store close operation failed, couldn't close store", e);
			    } catch (Throwable e) {
				    log.error("[EJM]: Caught a throwable while closing email store ", e);
			    }
		    }
	    }
	    log.info("[EJM]: Email monitoring daemon stopped");
    }

    private void processMessages(RegistryService.Client registryClient, Message[] searchMessages) throws MessagingException {
        List<Message> processedMessages = new ArrayList<>();
        List<Message> unreadMessages = new ArrayList<>();
        for (Message message : searchMessages) {
            try {
                JobStatusResult jobStatusResult = parse(message);
                TaskContext taskContext = null;
                if (jobStatusResult.getJobId() != null) {
                    taskContext = jobMonitorMap.get(jobStatusResult.getJobId());
                } else {
                    log.info("Returned null for job id, message subject--> {}" , message.getSubject());
                }
                if (taskContext == null) {
                    if (jobStatusResult.getJobName() != null) {
                        taskContext = jobMonitorMap.get(jobStatusResult.getJobName());
                    } else {
                        log.info("Returned null for job name, message subject --> {}" , message.getSubject());
                    }
                }
                if (taskContext != null) {
                    process(registryClient, jobStatusResult, taskContext);
                    processedMessages.add(message);

                } else if (!jobStatusResult.isAuthoritative()
                        && (new Date()).getTime() - message.getSentDate().getTime() > 1000 * 60 * 5) {
                    //marking old custom Airavata emails as read
                    processedMessages.add(message);
                    log.info("Marking old Airavata custom emails as read, message subject --> {}", message.getSubject());
                } else {
                    // we can get JobExecutionContext null in multiple Gfac instances environment,
                    // where this job is not submitted by this Gfac instance hence we ignore this message.
                    unreadMessages.add(message);
//                  log.info("JobExecutionContext is not found for job Id " + jobStatusResult.getJobId());
                }
            } catch (AiravataException e) {
                log.error("[EJM]: Error parsing email message =====================================>", e);
                try {
                    writeEnvelopeOnError(message);
                } catch (MessagingException e1) {
                    log.error("[EJM]: Error printing envelop of the email");
                }
                unreadMessages.add(message);
            } catch (MessagingException e) {
                log.error("[EJM]: Error while retrieving sender address from message : " + message.toString());
                unreadMessages.add(message);
            }
        }
        if (!processedMessages.isEmpty()) {
            Message[] seenMessages = new Message[processedMessages.size()];
            processedMessages.toArray(seenMessages);
            try {
                emailFolder.setFlags(seenMessages, new Flags(Flags.Flag.SEEN), true);
            } catch (MessagingException e) {
                if (!store.isConnected()) {
                    store.connect();
                    emailFolder.setFlags(seenMessages, new Flags(Flags.Flag.SEEN), true);
                }
            }

        }
        if (!unreadMessages.isEmpty()) {
            Message[] unseenMessages = new Message[unreadMessages.size()];
            unreadMessages.toArray(unseenMessages);
            try {
                emailFolder.setFlags(unseenMessages, new Flags(Flags.Flag.SEEN), false);
            } catch (MessagingException e) {
	            if (!store.isConnected()) {
		            store.connect();
		            emailFolder.setFlags(unseenMessages, new Flags(Flags.Flag.SEEN), false);
		            flushUnseenMessages = unseenMessages; // anyway we need to push this update.
	            } else {
		            flushUnseenMessages = unseenMessages; // anyway we need to push this update.
	            }
            }
        }
    }

    private void process(RegistryService.Client registryClient, JobStatusResult jobStatusResult, TaskContext taskContext){
        canceledJobs.remove(jobStatusResult.getJobId());
        JobState resultState = jobStatusResult.getState();
        // TODO : update job state on process context
        boolean runOutflowTasks = false;
        JobStatus jobStatus = new JobStatus();
        ProcessContext parentProcessContext = taskContext.getParentProcessContext();
        JobModel jobModel = parentProcessContext.getJobModel();
        String jobDetails = "JobName : " + jobStatusResult.getJobName() + ", JobId : " + jobStatusResult.getJobId();

        JobState currentState = null;
        List<JobStatus> jobStatusList = jobModel.getJobStatuses();
        if (jobStatusList != null && jobStatusList.size() > 0) {
            JobStatus lastStatus = jobStatusList.get(0);
            for (JobStatus temp : jobStatusList) {
                if (temp.getTimeOfStateChange() >= lastStatus.getTimeOfStateChange()) {
                    lastStatus = temp;
                }
            }
            currentState = lastStatus.getJobState();
        }

        // TODO - Handle all other valid JobStates
        // FIXME - What if non-authoritative email comes later (getting accumulated in the email account)
        if (resultState == JobState.COMPLETE) {
            if (jobStatusResult.isAuthoritative()) {
                if (currentState != null && currentState == JobState.COMPLETE) {
                    jobMonitorMap.remove(jobStatusResult.getJobId());
                    runOutflowTasks = false;
                    log.info("[EJM]: Authoritative job Complete email received after early Airavata custom complete email," +
                            " removed job from job monitoring. " + jobDetails);
                } else {
                    jobMonitorMap.remove(jobStatusResult.getJobId());
                    runOutflowTasks = true;
                    jobStatus.setJobState(JobState.COMPLETE);
                    jobStatus.setReason("Complete email received");
                    jobStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
                    log.info("[EJM]: Authoritative job Complete email received , removed job from job monitoring. " + jobDetails);
                }
            } else {
                runOutflowTasks = true;
                jobStatus.setJobState(JobState.COMPLETE);
                jobStatus.setReason("Complete email received");
                jobStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
                log.info("[EJM]: Non Authoritative Job Complete email received. " + jobDetails);
            }
        }else if (resultState == JobState.QUEUED) {
            //It is possible that we will get an early complete message from custom Airavata emails instead from the
            //scheduler
            if (currentState != JobState.COMPLETE) {
                // nothing special thing to do, update the status change to rabbit mq at the end of this method.
                jobStatus.setJobState(JobState.QUEUED);
                jobStatus.setReason("Queue email received");
                jobStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
                log.info("[EJM]: Job Queued email received, " + jobDetails);
            }
        }else if (resultState == JobState.ACTIVE) {
            //It is possible that we will get an early complete message from custom Airavata emails instead from the
            //scheduler
            if (currentState != JobState.COMPLETE) {
                // nothing special thing to do, update the status change to rabbit mq at the end of this method.
                jobStatus.setJobState(JobState.ACTIVE);
                jobStatus.setReason("Active email received");
                jobStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
                log.info("[EJM]: Job Active email received, " + jobDetails);
            }
        }else if (resultState == JobState.FAILED) {
            //It is possible that we will get an early complete message from custom Airavata emails instead from the
            //scheduler
            if (currentState != JobState.COMPLETE) {
                jobMonitorMap.remove(jobStatusResult.getJobId());
                runOutflowTasks = true;
                jobStatus.setJobState(JobState.FAILED);
                jobStatus.setReason("Failed email received");
                jobStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
                log.info("[EJM]: Job failed email received , removed job from job monitoring. " + jobDetails);
            }
        }else if (resultState == JobState.CANCELED) {
            //It is possible that we will get an early complete message from custom Airavata emails instead from the
            //scheduler
            if (currentState != JobState.COMPLETE) {
                jobMonitorMap.remove(jobStatusResult.getJobId());
                jobStatus.setJobState(JobState.CANCELED);
                jobStatus.setReason("Canceled email received");
                jobStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
                log.info("[EJM]: Job canceled mail received, removed job from job monitoring. " + jobDetails);
                runOutflowTasks = true; // we run out flow and this will move process to cancel state.
            }
        }

        if (jobStatus.getJobState() != null) {
		    try {
			    jobModel.setJobStatuses(Arrays.asList(jobStatus));
			    log.info("[EJM]: Publishing status changes to amqp. " + jobDetails);
			    GFacUtils.saveJobStatus(parentProcessContext, registryClient, jobModel);
		    } catch (GFacException e) {
			    log.error("expId: {}, processId: {}, taskId: {}, jobId: {} :- Error while save and publishing Job " +
                        "status {}", taskContext.getExperimentId(), taskContext.getProcessId(), jobModel
                        .getTaskId(), jobModel.getJobId(), jobStatus.getJobState());
		    }
	    }

        if (runOutflowTasks) {
            log.info("[EJM]: Calling Out Handler chain of " + jobDetails);
	        try {
                TaskStatus taskStatus = new TaskStatus(TaskState.COMPLETED);
                taskStatus.setTimeOfStateChange(AiravataUtils.getCurrentTimestamp().getTime());
                taskStatus.setReason("Job monitoring completed with final state: " + TaskState.COMPLETED.name());
                taskContext.setTaskStatus(taskStatus);
                GFacUtils.saveAndPublishTaskStatus(taskContext, registryClient);
                if (parentProcessContext.isCancel()) {
                    ProcessStatus processStatus = new ProcessStatus(ProcessState.CANCELLING);
                    processStatus.setReason("Process has been cancelled");
                    parentProcessContext.setProcessStatus(processStatus);
                    GFacUtils.saveAndPublishProcessStatus(parentProcessContext, registryClient);
                }
		        GFacThreadPoolExecutor.getCachedThreadPool().execute(new GFacWorker(parentProcessContext));
	        } catch (GFacException e) {
		        log.info("[EJM]: Error while running output tasks", e);
	        }
        }
    }

    private void writeEnvelopeOnError(Message m) throws MessagingException {
        Address[] a;
        // FROM
        if ((a = m.getFrom()) != null) {
            for (int j = 0; j < a.length; j++)
                log.error("FROM: " + a[j].toString());
        }
        // TO
        if ((a = m.getRecipients(Message.RecipientType.TO)) != null) {
            for (int j = 0; j < a.length; j++)
                log.error("TO: " + a[j].toString());
        }
        // SUBJECT
        if (m.getSubject() != null)
            log.error("SUBJECT: " + m.getSubject());
    }

    public void stopMonitoring() {
        stopMonitoring = true;
    }

    public void setDate(Date date) {
        this.monitorStartDate = date;
    }

    private class CancelTimerTask extends TimerTask {

        @Override
        public void run() {
            if (!canceledJobs.isEmpty()) {
                Iterator<Map.Entry<String, Boolean>> cancelJobIter = canceledJobs.entrySet().iterator();
                while (cancelJobIter.hasNext()) {
                    Map.Entry<String, Boolean> cancelJobIdWithFlag = cancelJobIter.next();
                    if (!cancelJobIdWithFlag.getValue()) {
                        cancelJobIdWithFlag.setValue(Boolean.TRUE);
                    } else {
                        TaskContext taskContext = jobMonitorMap.get(cancelJobIdWithFlag.getKey());
                        if (taskContext != null) {
                            taskContext.setCancel(true);
                            stopMonitor(cancelJobIdWithFlag.getKey(), true);
                        }
                        cancelJobIter.remove();
                    }
                }
            }
        }
    }
}
