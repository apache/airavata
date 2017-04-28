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
package org.apache.airavata.monitoring;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.monitoring.mailbox.MailConfig;

import java.util.Properties;

public class Util {
    /**
     * Fetch mail configurations.
     *
     * @return MailConfig
     */
    public static MailConfig getMailConfig() throws ApplicationSettingsException {
        MailConfig mailConfig = new MailConfig();
//        mailConfig.setHost(ServerSettings.getEmailBasedMonitorHost());
//        mailConfig.setUser(ServerSettings.getEmailBasedMonitorAddress());
//        mailConfig.setPassword(ServerSettings.getEmailBasedMonitorPassword());
//        mailConfig.setStoreProtocol(ServerSettings.getEmailBasedMonitorStoreProtocol());
//        mailConfig.setFolder(ServerSettings.getEmailBasedMonitorFolderName());
//        mailConfig.setPollingInterval(ServerSettings.getEmailMonitorPeriod());
        mailConfig.setHost("smtp.gmail.com");
        mailConfig.setUser("test.airavata@gmail.com");
        mailConfig.setPassword("airavata");
        mailConfig.setStoreProtocol("imaps");
        mailConfig.setFolder("inbox");
        mailConfig.setPollingInterval(1000);
//        Properties props = new Properties();
//        props.setProperty("mail.store.protocol", "imaps");
//        props.setProperty("mail.smtp.host", "smtp.gmail.com");
//        props.setProperty("mail.smtp.socketFactory.port", "993");
//        props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
//        props.setProperty("mail.smtp.port", "993");
//        props.setProperty("mail.userID", "test.airavata@gmail.com");
//        props.setProperty("mail.password", "airavata");
        return mailConfig;
    }

    /**
     * Fetch Broker Properties. Will be reworked to fetch the properties from properties file.
     *
     * @return
     */
    public static Properties getBrokerProperties() {
        Properties props = new Properties();
        props.setProperty("monitor.email.exchange.name", "monitor");
        props.setProperty("monitor.email.broker.URI", "amqp://localhost:5672");
        props.setProperty("monitor.email.broker.queue1.name", "q1");
        props.setProperty("monitor.email.broker.queue2.name", "q2");
        return props;
    }

}
