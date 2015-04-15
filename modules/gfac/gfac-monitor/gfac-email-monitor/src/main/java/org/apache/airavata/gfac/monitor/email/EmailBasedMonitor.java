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
import org.apache.airavata.gfac.core.context.JobExecutionContext;
import org.apache.airavata.gfac.core.cpi.BetterGfacImpl;
import org.apache.airavata.gfac.core.utils.GFacThreadPoolExecutor;
import org.apache.airavata.gfac.core.utils.OutHandlerWorker;
import org.apache.airavata.gfac.monitor.email.parser.EmailParser;
import org.apache.airavata.gfac.monitor.email.parser.LonestarEmailParser;
import org.apache.airavata.gfac.monitor.email.parser.PBSEmailParser;
import org.apache.airavata.gfac.monitor.email.parser.SLURMEmailParser;
import org.apache.airavata.model.appcatalog.computeresource.EmailMonitorProperty;
import org.apache.airavata.model.appcatalog.computeresource.EmailProtocol;
import org.apache.airavata.model.messaging.event.JobIdentifier;
import org.apache.airavata.model.messaging.event.JobStatusChangeRequestEvent;
import org.apache.airavata.model.workspace.experiment.JobState;
import org.apache.airavata.model.workspace.experiment.JobStatus;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.FlagTerm;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class EmailBasedMonitor implements Runnable{

    private static final AiravataLogger log = AiravataLoggerFactory.getLogger(EmailBasedMonitor.class);

    private static final String PBS_CONSULT_SDSC_EDU = "pbsconsult@sdsc.edu";
    private static final String SLURM_BATCH_STAMPEDE = "slurm@batch1.stampede.tacc.utexas.edu";
    private static final String LONESTAR_ADDRESS = "root@c312-206.ls4.tacc.utexas.edu";
    private final EmailMonitorProperty emailMonitorProperty;
    private boolean stopMonitoring = false;

    private Session session ;
    private Store store;
    private Folder emailFolder;
//    private String host, emailAddress, password, folderName, mailStoreProtocol;
    private Properties properties;

    private Map<String, JobExecutionContext> jobMonitorMap = new ConcurrentHashMap<String, JobExecutionContext>();

    public EmailBasedMonitor(EmailMonitorProperty emailMonitorProp) {
        this.emailMonitorProperty = emailMonitorProp;
        init();
    }

    private void init() {
        properties = new Properties();
        properties.put("mail.store.protocol", emailMonitorProperty.getStoreProtocol());

    }

/*    public static EmailBasedMonitor getInstant(EmailMonitorProperty emailMonitorProp, MonitorPublisher monitorPublisher)
            throws ApplicationSettingsException {
        if (emailBasedMonitor == null) {
            synchronized (EmailBasedMonitor.class) {
                if (emailBasedMonitor == null) {
                    emailBasedMonitor = new EmailBasedMonitor(emailMonitorProp);
                    Thread thread = new Thread(emailBasedMonitor);
                    thread.start();
                }
            }
        }

        return emailBasedMonitor;
    }*/

    public void addToJobMonitorMap(JobExecutionContext jobExecutionContext) {
        addToJobMonitorMap(jobExecutionContext.getJobDetails().getJobID(), jobExecutionContext);
    }

    public void addToJobMonitorMap(String jobId, JobExecutionContext jobExecutionContext) {
        log.info("Added Job Id : " + jobId + " to email based monitor map");
        jobMonitorMap.put(jobId, jobExecutionContext);
    }

    private JobStatusResult parse(Message message) throws MessagingException, AiravataException {
        Address fromAddress = message.getFrom()[0];
        EmailParser emailParser;
        String addressStr = fromAddress.toString();
        switch (addressStr) {
            case PBS_CONSULT_SDSC_EDU:
                emailParser = new PBSEmailParser();
                break;
            case SLURM_BATCH_STAMPEDE:
                emailParser = new SLURMEmailParser();
                break;
            case LONESTAR_ADDRESS:
                emailParser = new LonestarEmailParser();
                break;
            default:
                throw new AiravataException("Un-handle address type for email monitoring -->  " + addressStr);
        }
        return emailParser.parseEmail(message);
    }

    @Override
    public void run() {
        try {
            session = Session.getDefaultInstance(properties);
            store = session.getStore(getProtocol(emailMonitorProperty.getStoreProtocol()));
            store.connect(emailMonitorProperty.getHost(), emailMonitorProperty.getEmailAddress(),
                    emailMonitorProperty.getPassword());
            while (!(stopMonitoring || ServerSettings.isStopAllThreads())) {
                if (!store.isConnected()) {
                    store.connect();
                }
                Thread.sleep(ServerSettings.getEmailMonitorPeriod());
                emailFolder = store.getFolder(emailMonitorProperty.getFolderName());
                emailFolder.open(Folder.READ_WRITE);
                Message[] searchMessages = emailFolder.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
                List<Message> processedMessages = new ArrayList<>();
                List<Message> unreadMessages = new ArrayList<>();
                for (Message message : searchMessages) {
                    try {
                        JobStatusResult jobStatusResult = parse(message);
                        JobExecutionContext jEC = jobMonitorMap.get(jobStatusResult.getJobId());
                        if (jEC != null) {
                            process(jobStatusResult, jEC);
                            processedMessages.add(message);
                        } else {
                            // we can get JobExecutionContext null in multiple Gfac instances environment,
                            // where this job is not submitted by this Gfac instance hence we ignore this message.
                            unreadMessages.add(message);
//                            log.info("JobExecutionContext is not found for job Id " + jobStatusResult.getJobId());
                        }
                    } catch (AiravataException e) {
                        log.error("Error parsing email message =====================================>", e);
                        try {
                            writeEnvelopeOnError(message);
                        } catch (MessagingException e1) {
                            log.error("Error printing envelop of the email");
                        }
                    }
                }
                if (!processedMessages.isEmpty()) {
                    Message[] seenMessages = new Message[processedMessages.size()];
                    processedMessages.toArray(seenMessages);
                    emailFolder.setFlags(seenMessages, new Flags(Flags.Flag.SEEN), true);

                }
                if (!unreadMessages.isEmpty()) {
                    Message[] unseenMessages = new Message[unreadMessages.size()];
                    unreadMessages.toArray(unseenMessages);
                    emailFolder.setFlags(unseenMessages, new Flags(Flags.Flag.SEEN), false);
                }
                emailFolder.close(false);
            }
        } catch (MessagingException e) {
            log.error("Couldn't connect to the store ", e);
        } catch (InterruptedException e) {
            log.error("Interrupt exception while sleep ", e);
        } catch (AiravataException e) {
            log.error("UnHandled arguments ", e);
        } finally {
            try {
                store.close();
            } catch (MessagingException e) {
                log.error("Store close operation failed, couldn't close store", e);
            }
        }
    }

    private String getProtocol(EmailProtocol storeProtocol) throws AiravataException {
        switch (storeProtocol) {
            case IMAPS:
                return "imaps";
            case POP3:
                return "pop3";
            default:
                throw new AiravataException("Unhandled Email store protocol ");
        }
    }
    private void process(JobStatusResult jobStatusResult, JobExecutionContext jEC){
        JobState resultState = jobStatusResult.getState();
        jEC.getJobDetails().setJobStatus(new JobStatus(resultState));
        if (resultState == JobState.COMPLETE) {
            GFacThreadPoolExecutor.getFixedThreadPool().submit(new OutHandlerWorker(jEC, BetterGfacImpl.getMonitorPublisher()));
        }else if (resultState == JobState.QUEUED) {
            // nothing special thing to do, update the status change to rabbit mq at the end of this method.
        }else if (resultState == JobState.FAILED) {
            jobMonitorMap.remove(jobStatusResult.getJobId());
            log.info("Job failed email received , removed job from job monitoring");
        }else if (resultState == JobState.CANCELED) {
            jobMonitorMap.remove(jobStatusResult.getJobId());
            log.info("Job canceled mail received, removed job from job monitoring");
            
        }
        publishJobStatusChange(jEC);
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
        log.debugId(jobStatus.getJobIdentity().getJobId(), "Published job status change request, " +
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
}
