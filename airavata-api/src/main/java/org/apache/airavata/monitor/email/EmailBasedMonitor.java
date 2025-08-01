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
import java.util.*;
import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.utils.ApplicationSettings;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.model.appcatalog.computeresource.ResourceJobManagerType;
import org.apache.airavata.monitor.AbstractMonitor;
import org.apache.airavata.monitor.JobStatusResult;
import org.apache.airavata.monitor.email.parser.EmailParser;
import org.apache.airavata.monitor.email.parser.ResourceConfig;
import org.apache.airavata.registry.api.RegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

public class EmailBasedMonitor extends AbstractMonitor implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(EmailBasedMonitor.class);

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

    public EmailBasedMonitor() throws Exception {
        init();
        populateAddressAndParserMap(resourceConfigs);
    }

    private void init() throws Exception {
        loadContext();
        host = ServerSettings.getEmailBasedMonitorHost();
        emailAddress = ServerSettings.getEmailBasedMonitorAddress();
        password = ServerSettings.getEmailBasedMonitorPassword();
        storeProtocol = ServerSettings.getEmailBasedMonitorStoreProtocol();
        folderName = ServerSettings.getEmailBasedMonitorFolderName();
        emailExpirationTimeMinutes = Long.parseLong(ServerSettings.getSetting("email.expiration.minutes"));
        publisherId = ServerSettings.getSetting("job.monitor.email.publisher.id");
        if (!(storeProtocol.equals(IMAPS) || storeProtocol.equals(POP3))) {
            throw new AiravataException(
                    "Unsupported store protocol , expected " + IMAPS + " or " + POP3 + " but found " + storeProtocol);
        }
        properties = new Properties();
        properties.put("mail.store.protocol", storeProtocol);
    }

    private void loadContext() throws Exception {
        Yaml yaml = new Yaml();
        InputStream emailConfigStream =
                ApplicationSettings.loadFile("email-config.yml").openStream();
        Object load = yaml.load(emailConfigStream);

        if (load == null) {
            throw new Exception("Could not load the configuration");
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
        populateAddressAndParserMap(resourceConfigs);
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
                    Class<? extends EmailParser> emailParserClass =
                            Class.forName(config.getEmailParser()).asSubclass(EmailParser.class);
                    EmailParser emailParser = emailParserClass.getConstructor().newInstance();
                    emailParserMap.put(type, emailParser);
                } catch (Exception e) {
                    throw new AiravataException("Error while instantiation email parsers", e);
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
        RegistryService.Client regClient = getRegistryClientPool().getResource();

        try {
            JobStatusResult jobStatusResult = emailParser.parseEmail(message, regClient);
            jobStatusResult.setPublisherName(publisherId);
            var jobId = jobStatusResult.getJobId();
            var jobName = jobStatusResult.getJobName();
            var jobStatus = jobStatusResult.getState().getValue();
            log.info("Parsed Job Status: From=[{}], Id={}, Name={}, State={}", publisherId, jobId, jobName, jobStatus);
            return jobStatusResult;
        } catch (Exception e) {
            getRegistryClientPool().returnBrokenResource(regClient);
            throw e;
        } finally {
            getRegistryClientPool().returnResource(regClient);
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
    public void run() {

        while (!ServerSettings.isStopAllThreads()) {
            try {
                Session session = Session.getDefaultInstance(properties);
                store = session.getStore(storeProtocol);
                store.connect(host, emailAddress, password);
                emailFolder = store.getFolder(folderName);
                // first we search for all unread messages.
                SearchTerm unseenBefore = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
                while (!ServerSettings.isStopAllThreads()) {
                    Thread.sleep(ServerSettings.getEmailMonitorPeriod()); // sleep for long enough
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
            } catch (InterruptedException e) {
                log.error("[EJM]: Interrupt exception while sleep ", e);
            } catch (AiravataException e) {
                log.error("[EJM]: UnHandled arguments ", e);
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
    }

    private void processMessages(Message[] searchMessages) throws MessagingException {
        List<Message> processedMessages = new ArrayList<>();
        List<Message> unreadMessages = new ArrayList<>();
        for (Message message : searchMessages) {
            var msgHash = message.hashCode();
            try {
                JobStatusResult jobStatusResult = parse(message, publisherId);
                log.info("read JobStatusUpdate<{}> from {}: {}", msgHash, publisherId, jobStatusResult);
                submitJobStatus(jobStatusResult);
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

    public void startServer() throws InterruptedException {
        Thread t = new Thread(this);
        t.start();
        t.join();
    }

    public static void main(String[] args) throws Exception {
        EmailBasedMonitor monitor = new EmailBasedMonitor();
        monitor.startServer();
    }
}
