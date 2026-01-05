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
package org.apache.airavata.monitor.email;

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
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.model.ResourceJobManagerType;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.common.utils.ShutdownFlag;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.config.ServerLifecycle;
import org.apache.airavata.monitor.AbstractMonitor;
import org.apache.airavata.monitor.JobStatusResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

@Component
@Profile("!test")
@ConditionalOnProperty(name = "services.monitor.email.enabled", havingValue = "true", matchIfMissing = true)
public class EmailBasedMonitor extends ServerLifecycle {

    private static final Logger log = LoggerFactory.getLogger(EmailBasedMonitor.class);

    private final AiravataServerProperties airavataProperties;
    private final org.apache.airavata.service.registry.RegistryService registryService;
    private final ApplicationContext applicationContext;
    private final AbstractMonitor abstractMonitor;

    private static final String IMAPS = "imaps";
    private static final String POP3 = "pop3";

    private Store store;
    private Folder emailFolder;
    private Properties properties;
    private String host, emailAddress, password, storeProtocol, folderName;
    private final Map<ResourceJobManagerType, EmailParser> emailParserMap = new HashMap<>();
    private final Map<String, ResourceJobManagerType> addressMap = new HashMap<>();
    private Message[] flushUnseenMessages;
    private final Map<ResourceJobManagerType, ResourceConfig> resourceConfigs = new HashMap<>();
    private long emailExpirationTimeMinutes;
    private String publisherId;
    private Thread emailThread;

    public EmailBasedMonitor(
            org.apache.airavata.service.registry.RegistryService registryService,
            AiravataServerProperties airavataProperties,
            ApplicationContext applicationContext)
            throws Exception {
        this.registryService = registryService;
        this.airavataProperties = airavataProperties;
        this.applicationContext = applicationContext;
        this.abstractMonitor = new AbstractMonitor(registryService, airavataProperties);
        // Don't initialize here - wait for @PostConstruct when properties are injected
    }

    @jakarta.annotation.PostConstruct
    public void init() {
        try {
            loadContext();
        } catch (Exception e) {
            log.error("Error loading email context", e);
            throw new RuntimeException("Failed to initialize EmailBasedMonitor", e);
        }
        host = airavataProperties.services.monitor.email.host;
        emailAddress = airavataProperties.services.monitor.email.address;
        password = airavataProperties.services.monitor.email.password;
        storeProtocol = airavataProperties.services.monitor.email.storeProtocol;
        folderName = airavataProperties.services.monitor.email.folderName;
        emailExpirationTimeMinutes = airavataProperties.services.monitor.email.expiryMins;
        publisherId = airavataProperties.services.monitor.compute.emailPublisherId;
        if (!(storeProtocol.equals(IMAPS) || storeProtocol.equals(POP3))) {
            throw new RuntimeException(
                    "Unsupported store protocol , expected " + IMAPS + " or " + POP3 + " but found " + storeProtocol);
        }
        properties = new Properties();
        properties.put("mail.store.protocol", storeProtocol);
        try {
            populateAddressAndParserMap(resourceConfigs);
        } catch (Exception e) {
            log.error("Error populating address and parser map", e);
            throw new RuntimeException("Failed to initialize EmailBasedMonitor", e);
        }
    }

    private void loadContext() throws Exception {
        Yaml yaml = new Yaml();
        java.net.URL emailConfigUrl = AiravataServerProperties.loadFile("email-config.yml");
        if (emailConfigUrl == null) {
            log.warn("email-config.yml not found. Email monitoring will use default configuration.");
            return; // Use default configuration
        }
        InputStream emailConfigStream = emailConfigUrl.openStream();
        Object load = yaml.load(emailConfigStream);

        if (load == null) {
            log.warn("Could not load email-config.yml. Email monitoring will use default configuration.");
            return; // Use default configuration
        }

        if (load instanceof Map) {
            Map<String, Object> loadMap = (Map<String, Object>) load;
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
        // populateAddressAndParserMap will be called from init() after properties are loaded
    }

    private void populateAddressAndParserMap(Map<ResourceJobManagerType, ResourceConfig> resourceConfigs)
            throws AiravataException {
        for (Map.Entry<ResourceJobManagerType, ResourceConfig> resourceConfigEntry : resourceConfigs.entrySet()) {
            ResourceJobManagerType type = resourceConfigEntry.getKey();
            ResourceConfig config = resourceConfigEntry.getValue();
            List<String> resourceEmailAddresses = config.getResourceEmailAddresses();
            if (resourceEmailAddresses != null && !resourceEmailAddresses.isEmpty()) {
                for (String resourceEmailAddress : resourceEmailAddresses) {
                    addressMap.put(resourceEmailAddress, type);
                }
                try {
                    // Extract simple class name from full class name
                    String parserClassName = config.getEmailParser();
                    String simpleClassName = parserClassName.substring(parserClassName.lastIndexOf('.') + 1);
                    // Spring default bean name is simple class name with first letter lowercase
                    String beanName = simpleClassName.substring(0, 1).toLowerCase() + simpleClassName.substring(1);

                    EmailParser emailParser = applicationContext.getBean(beanName, EmailParser.class);
                    emailParserMap.put(type, emailParser);
                } catch (org.springframework.beans.factory.NoSuchBeanDefinitionException e) {
                    throw new AiravataException(
                            "EmailParser bean not found: " + config.getEmailParser()
                                    + ". Make sure it's a Spring bean with @Component annotation.",
                            e);
                } catch (Exception e) {
                    throw new AiravataException("Error while getting email parser bean: " + config.getEmailParser(), e);
                }
            }
        }
    }

    public void monitor(String jobId) {
        log.info("[EJM]: Added monitor Id : {} to email based monitor map", jobId);
    }

    private JobStatusResult parse(Message message, String publisherId) throws MessagingException, AiravataException {
        Address fromAddress = message.getFrom()[0];
        String addressStr = fromAddress.toString();
        ResourceJobManagerType jobMonitorType = getJobMonitorType(addressStr);
        EmailParser emailParser = emailParserMap.get(jobMonitorType);
        if (emailParser == null) {
            throw new AiravataException("[EJM]: Un-handle resource job manager type: " + jobMonitorType.toString()
                    + " for email monitoring -->  " + addressStr);
        }
        try {
            JobStatusResult jobStatusResult = emailParser.parseEmail(message, getRegistryService());
            jobStatusResult.setPublisherName(publisherId);
            var jobId = jobStatusResult.getJobId();
            var jobName = jobStatusResult.getJobName();
            var jobStatus = jobStatusResult.getState().getValue();
            log.info("Parsed Job Status: From=[{}], Id={}, Name={}, State={}", publisherId, jobId, jobName, jobStatus);
            return jobStatusResult;
        } catch (Exception e) {
            throw e;
        }
    }

    private ResourceJobManagerType getJobMonitorType(String addressStr) throws AiravataException {
        for (Map.Entry<String, ResourceJobManagerType> addressEntry : addressMap.entrySet()) {
            if (addressStr.contains(addressEntry.getKey())) {
                return addressEntry.getValue();
            }
        }
        throw new AiravataException("[EJM]: Couldn't identify Resource job manager type from address " + addressStr);
    }

    @Override
    public String getServerName() {
        return "Email Monitor";
    }

    @Override
    public String getServerVersion() {
        return "1.0";
    }

    @Override
    public int getPhase() {
        return 40; // Start after realtime monitor
    }

    @Override
    protected void doStart() throws Exception {
        emailThread = new Thread(() -> {
            while (!ShutdownFlag.isStopAllThreads() && !Thread.currentThread().isInterrupted()) {
                try {
                    Session session = Session.getDefaultInstance(properties);
                    store = session.getStore(storeProtocol);
                    store.connect(host, emailAddress, password);
                    emailFolder = store.getFolder(folderName);
                    // first we search for all unread messages.
                    SearchTerm unseenBefore = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
                    while (!ShutdownFlag.isStopAllThreads()
                            && !Thread.currentThread().isInterrupted()) {
                        Thread.sleep(airavataProperties.services.monitor.email.period); // sleep for long enough
                        if (!store.isConnected()) {
                            store.connect();
                            emailFolder = store.getFolder(folderName);
                        }
                        log.info("[EJM]: Retrieving unseen emails");
                        if (emailFolder == null) {
                            return;
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
                            Message[] searchMessages = emailFolder.search(unseenBefore);
                            if (searchMessages == null || searchMessages.length == 0) {
                                log.info("[EJM]: No new email messages");
                            } else {
                                log.info("[EJM]: {} new email/s received", searchMessages.length);
                                processMessages(searchMessages);
                            }
                            emailFolder.close(false);
                        }
                    }
                } catch (MessagingException e) {
                    log.error("[EJM]: Couldn't connect to the store ", e);
                    try {
                        Thread.sleep(airavataProperties.services.monitor.email.connectionRetryInterval);
                    } catch (InterruptedException ie) {
                        log.error("[EJM]: Interrupted while waiting before retry", ie);
                        Thread.currentThread().interrupt();
                    }
                } catch (InterruptedException e) {
                    log.error("[EJM]: Interrupt exception while sleep ", e);
                } catch (Throwable e) {
                    log.error("[EJM]: Caught a throwable ", e);
                } finally {
                    try {
                        if (emailFolder != null) {
                            emailFolder.close(false);
                        }
                        if (store != null) {
                            store.close();
                        }
                    } catch (MessagingException e) {
                        log.error("[EJM]: Store close operation failed, couldn't close store", e);
                    } catch (Throwable e) {
                        log.error("[EJM]: Caught a throwable while closing email store ", e);
                    }
                }
            }
            log.info("[EJM]: Email monitoring daemon stopped");
        });
        emailThread.setName("EmailBasedMonitor-Worker");
        emailThread.setDaemon(true);
        emailThread.start();
    }

    @Override
    protected void doStop() throws Exception {
        ShutdownFlag.setStopAllThreads(true);
        if (emailThread != null) {
            emailThread.interrupt();
            try {
                emailThread.join(5000); // Wait up to 5 seconds
            } catch (InterruptedException e) {
                log.warn("Interrupted while waiting for email thread to stop", e);
                Thread.currentThread().interrupt();
            }
        }
        // Close email connections
        try {
            if (emailFolder != null) {
                emailFolder.close(false);
            }
            if (store != null) {
                store.close();
            }
        } catch (MessagingException e) {
            log.warn("Error closing email store", e);
        }
    }

    @Override
    public boolean isRunning() {
        // ServerLifecycle provides the base isRunning() implementation
        // Check if the email thread is also alive
        return super.isRunning() && emailThread != null && emailThread.isAlive();
    }

    protected org.apache.airavata.service.registry.RegistryService getRegistryService() {
        return registryService;
    }

    private void processMessages(Message[] searchMessages) throws MessagingException {
        List<Message> processedMessages = new ArrayList<>();
        List<Message> unreadMessages = new ArrayList<>();
        for (Message message : searchMessages) {
            var msgHash = message.hashCode();
            try {
                JobStatusResult jobStatusResult = parse(message, publisherId);
                log.info("read JobStatusUpdate<{}> from {}: {}", msgHash, publisherId, jobStatusResult);
                abstractMonitor.submitJobStatus(jobStatusResult);
                processedMessages.add(message);
            } catch (Exception e) {
                var msgTime = message.getReceivedDate().getTime();
                var msgExpiryTime =
                        msgTime + Duration.ofMinutes(emailExpirationTimeMinutes).toMillis();
                if (System.currentTimeMillis() > msgExpiryTime) {
                    processedMessages.add(message);
                    log.error("cannot read JobStatusUpdate<{}> from {}. marked as timeout", msgHash, publisherId, e);
                } else {
                    log.error("cannot read JobStatusUpdate<{}> from {}. marked as requeue", msgHash, publisherId, e);
                    unreadMessages.add(message);
                }
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
                // anyway we need to push this update.
                if (!store.isConnected()) {
                    store.connect();
                    emailFolder.setFlags(unseenMessages, new Flags(Flags.Flag.SEEN), false);
                }
                flushUnseenMessages = unseenMessages;
            }
        }
    }
}
