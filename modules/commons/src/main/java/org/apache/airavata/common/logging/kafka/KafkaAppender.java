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
package org.apache.airavata.common.logging.kafka;


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import com.google.gson.Gson;
import org.apache.airavata.common.logging.Exception;
import org.apache.airavata.common.logging.LogEntry;
import org.apache.airavata.common.logging.ServerId;
import org.apache.airavata.common.utils.AwsMetadata;
import org.apache.airavata.common.utils.BuildConstant;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Arrays;
import java.util.Properties;

public class KafkaAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {
    private final static Logger logger = LoggerFactory.getLogger(KafkaAppender.class);

    private final Producer<String, String> producer;
    private final String kafkaTopic;

    private  ServerId serverId = null;

    public KafkaAppender(String kafkaHost, String kafkaTopicPrefix) {
        Properties props = new Properties();
        props.put("bootstrap.servers", kafkaHost);
        props.put("acks", "0");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 10000); // Send the batch every 10 seconds
        props.put("buffer.memory", 33554432);
        props.put("producer.type", "async");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        this.kafkaTopic = createKafkaTopic(kafkaTopicPrefix);
        logger.info("Starting kafka producer: bootstrap-server:{}, topic : {}", kafkaHost, this.kafkaTopic);
        this.producer = new KafkaProducer<>(props);
        if(ServerSettings.isRunningOnAws()) {
            final AwsMetadata awsMetadata = new AwsMetadata();
            serverId = new ServerId(awsMetadata.getId(), awsMetadata.getHostname(),
                    BuildConstant.VERSION, ServerSettings.getServerRoles());
        } else {
            serverId = new ServerId(ServerSettings.getIp(), ServerSettings.getIp(),
                    BuildConstant.VERSION, ServerSettings.getServerRoles());
        }
    }

    @Override
    protected void append(ILoggingEvent event) {
        event.prepareForDeferredProcessing();
        //todo do more elegant streaming approach to publish logs

        if (!event.getLevel().equals(Level.ALL) &&         // OFF AND ALL are not loggable levels
                !event.getLevel().equals(Level.OFF)) {
            final IThrowableProxy throwableProxy = event.getThrowableProxy();
            final LogEntry entry = throwableProxy != null ?
                    new LogEntry(serverId, event.getMessage(), Instant.ofEpochMilli(event.getTimeStamp()).toString(),
                            event.getLevel().toString(), event.getLoggerName(), event.getMDCPropertyMap(),
                            event.getThreadName() != null ? event.getThreadName() : null,
                            new Exception(throwableProxy.getMessage(), toStringArray(throwableProxy.getStackTraceElementProxyArray())
                            , throwableProxy.getClassName()))
                    : new LogEntry(serverId, event.getMessage(), Instant.ofEpochMilli(event.getTimeStamp()).toString(),
                    event.getLevel().toString(), event.getLoggerName(), event.getMDCPropertyMap(),
                    event.getThreadName() != null ? event.getThreadName() : null);
            producer.send(new ProducerRecord<>(kafkaTopic, new Gson().toJson(entry)));
        }
    }


    private String[] toStringArray(StackTraceElementProxy[] stackTraceElement) {
        return Arrays.stream(stackTraceElement).map(StackTraceElementProxy::getSTEAsString).toArray(String[]::new);
    }

    private String createKafkaTopic(String kafkaTopicPrefix) {
        final String[] serverRoles = ServerSettings.getServerRoles();
        if (serverRoles.length >= 4) {
            return String.format("%s_all_logs", kafkaTopicPrefix);
        }
        return String.format("%s_%s_logs", kafkaTopicPrefix, ServerSettings.getServerRoles()[0]);
    }

    public void close() {
        producer.close();
    }
}
