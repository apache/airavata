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
import org.apache.airavata.common.logger.AiravataLogger;
import org.apache.airavata.common.logger.AiravataLoggerFactory;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.core.context.JobExecutionContext;
import org.apache.airavata.gfac.core.cpi.BetterGfacImpl;
import org.apache.airavata.gfac.core.utils.GFacThreadPoolExecutor;
import org.apache.airavata.gfac.core.utils.GFacUtils;
import org.apache.airavata.gfac.core.utils.OutHandlerWorker;
import org.apache.airavata.gfac.monitor.email.parser.EmailParser;
import org.apache.airavata.gfac.monitor.email.parser.LSFEmailParser;
import org.apache.airavata.gfac.monitor.email.parser.PBSEmailParser;
import org.apache.airavata.gfac.monitor.email.parser.SLURMEmailParser;
import org.apache.airavata.gfac.monitor.email.parser.UGEEmailParser;
import org.apache.airavata.model.appcatalog.computeresource.ResourceJobManagerType;
import org.apache.airavata.model.messaging.event.JobIdentifier;
import org.apache.airavata.model.messaging.event.JobStatusChangeRequestEvent;
import org.apache.airavata.model.workspace.experiment.ActionableGroup;
import org.apache.airavata.model.workspace.experiment.CorrectiveAction;
import org.apache.airavata.model.workspace.experiment.ErrorCategory;
import org.apache.airavata.model.workspace.experiment.ErrorDetails;
import org.apache.airavata.model.workspace.experiment.JobState;
import org.apache.airavata.model.workspace.experiment.JobStatus;
import org.apache.airavata.registry.cpi.ChildDataType;
import org.apache.airavata.registry.cpi.Registry;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.FlagTerm;
import javax.mail.search.SearchTerm;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class EmailBasedMonitor implements Runnable{
    private static final AiravataLogger log = AiravataLoggerFactory.getLogger(EmailBasedMonitor.class);

    public static final int COMPARISON = 6; // after and equal
    public static final String IMAPS = "imaps";
    public static final String POP3 = "pop3";
    private boolean stopMonitoring = false;

    private Session session ;
    private Store store;
    private Folder emailFolder;
    private Properties properties;
    private Map<String, JobExecutionContext> jobMonitorMap = new ConcurrentHashMap<String, JobExecutionContext>();
    private String host, emailAddress, password, storeProtocol, folderName ;
    private Date monitorStartDate;
    private Map<ResourceJobManagerType, EmailParser> emailParserMap = new HashMap<ResourceJobManagerType, EmailParser>();
    private ExecutorService executor;
    private Message[] flushUnseenMessages;

    public EmailBasedMonitor(ResourceJobManagerType type) throws AiravataException {
        init();
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
        executor = Executors.newFixedThreadPool(30);

    }

    public void addToJobMonitorMap(JobExecutionContext jobExecutionContext) {
        String monitorId = jobExecutionContext.getJobDetails().getJobID();
        if (monitorId == null || monitorId.isEmpty()) {
            monitorId = jobExecutionContext.getJobDetails().getJobName();
        }
        addToJobMonitorMap(monitorId, jobExecutionContext);
    }

    public void addToJobMonitorMap(String monitorId, JobExecutionContext jobExecutionContext) {
        log.info("[EJM]: Added monitor Id : " + monitorId + " to email based monitor map");
        jobMonitorMap.put(monitorId, jobExecutionContext);
    }

    private JobStatusResult parse(Message message) throws MessagingException, AiravataException {
        Address fromAddress = message.getFrom()[0];
        String addressStr = fromAddress.toString();
        ResourceJobManagerType jobMonitorType = getJobMonitorType(addressStr);
        EmailParser emailParser = emailParserMap.get(jobMonitorType);
        if (emailParser == null) {
            switch (jobMonitorType) {
                case PBS:
                    emailParser = new PBSEmailParser();
                    break;
                case SLURM:
                    emailParser = new SLURMEmailParser();
                    break;
                case LSF:
                    emailParser = new LSFEmailParser();
                    break;
                case UGE:
                    emailParser = new UGEEmailParser();
                    break;
                default:
                    throw new AiravataException("[EJM]: Un-handle resource job manager type: " + jobMonitorType.toString() + " for email monitoring -->  " + addressStr);
            }

            emailParserMap.put(jobMonitorType, emailParser);
        }
        return emailParser.parseEmail(message);
    }

    private ResourceJobManagerType getJobMonitorType(String addressStr) throws AiravataException {
        switch (addressStr) {
            case "pbsconsult@sdsc.edu":   // trestles , gordan
            case "adm@trident.bigred2.uits.iu.edu":  // bigred2
            case "root <adm@trident.bigred2.uits.iu.edu>": // bigred2
            case "root <adm@scyld.localdomain>": // alamo
            case "root <adm@tg-login1.blacklight.psc.xsede.org>": //blacklight
                return ResourceJobManagerType.PBS;
            case "SDSC Admin <slurm@comet-fe3.sdsc.edu>": // comet
            case "slurm@batch1.stampede.tacc.utexas.edu": // stampede
            case "slurm user <slurm@tempest.dsc.soic.indiana.edu>": //tempest
                return ResourceJobManagerType.SLURM;
//            case "lsf":
//                return ResourceJobManagerType.LSF;
            default:
                if (addressStr.contains("ls4.tacc.utexas.edu>")) { // lonestar
                    return ResourceJobManagerType.UGE;
                } else if (addressStr.contains("blacklight.psc.xsede.org")) {
                    return ResourceJobManagerType.PBS;
                } else {
                    throw new AiravataException("[EJM]: Couldn't identify Resource job manager type from address " + addressStr);
                }
        }

    }

    @Override
    public void run() {
        try {
            session = Session.getDefaultInstance(properties);
            store = session.getStore(storeProtocol);
            store.connect(host, emailAddress, password);
            emailFolder = store.getFolder(folderName);
            // first time we search for all unread messages.
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
                    // read unread messages
                    Message[] searchMessages = getMessagesToProcess();
                    if (searchMessages == null || searchMessages.length == 0) {
                        log.info("[EJM]: No new email messages");
                    } else {
                        log.info("[EJM]: " + searchMessages.length + " new email/s received");
                    }
                    processMessages(searchMessages);
                }
                emailFolder.close(false);
            }
        } catch (MessagingException e) {
            log.error("[EJM]: Couldn't connect to the store ", e);
        } catch (InterruptedException e) {
            log.error("[EJM]: Interrupt exception while sleep ", e);
        } catch (AiravataException e) {
            log.error("[EJM]: UnHandled arguments ", e);
        } finally {
            try {
                emailFolder.close(false);
                store.close();
            } catch (MessagingException e) {
                log.error("[EJM]: Store close operation failed, couldn't close store", e);
            }
        }
        // Recursively try to connect to email server and monitor
        if (!(stopMonitoring || ServerSettings.isStopAllThreads())) {
            log.info("[EJM]: Retry email monitoring on exceptions");
            run();
        }

    }

    private Message[] getMessagesToProcess() throws MessagingException {
        //TODO: improve this logic to get only previously unprocessed unread mails.
        SearchTerm unseenBefore = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
        log.info("[EJM]: Retrieving unseen emails");
        return emailFolder.search(unseenBefore);
    }

    private void processMessages(Message[] searchMessages) throws MessagingException {
        List<Message> processedMessages = Collections.synchronizedList(new ArrayList<>());
        List<Message> unreadMessages = Collections.synchronizedList(new ArrayList<>());
        List<Future<JobStatusResult>> futureList = new ArrayList<>();

        // use thread pool to increase email processing
        for (Message message : searchMessages) {
            Future<JobStatusResult> jobStatusFuture = executor.submit(new Callable<JobStatusResult>() {
                JobStatusResult jobStatusResult = null;
                @Override
                public JobStatusResult call() throws Exception {
                    try {
                        jobStatusResult = parse(message);
                        JobExecutionContext jEC = jobMonitorMap.get(jobStatusResult.getJobId());
                        if (jEC == null) {
                            jEC = jobMonitorMap.get(jobStatusResult.getJobName());
                        }
                        if (jEC != null) {
                            process(jobStatusResult, jEC);
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
                    return jobStatusResult;
                }
            });
            futureList.add(jobStatusFuture);
        }
        // wait until all thread returns
        for (Future<JobStatusResult> jobStatusResultFuture : futureList) {
            try {
                jobStatusResultFuture.get();
            } catch (InterruptedException e) {
                log.error("Error while calling future.get() ", e);
            } catch (ExecutionException e) {
                log.error("Error while calling future.get()", e);
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
                    try {
                        store.connect();
                        emailFolder.setFlags(unseenMessages, new Flags(Flags.Flag.SEEN), false);
                    } catch (MessagingException e1) {
                        flushUnseenMessages = unseenMessages; // anyway we need to push this update.
                        throw e1;
                    }

                }
            }
        }
    }

    private void process(JobStatusResult jobStatusResult, JobExecutionContext jEC){
        JobState resultState = jobStatusResult.getState();
        jEC.getJobDetails().setJobStatus(new JobStatus(resultState));
        boolean runOutHandlers = false;
        String jobDetails = "JobName : " + jobStatusResult.getJobName() + ", JobId : " + jobStatusResult.getJobId();
        // TODO - Handle all other valid JobStates
        if (resultState == JobState.COMPLETE) {
            jobMonitorMap.remove(jobStatusResult.getJobId());
            runOutHandlers = true;
            log.info("[EJM]: Job Complete email received , removed job from job monitoring. " + jobDetails);
        }else if (resultState == JobState.QUEUED) {
            // nothing special thing to do, update the status change to rabbit mq at the end of this method.
            log.info("[EJM]: Job Queued email received, " + jobDetails);
        }else if (resultState == JobState.ACTIVE) {
            // nothing special thing to do, update the status change to rabbit mq at the end of this method.
            log.info("[EJM]: Job Active email received, " + jobDetails);
        }else if (resultState == JobState.FAILED) {
            jobMonitorMap.remove(jobStatusResult.getJobId());
            runOutHandlers = true;
            log.info("[EJM]: Job failed email received , removed job from job monitoring. " + jobDetails);
            try {
                GFacUtils.saveErrorDetails(jEC,"Job runs on remote compute resource failed", CorrectiveAction.RETRY_SUBMISSION, ErrorCategory.APPLICATION_FAILURE);
            } catch (GFacException e) {
                log.info("[EJM]: Error while saving error details for jobId:{}, expId: {}", jEC.getJobDetails().getJobID(), jEC.getExperimentID());
            }
        }else if (resultState == JobState.CANCELED) {
            jobMonitorMap.remove(jobStatusResult.getJobId());
            runOutHandlers = false; // Do we need to run out handlers in canceled case?
            log.info("[EJM]: Job canceled mail received, removed job from job monitoring. " + jobDetails);

        }
        log.info("[EJM]: Publishing status changes to amqp. " + jobDetails);
        publishJobStatusChange(jEC);

        if (runOutHandlers) {
            log.info("[EJM]: Calling Out Handler chain of " + jobDetails);
            GFacThreadPoolExecutor.getCachedThreadPool().execute(new OutHandlerWorker(jEC, BetterGfacImpl.getMonitorPublisher()));
        }
    }

    private void publishJobStatusChange(JobExecutionContext jobExecutionContext) {
        JobStatusChangeRequestEvent jobStatus = new JobStatusChangeRequestEvent();
        JobIdentifier jobIdentity = new JobIdentifier(jobExecutionContext.getJobDetails().getJobID(),
                jobExecutionContext.getTaskData().getTaskID(),
                jobExecutionContext.getWorkflowNodeDetails().getNodeInstanceId(),
                jobExecutionContext.getExperimentID(),
                jobExecutionContext.getGatewayID());
        jobStatus.setJobIdentity(jobIdentity);
        jobStatus.setState(jobExecutionContext.getJobDetails().getJobStatus().getJobState());
        // we have this JobStatus class to handle amqp monitoring
        log.debugId(jobStatus.getJobIdentity().getJobId(), "[EJM]: Published job status(" +
                        jobExecutionContext.getJobDetails().getJobStatus().getJobState().toString() + ") change request, " +
                        "experiment {} , task {}", jobStatus.getJobIdentity().getExperimentId(),
                jobStatus.getJobIdentity().getTaskId());

        BetterGfacImpl.getMonitorPublisher().publish(jobStatus);
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
