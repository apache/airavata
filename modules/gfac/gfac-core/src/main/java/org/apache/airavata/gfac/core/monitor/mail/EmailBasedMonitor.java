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
package org.apache.airavata.gfac.core.monitor.mail;

import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.logger.AiravataLogger;
import org.apache.airavata.common.logger.AiravataLoggerFactory;
import org.apache.airavata.common.utils.MonitorPublisher;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.gfac.core.context.JobExecutionContext;
import org.apache.airavata.gfac.core.utils.GFacThreadPoolExecutor;
import org.apache.airavata.gfac.core.utils.OutHandlerWorker;
import org.apache.airavata.gfac.core.monitor.mail.parser.EmailParser;
import org.apache.airavata.gfac.core.monitor.mail.parser.PBSEmailParser;
import org.apache.airavata.gfac.core.monitor.mail.parser.SLURMEmailParser;
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
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class EmailBasedMonitor implements Runnable{

    private static final AiravataLogger log = AiravataLoggerFactory.getLogger(EmailBasedMonitor.class);

    private static final String PBS_CONSULT_SDSC_EDU = "pbsconsult@sdsc.edu";
    private static final String SLURM_BATCH_STAMPEDE = "slurm@batch1.stampede.tacc.utexas.edu";
    private static EmailBasedMonitor emailBasedMonitor;
    private final MonitorPublisher monitorPublisher;

    private Session session ;
    private Store store;
    private Folder emailFolder;
    private String host, emailAddress, password, folderName, mailStoreProtocol;
    private Properties properties;

    private Map<String, JobExecutionContext> jobMonitorMap = new ConcurrentHashMap<String, JobExecutionContext>();

    private EmailBasedMonitor(MonitorPublisher monitorPublisher) throws ApplicationSettingsException {
        this.monitorPublisher = monitorPublisher;
        init();
    }

    private void init() throws ApplicationSettingsException {
        host = ServerSettings.getEmailBasedMonitorHost();
        emailAddress = ServerSettings.getEmailBasedMonitorAddress();
        password = ServerSettings.getEmailBasedMonitorPassword();
        folderName = ServerSettings.getEmailBasedMonitorFolderName();
        mailStoreProtocol = ServerSettings.getEmailBasedMonitorStoreProtocol();

        properties = new Properties();
        properties.put("mail.store.protocol", mailStoreProtocol);

    }

    public static EmailBasedMonitor getInstant(MonitorPublisher monitorPublisher) throws ApplicationSettingsException {
        if (emailBasedMonitor == null) {
            synchronized (EmailBasedMonitor.class) {
                if (emailBasedMonitor == null) {
                    emailBasedMonitor = new EmailBasedMonitor(monitorPublisher);
                    Thread thread = new Thread(emailBasedMonitor);
                    thread.start();
                }
            }
        }

        return emailBasedMonitor;
    }

    public void addToJobMonitorMap(JobExecutionContext jobExecutionContext) {
        addToJobMonitorMap(jobExecutionContext.getJobDetails().getJobID(), jobExecutionContext);
    }

    public void addToJobMonitorMap(String jobId, JobExecutionContext jobExecutionContext) {
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
            default:
                throw new AiravataException("Un-handle address type for email monitoring -->  " + addressStr);
        }
        return emailParser.parseEmail(message);
    }

    @Override
    public void run() {
        try {
            session = Session.getDefaultInstance(properties);
            store = session.getStore(mailStoreProtocol);
            store.connect(host, emailAddress, password);
            while (!ServerSettings.isStopAllThreads()) {
                if (!store.isConnected()) {
                    store.connect();
                }
                Thread.sleep(2000);
                emailFolder = store.getFolder(folderName);
                emailFolder.open(Folder.READ_WRITE);
                Message[] searchMessages = emailFolder.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
                for (Message message : searchMessages) {
                    try {
                        JobStatusResult jobStatusResult = parse(message);
                        process(jobStatusResult);
                    } catch (AiravataException e) {
                        log.error("Error parsing email message =====================================>", e);
                        try {
                            writeEnvelopeOnError(message);
                        } catch (MessagingException e1) {
                            log.error("Error printing envelop of the email");
                        }

                    }
                }
                emailFolder.setFlags(searchMessages, new Flags(Flags.Flag.SEEN), true);
                emailFolder.close(false);
            }

        } catch (MessagingException e) {
            log.error("Couldn't connect to the store ", e);
        } catch (InterruptedException e) {
            log.error("Interrupt exception while sleep ", e);
        } finally {
            try {
                store.close();
            } catch (MessagingException e) {
                log.error("Store close operation failed, couldn't close store", e);
            }
        }
    }

    private void process(JobStatusResult jobStatusResult) throws AiravataException {
        JobExecutionContext jEC = jobMonitorMap.get(jobStatusResult.getJobId());
        if (jEC == null) {
            throw new AiravataException("JobExecutionContext is not found for job Id " + jobStatusResult.getJobId());
        }
        JobState resultState = jobStatusResult.getState();
        jEC.getJobDetails().setJobStatus(new JobStatus(resultState));
        if (resultState == JobState.COMPLETE) {
            GFacThreadPoolExecutor.getFixedThreadPool().submit(new OutHandlerWorker(jEC, monitorPublisher));
        }else if (resultState == JobState.QUEUED) {
            // TODO - publish queued rabbitmq message
        }else if (resultState == JobState.FAILED) {
            // TODO - handle failed scenario
            jobMonitorMap.remove(jobStatusResult.getJobId());
            log.info("Job failed email received , removed job from job monitoring");
//            monitorPublisher.publish(jEC.getJobDetails().getJobStatus());
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

        monitorPublisher.publish(jobStatus);
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
}
