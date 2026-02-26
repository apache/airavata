/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.execution.monitoring;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import io.temporal.activity.ActivityOptions;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.common.RetryOptions;
import io.temporal.failure.ActivityFailure;
import io.temporal.spring.boot.ActivityImpl;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import jakarta.annotation.PostConstruct;
import jakarta.mail.Address;
import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.search.FlagTerm;
import jakarta.mail.search.SearchTerm;
import java.io.InputStream;
import java.io.Serializable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.airavata.compute.provider.slurm.ResourceConfig;
import org.apache.airavata.compute.provider.slurm.SLURMEmailParser;
import org.apache.airavata.compute.resource.model.ResourceJobManagerType;
import org.apache.airavata.compute.resource.service.JobService;
import org.apache.airavata.config.ConfigResolver;
import org.apache.airavata.config.ServerProperties;
import org.apache.airavata.core.exception.CoreExceptions.AiravataException;
import org.apache.airavata.core.util.IdGenerator;
import org.apache.airavata.execution.activity.ProcessActivity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

/**
 * Temporal workflow for email-based job status monitoring.
 *
 * <p>Replaces the former daemon thread with a durable Temporal workflow
 * that sleeps between polls and calls {@link MonitorActivities#pollEmails()}
 * as a retryable activity. Uses {@code continueAsNew} to bound history.
 */
public class EmailMonitorWorkflow {

    public static final String TASK_QUEUE = ProcessActivity.TASK_QUEUE;
    private static final int MAX_ITERATIONS_BEFORE_CONTINUE_AS_NEW = 100;

    // -------------------------------------------------------------------------
    // Workflow interface
    // -------------------------------------------------------------------------

    @WorkflowInterface
    public interface MonitorWf {
        @WorkflowMethod
        void run(MonitorInput input);
    }

    public record MonitorInput(long pollIntervalMs, long connectionRetryMs) implements Serializable {}

    // -------------------------------------------------------------------------
    // Activity interface
    // -------------------------------------------------------------------------

    @ActivityInterface
    public interface MonitorActivities {
        @ActivityMethod
        void pollEmails();
    }

    // -------------------------------------------------------------------------
    // Workflow implementation
    // -------------------------------------------------------------------------

    @WorkflowImpl(taskQueues = TASK_QUEUE)
    public static class MonitorWfImpl implements MonitorWf {
        private final MonitorActivities activities = Workflow.newActivityStub(
                MonitorActivities.class,
                ActivityOptions.newBuilder()
                        .setStartToCloseTimeout(Duration.ofMinutes(5))
                        .setRetryOptions(RetryOptions.newBuilder()
                                .setMaximumAttempts(3)
                                .setInitialInterval(Duration.ofSeconds(10))
                                .setMaximumInterval(Duration.ofSeconds(60))
                                .setBackoffCoefficient(2.0)
                                .build())
                        .build());

        @Override
        public void run(MonitorInput input) {
            for (int i = 0; i < MAX_ITERATIONS_BEFORE_CONTINUE_AS_NEW; i++) {
                Workflow.sleep(Duration.ofMillis(input.pollIntervalMs()));
                try {
                    activities.pollEmails();
                } catch (ActivityFailure e) {
                    // Activity exhausted retries — log and continue polling
                }
            }
            Workflow.continueAsNew(input);
        }
    }

    // -------------------------------------------------------------------------
    // Activity implementation (Spring-managed, full DI)
    // -------------------------------------------------------------------------

    @ConditionalOnProperty(prefix = "airavata.services.monitor.email", name = "enabled", havingValue = "true")
    @Profile("!test")
    @Component
    @ActivityImpl(taskQueues = TASK_QUEUE)
    public static class MonitorActivitiesImpl implements MonitorActivities {

        private static final Logger log = LoggerFactory.getLogger(MonitorActivitiesImpl.class);
        private static final String IMAPS = "imaps";
        private static final String POP3 = "pop3";

        private final ServerProperties airavataProperties;
        private final JobService jobService;
        private final ApplicationContext applicationContext;
        private final JobStatusMonitor jobStatusMonitor;

        private String host, emailAddress, password, storeProtocol, folderName, publisherId;
        private Properties mailProperties;
        private long emailExpirationTimeMinutes;
        private final Map<ResourceJobManagerType, SLURMEmailParser> emailParserMap = new HashMap<>();
        private final Map<String, ResourceJobManagerType> addressMap = new HashMap<>();
        private final Map<ResourceJobManagerType, ResourceConfig> resourceConfigs = new HashMap<>();

        public MonitorActivitiesImpl(
                JobService jobService, ServerProperties airavataProperties, ApplicationContext applicationContext) {
            this.jobService = jobService;
            this.airavataProperties = airavataProperties;
            this.applicationContext = applicationContext;
            JobStatusMonitor monitor = null;
            try {
                monitor = applicationContext.getBean(JobStatusMonitor.class);
            } catch (Exception ignored) {
            }
            this.jobStatusMonitor = monitor;
        }

        @PostConstruct
        public void init() {
            try {
                loadContext();
            } catch (Exception e) {
                log.error("Error loading email context", e);
                throw new RuntimeException("Failed to initialize email monitor", e);
            }
            host = airavataProperties.services().monitor().email().host();
            emailAddress = airavataProperties.services().monitor().email().address();
            password = airavataProperties.services().monitor().email().password();
            storeProtocol = airavataProperties.services().monitor().email().storeProtocol();
            folderName = airavataProperties.services().monitor().email().folderName();
            emailExpirationTimeMinutes =
                    airavataProperties.services().monitor().email().expiryMins();
            publisherId = airavataProperties.services().monitor().compute().emailPublisherId();
            if (!(storeProtocol.equals(IMAPS) || storeProtocol.equals(POP3))) {
                throw new RuntimeException("Unsupported store protocol, expected " + IMAPS + " or " + POP3
                        + " but found " + storeProtocol);
            }
            mailProperties = new Properties();
            mailProperties.put("mail.store.protocol", storeProtocol);
            try {
                populateAddressAndParserMap();
            } catch (Exception e) {
                log.error("Error populating address and parser map", e);
                throw new RuntimeException("Failed to initialize email monitor", e);
            }
        }

        @Override
        public void pollEmails() {
            Store store = null;
            Folder folder = null;
            try {
                Session session = Session.getDefaultInstance(mailProperties);
                store = session.getStore(storeProtocol);
                store.connect(host, emailAddress, password);
                folder = store.getFolder(folderName);
                folder.open(Folder.READ_WRITE);

                SearchTerm unseenBefore = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
                Message[] messages = folder.search(unseenBefore);

                if (messages == null || messages.length == 0) {
                    log.info("[EJM]: No new email messages");
                    return;
                }

                log.info("[EJM]: {} new email/s received", messages.length);
                processMessages(messages, folder, store);

            } catch (Exception e) {
                log.error("[EJM]: Error during email poll", e);
                throw new RuntimeException("Email poll failed", e);
            } finally {
                try {
                    if (folder != null && folder.isOpen()) {
                        folder.close(false);
                    }
                } catch (MessagingException e) {
                    log.warn("[EJM]: Error closing folder", e);
                }
                try {
                    if (store != null && store.isConnected()) {
                        store.close();
                    }
                } catch (MessagingException e) {
                    log.warn("[EJM]: Error closing store", e);
                }
            }
        }

        // ----- Private helpers (moved from old daemon) -----

        @SuppressWarnings("unchecked")
        private void loadContext() throws Exception {
            Yaml yaml = new Yaml();
            java.net.URL emailConfigUrl = ConfigResolver.loadFile("email-config.yml");
            InputStream emailConfigStream = emailConfigUrl.openStream();
            Object load = yaml.load(emailConfigStream);

            if (load == null) {
                log.warn("Could not load email-config.yml. Email monitoring will use default configuration.");
                return;
            }

            if (load instanceof Map<?, ?> rawMap) {
                Map<String, Object> loadMap = (Map<String, Object>) rawMap;
                Map<String, Object> configMap = (Map<String, Object>) loadMap.get("config");
                List<Map<String, Object>> resourceObjs = (List<Map<String, Object>>) configMap.get("resources");
                if (resourceObjs != null) {
                    resourceObjs.forEach(resource -> {
                        ResourceConfig resourceConfig = new ResourceConfig();
                        String identifier = resource.get("jobManagerType").toString();
                        resourceConfig.setJobManagerType(ResourceJobManagerType.valueOf(identifier));
                        Object emailParser = resource.get("emailParser");
                        if (emailParser != null) {
                            resourceConfig.setEmailParser(emailParser.toString());
                        }
                        List<String> emailAddressList = (List<String>) resource.get("resourceEmailAddresses");
                        resourceConfig.setResourceEmailAddresses(emailAddressList);
                        resourceConfigs.put(resourceConfig.getJobManagerType(), resourceConfig);
                    });
                }
            }
        }

        private void populateAddressAndParserMap() throws AiravataException {
            for (Map.Entry<ResourceJobManagerType, ResourceConfig> entry : resourceConfigs.entrySet()) {
                ResourceJobManagerType type = entry.getKey();
                ResourceConfig config = entry.getValue();
                List<String> resourceEmailAddresses = config.getResourceEmailAddresses();
                if (resourceEmailAddresses != null && !resourceEmailAddresses.isEmpty()) {
                    for (String addr : resourceEmailAddresses) {
                        addressMap.put(addr, type);
                    }
                    try {
                        String parserClassName = config.getEmailParser();
                        String simpleClassName = parserClassName.substring(parserClassName.lastIndexOf('.') + 1);
                        String beanName = simpleClassName.substring(0, 1).toLowerCase() + simpleClassName.substring(1);
                        SLURMEmailParser emailParser = applicationContext.getBean(beanName, SLURMEmailParser.class);
                        emailParserMap.put(type, emailParser);
                    } catch (org.springframework.beans.factory.NoSuchBeanDefinitionException e) {
                        throw new AiravataException("SLURMEmailParser bean not found: " + config.getEmailParser(), e);
                    } catch (Exception e) {
                        throw new AiravataException("Error getting email parser bean: " + config.getEmailParser(), e);
                    }
                }
            }
        }

        private void processMessages(Message[] searchMessages, Folder folder, Store store) throws MessagingException {
            List<Message> processedMessages = new ArrayList<>();
            for (Message message : searchMessages) {
                var msgHash = message.hashCode();
                try {
                    JobStatusResult jobStatusResult = parse(message);
                    log.info("read JobStatusUpdate<{}> from {}: {}", msgHash, publisherId, jobStatusResult);
                    if (jobStatusMonitor != null) {
                        jobStatusMonitor.publish(jobStatusResult);
                    } else {
                        log.warn(
                                "[EJM]: JobStatusMonitor not available; dropping result for job {}",
                                jobStatusResult.getJobId());
                    }
                    processedMessages.add(message);
                } catch (Exception e) {
                    var msgTime = message.getReceivedDate().getTime();
                    var msgExpiryTime = msgTime
                            + Duration.ofMinutes(emailExpirationTimeMinutes).toMillis();
                    if (IdGenerator.getUniqueTimestamp().getTime() > msgExpiryTime) {
                        processedMessages.add(message);
                        log.error(
                                "cannot read JobStatusUpdate<{}> from {}. marked as timeout", msgHash, publisherId, e);
                    } else {
                        log.error(
                                "cannot read JobStatusUpdate<{}> from {}. marked as requeue", msgHash, publisherId, e);
                    }
                }
            }
            if (!processedMessages.isEmpty()) {
                Message[] seenMessages = processedMessages.toArray(new Message[0]);
                try {
                    folder.setFlags(seenMessages, new Flags(Flags.Flag.SEEN), true);
                } catch (MessagingException e) {
                    if (!store.isConnected()) {
                        store.connect();
                        folder.setFlags(seenMessages, new Flags(Flags.Flag.SEEN), true);
                    }
                }
            }
        }

        private JobStatusResult parse(Message message) throws MessagingException, AiravataException {
            Address fromAddress = message.getFrom()[0];
            String addressStr = fromAddress.toString();
            ResourceJobManagerType jobMonitorType = getJobMonitorType(addressStr);
            SLURMEmailParser emailParser = emailParserMap.get(jobMonitorType);
            if (emailParser == null) {
                throw new AiravataException("[EJM]: Unhandled resource job manager type: " + jobMonitorType
                        + " for email monitoring --> " + addressStr);
            }
            JobStatusResult jobStatusResult = emailParser.parseEmail(message, jobService);
            jobStatusResult.setPublisherName(publisherId);
            log.info(
                    "Parsed Job Status: From=[{}], Id={}, Name={}, State={}",
                    publisherId,
                    jobStatusResult.getJobId(),
                    jobStatusResult.getJobName(),
                    jobStatusResult.getState());
            return jobStatusResult;
        }

        private ResourceJobManagerType getJobMonitorType(String addressStr) throws AiravataException {
            for (Map.Entry<String, ResourceJobManagerType> entry : addressMap.entrySet()) {
                if (addressStr.contains(entry.getKey())) {
                    return entry.getValue();
                }
            }
            throw new AiravataException(
                    "[EJM]: Couldn't identify Resource job manager type from address " + addressStr);
        }
    }

    // -------------------------------------------------------------------------
    // Launcher — starts the workflow on application boot
    // -------------------------------------------------------------------------

    @ConditionalOnProperty(prefix = "airavata.services.monitor.email", name = "enabled", havingValue = "true")
    @Profile("!test")
    @Component
    public static class EmailMonitorLauncher {

        private static final Logger log = LoggerFactory.getLogger(EmailMonitorLauncher.class);
        private final WorkflowClient workflowClient;
        private final ServerProperties properties;

        public EmailMonitorLauncher(WorkflowClient workflowClient, ServerProperties properties) {
            this.workflowClient = workflowClient;
            this.properties = properties;
        }

        @EventListener(ApplicationStartedEvent.class)
        public void startEmailMonitor() {
            try {
                MonitorWf workflow = workflowClient.newWorkflowStub(
                        MonitorWf.class,
                        WorkflowOptions.newBuilder()
                                .setWorkflowId("email-monitor")
                                .setTaskQueue(TASK_QUEUE)
                                .build());

                long pollInterval = properties.services().monitor().email().period();
                long retryInterval = properties.services().monitor().email().connectionRetryInterval();

                WorkflowClient.start(workflow::run, new MonitorInput(pollInterval, retryInterval));
                log.info("Started email monitor Temporal workflow");
            } catch (Exception e) {
                log.error("Failed to start email monitor workflow", e);
            }
        }
    }
}
