package org.apache.airavata.job.monitor.kafka;

import org.apache.airavata.job.monitor.parser.JobStatusResult;
import org.apache.airavata.model.status.JobState;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.Map;

public class JobStatusResultDeserializer implements Deserializer<JobStatusResult> {
    @Override
    public void configure(Map<String, ?> map, boolean b) {

    }

    @Override
    public JobStatusResult deserialize(String s, byte[] bytes) {
        String deserializedData = new String(bytes);
        String[] parts = deserializedData.split(",");
        JobStatusResult jobStatusResult = new JobStatusResult();
        jobStatusResult.setJobId(parts[0]);
        jobStatusResult.setJobName(parts[1]);
        jobStatusResult.setState(JobState.valueOf(parts[2]));
        return jobStatusResult;
    }

    @Override
    public void close() {

    }
}
