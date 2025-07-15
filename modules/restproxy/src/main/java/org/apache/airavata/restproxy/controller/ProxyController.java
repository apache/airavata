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
package org.apache.airavata.restproxy.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.util.Properties;
import org.apache.airavata.restproxy.RestProxyConfiguration;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class ProxyController {
    private static final Logger log = LoggerFactory.getLogger(ProxyController.class);

    private KafkaProducer<String, String> producer;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestProxyConfiguration restProxyConfiguration;

    public ProxyController(RestProxyConfiguration restProxyConfiguration) {
        this.restProxyConfiguration = restProxyConfiguration;
    }

    @PostConstruct
    public void init() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, restProxyConfiguration.getBrokerUrl());
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "RestProxyProducer");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producer = new KafkaProducer<>(props);
    }

    @PostMapping(value = "/topics/{topic}", consumes = "application/vnd.kafka.json.v2+json")
    public ResponseEntity<?> postToKafka(@PathVariable("topic") String topic, @RequestBody String body) {
        try {
            JsonNode root = objectMapper.readTree(body);
            if (!root.has("records")) {
                return ResponseEntity.badRequest().body("Missing 'records' field");
            }
            for (JsonNode record : root.get("records")) {
                JsonNode valueNode = record.get("value");
                if (valueNode == null) continue;
                String valueStr = objectMapper.writeValueAsString(valueNode);
                log.info("Received message for topic {}: {}", topic, valueStr);
                var kafkaRecord = new ProducerRecord<String, String>(topic, valueStr);
                producer.send(kafkaRecord).get();
                log.info("RestProxyProducer posted to topic {}: {}", topic, valueStr);
                producer.flush();
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
