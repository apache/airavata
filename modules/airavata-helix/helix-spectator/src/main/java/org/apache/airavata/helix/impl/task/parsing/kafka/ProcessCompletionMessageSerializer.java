package org.apache.airavata.helix.impl.task.parsing.kafka;

import org.apache.airavata.helix.impl.task.parsing.ProcessCompletionMessage;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class ProcessCompletionMessageSerializer implements Serializer<ProcessCompletionMessage> {

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public byte[] serialize(String topic, ProcessCompletionMessage data) {
        String serialized = data.getProcessId() + ";" + data.getExperimentId() + ";" + data.getGatewayId();
        return serialized.getBytes();
    }

    @Override
    public void close() {

    }
}
