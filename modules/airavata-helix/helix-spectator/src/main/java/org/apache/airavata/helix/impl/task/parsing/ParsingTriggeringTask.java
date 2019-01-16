package org.apache.airavata.helix.impl.task.parsing;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.helix.impl.task.AiravataTask;
import org.apache.airavata.helix.impl.task.TaskContext;
import org.apache.airavata.helix.task.api.TaskHelper;
import org.apache.airavata.helix.task.api.annotation.TaskDef;
import org.apache.helix.task.TaskResult;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.airavata.helix.impl.task.parsing.kafka.ProcessCompletionMessageSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

@TaskDef(name = "Parsing Triggering Task")
public class ParsingTriggeringTask extends AiravataTask {

    private final static Logger logger = LoggerFactory.getLogger(DataParsingTask.class);

    private static Producer<String, ProcessCompletionMessage> producer;

    private void createProducer() throws ApplicationSettingsException {

        if (producer == null) {
            Properties props = new Properties();
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                    ServerSettings.getSetting("kafka.parsing.broker.url"));
            props.put(ProducerConfig.CLIENT_ID_CONFIG, ServerSettings.getSetting("kafka.parsing.broker.publisher.id"));
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                    StringSerializer.class.getName());
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ProcessCompletionMessageSerializer.class.getName());
            producer = new KafkaProducer<String, ProcessCompletionMessage>(props);
        }
    }

    public void submitMessageToParserEngine(ProcessCompletionMessage completionMessage) throws ExecutionException, InterruptedException, ApplicationSettingsException {
        final ProducerRecord<String, ProcessCompletionMessage> record = new ProducerRecord<>(
                ServerSettings.getSetting("kafka.parser.topic"),
                completionMessage.getExperimentId(),
                completionMessage);
        RecordMetadata recordMetadata = producer.send(record).get();
        producer.flush();
    }

    @Override
    public TaskResult onRun(TaskHelper helper, TaskContext taskContext) {

        logger.info("Starting parsing triggerring task " + getTaskId() + ", experiment id " + getExperimentId());

        ProcessCompletionMessage completionMessage = new ProcessCompletionMessage();
        completionMessage.setExperimentId(getExperimentId());
        completionMessage.setProcessId(getProcessId());
        completionMessage.setGatewayId(getGatewayId());

        try {
            createProducer();
            submitMessageToParserEngine(completionMessage);
        } catch (Exception e) {
            logger.error("Failed to submit completion message to parsing engine", e);
        }
        return onSuccess("Successfully completed parsing triggering task");
    }

    @Override
    public void onCancel(TaskContext taskContext) {

    }
}
