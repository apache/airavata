package org.apache.airavata.job.monitor.kafka;

import org.apache.airavata.job.monitor.parser.JobStatusResult;
import org.apache.kafka.common.serialization.Serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Map;

public class JobStatusResultSerializer implements Serializer<JobStatusResult> {

    @Override
    public void configure(Map<String, ?> map, boolean b) {

    }

    @Override
    public byte[] serialize(String s, JobStatusResult jobStatusResult) {
        String serializedData = jobStatusResult.getJobId() + "," + jobStatusResult.getJobName() + "," + jobStatusResult.getState().name();
        return serializedData.getBytes();
    }

    @Override
    public void close() {

    }
}
