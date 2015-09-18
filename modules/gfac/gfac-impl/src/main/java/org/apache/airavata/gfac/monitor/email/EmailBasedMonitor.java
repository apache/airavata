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
package org.apache.airavata.gfac.monitor.email;

import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.gfac.core.GFacException;
import org.apache.airavata.gfac.core.GFacThreadPoolExecutor;
import org.apache.airavata.gfac.core.GFacUtils;
import org.apache.airavata.gfac.core.config.ResourceConfig;
import org.apache.airavata.gfac.core.context.ProcessContext;
import org.apache.airavata.gfac.core.monitor.EmailParser;
import org.apache.airavata.gfac.core.monitor.JobMonitor;
import org.apache.airavata.gfac.core.monitor.JobStatusResult;
import org.apache.airavata.gfac.impl.GFacWorker;
import org.apache.airavata.gfac.monitor.email.parser.LSFEmailParser;
import org.apache.airavata.gfac.monitor.email.parser.PBSEmailParser;
import org.apache.airavata.gfac.monitor.email.parser.SLURMEmailParser;
import org.apache.airavata.gfac.monitor.email.parser.UGEEmailParser;
import org.apache.airavata.model.appcatalog.computeresource.ResourceJobManagerType;
import org.apache.airavata.model.job.JobModel;
import org.apache.airavata.model.status.JobState;
import org.apache.airavata.model.status.JobStatus;
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
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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
    private Map<String, ProcessContext> jobMonitorMap = new ConcurrentHashMap<>();
    private String host, emailAddress, password, storeProtocol, folderName ;
    private Date monitorStartDate;
    private Map<ResourceJobManagerType, EmailParser> emailParserMap = new HashMap<ResourceJobManagerType, EmailParser>();
	private Map<String, ResourceJobManagerType> addressMap = new HashMap<>();


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
	public void monitor(String jobId, ProcessContext processContext) {
		log.info("[EJM]: Added monitor Id : " + jobId + " to email based monitor map");
		jobMonitorMap.put(jobId, processContext);
	}

	@Override
	public void stopMonitor(String jobId) {
		jobMonitorMap.remove(jobId);
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
		    if (addressEntry.getKey().matches(addressStr)) {
			    return addressEntry.getValue();
		    }
	    }
	    throw new AiravataException("[EJM]: Couldn't identify Resource job manager type from address " + addressStr);
    }

    @Override
    public void run() {

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
					    log.info("[EJM]: Job Monitor Map is empty, no need to retrieve emails");
					    continue;
				    } else {
					    log.info("[EJM]: " + jobMonitorMap.size() + " job/s in job monitor map");
				    }
				    if (!store.isConnected()) {
					    store.connect();
					    emailFolder = store.getFolder(folderName);
				    }
				    log.info("[EJM]: Retrieving unseen emails");
				    emailFolder.open(Folder.READ_WRITE);
				    Message[] searchMessages = emailFolder.search(unseenBefore);
				    if (searchMessages == null || searchMessages.length == 0) {
					    log.info("[EJM]: No new email messages");
				    } else {
					    log.info("[EJM]: " + searchMessages.length + " new email/s received");
				    }
				    processMessages(searchMessages);
				    emailFolder.close(false);
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

    private void processMessages(Message[] searchMessages) throws MessagingException {
        List<Message> processedMessages = new ArrayList<>();
        List<Message> unreadMessages = new ArrayList<>();
        for (Message message : searchMessages) {
            try {
                JobStatusResult jobStatusResult = parse(message);
                ProcessContext processContext = jobMonitorMap.get(jobStatusResult.getJobId());
                if (processContext == null) {
	                processContext = jobMonitorMap.get(jobStatusResult.getJobName());
                }
                if (processContext != null) {
                    process(jobStatusResult, processContext);
                    processedMessages.add(message);
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

                }
            }
        }
    }

    private void process(JobStatusResult jobStatusResult, ProcessContext processContext){
        JobState resultState = jobStatusResult.getState();
	    // TODO : update job state on process context
        boolean runOutflowTasks = false;
	    JobStatus jobStatus = new JobStatus();
	    JobModel jobModel = processContext.getJobModel();
        String jobDetails = "JobName : " + jobStatusResult.getJobName() + ", JobId : " + jobStatusResult.getJobId();
        // TODO - Handle all other valid JobStates
        if (resultState == JobState.COMPLETE) {
            jobMonitorMap.remove(jobStatusResult.getJobId());
	        jobStatus.setJobState(JobState.COMPLETE);
	        jobStatus.setReason("Complete email received");
	        runOutflowTasks = true;
            log.info("[EJM]: Job Complete email received , removed job from job monitoring. " + jobDetails);
        }else if (resultState == JobState.QUEUED) {
	        // nothing special thing to do, update the status change to rabbit mq at the end of this method.
	        jobStatus.setJobState(JobState.QUEUED);
	        jobStatus.setReason("Queue email received");
	        log.info("[EJM]: Job Queued email received, " + jobDetails);
        }else if (resultState == JobState.ACTIVE) {
            // nothing special thing to do, update the status change to rabbit mq at the end of this method.
	        jobStatus.setJobState(JobState.ACTIVE);
	        jobStatus.setReason("Active email received");
            log.info("[EJM]: Job Active email received, " + jobDetails);
        }else if (resultState == JobState.FAILED) {
            jobMonitorMap.remove(jobStatusResult.getJobId());
            runOutflowTasks = true;
	        jobStatus.setJobState(JobState.FAILED);
	        jobStatus.setReason("Failed email received");
            log.info("[EJM]: Job failed email received , removed job from job monitoring. " + jobDetails);
        }else if (resultState == JobState.CANCELED) {
            jobMonitorMap.remove(jobStatusResult.getJobId());
            runOutflowTasks = false; // Do we need to run out handlers in canceled case?
	        jobStatus.setJobState(JobState.CANCELED);
	        jobStatus.setReason("Canceled email received");
            log.info("[EJM]: Job canceled mail received, removed job from job monitoring. " + jobDetails);
        }
	    if (jobStatus.getJobState() != null) {
		    try {
			    jobModel.setJobStatus(jobStatus);
			    log.info("[EJM]: Publishing status changes to amqp. " + jobDetails);
			    GFacUtils.saveJobStatus(processContext, jobModel);
		    } catch (GFacException e) {
			    log.error("expId: {}, processId: {}, taskId: {}, jobId: {} :- Error while save and publishing Job " +
					    "status {}", processContext.getExperimentId(), processContext.getProcessId(), jobModel
					    .getTaskId(), jobModel.getJobId(), jobStatus.getJobState());
		    }
	    }

        if (runOutflowTasks) {
            log.info("[EJM]: Calling Out Handler chain of " + jobDetails);
	        try {
		        GFacThreadPoolExecutor.getCachedThreadPool().execute(new GFacWorker(processContext));
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


}
