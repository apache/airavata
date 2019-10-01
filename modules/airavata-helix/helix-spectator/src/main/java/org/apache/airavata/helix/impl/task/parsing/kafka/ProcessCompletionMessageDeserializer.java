package org.apache.airavata.helix.impl.task.parsing.kafka;

import org.apache.airavata.helix.impl.task.parsing.ProcessCompletionMessage;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

public class ProcessCompletionMessageDeserializer implements Deserializer<ProcessCompletionMessage> {
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public ProcessCompletionMessage deserialize(String topic, byte[] data) {
        String deserialized = new String(data);
        String parts[] = deserialized.split(";");
        ProcessCompletionMessage message = new ProcessCompletionMessage();
        message.setProcessId(parts[0]);
        message.setExperimentId(parts[1]);
        message.setGatewayId(parts[2]);
        return message;
    }

    @Override
    public void close() {

    }
}
